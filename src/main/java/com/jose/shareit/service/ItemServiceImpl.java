package com.jose.shareit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jose.shareit.model.Item;
import com.jose.shareit.model.User;
import com.jose.shareit.repository.ItemRepository;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class); // para poder depurar

    @Autowired
    private ItemRepository itemRepo;

    @Override
    public List<Item> findAll() {
        return itemRepo.findAll();
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return itemRepo.findById(id);
    }
    
    @Override
    public Map<String, Object> getCategorizedItemsForUser(User currentUser) {
        Map<String, Object> categorizedItems = new HashMap<>();
        List<Item> allItems = itemRepo.findAll();
        List<Item> availableItems = new ArrayList<>();
        List<Item> rentedItems = new ArrayList<>();
        List<Item> yourOfferedItems = new ArrayList<>();
        List<Item> itemsYouveLent = new ArrayList<>();

        for (Item item : allItems) {
            boolean isOwner = currentUser != null && item.getOwner() != null && item.getOwner().equals(currentUser);
            boolean isRentedByMe = currentUser != null && item.getRentedBy() != null && item.getRentedBy().equals(currentUser);

            if (isOwner) {
                if (item.isRented()) itemsYouveLent.add(item);
                else yourOfferedItems.add(item);
            } else {
                if (isRentedByMe) rentedItems.add(item);
                else if (!item.isRented()) availableItems.add(item);
            }
        }
        
        categorizedItems.put("availableItems", availableItems);
        categorizedItems.put("rentedItems", rentedItems);
        categorizedItems.put("yourOfferedItems", yourOfferedItems);
        categorizedItems.put("itemsYouveLent", itemsYouveLent);
        return categorizedItems;
    }

    @Override
    public Item createNewItem(Item item, User owner) {
        item.setOwner(owner);
        item.setRented(false);
        item.setStatus("Available");
        item.setBarcode(java.util.UUID.randomUUID().toString().substring(0, 12));
        return itemRepo.save(item);
    }

    @Override
    public Item rentItem(UUID itemId, User renter, String barcode) {
        Item item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalStateException("Item not found."));
        if (item.isRented()) throw new IllegalStateException("This item is already rented.");
        item.setRented(true);
        item.setRentedBy(renter);
        item.setStatus("Unavailable");
        item.setBarcode(barcode);
        return itemRepo.save(item);
    }

    @Override
    public Item makeAvailable(UUID itemId) {
        Item item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalStateException("Item not found."));
        item.setRented(false);
        item.setRentedBy(null);
        item.setStatus("Available");
        return itemRepo.save(item);
    }

    @Override
    public Item generateNewBarcode(UUID itemId) {
        Item item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalStateException("Item not found."));
        item.setBarcode(java.util.UUID.randomUUID().toString().substring(0, 12));
        return itemRepo.save(item);
    }
    
    @Override
    public Item markAsLent(UUID itemId) {
        Item item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalStateException("Item not found."));
        if(item.isRented()){
            throw new IllegalStateException("This item is already marked as lent.");
        }
        item.setRented(true);
        item.setStatus("Unavailable");
        return itemRepo.save(item);
    }
    
    @Override
    public void deleteItem(UUID itemId, User currentUser) {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to delete an item.");
        }

        Item item = itemRepo.findById(itemId)
            .orElseThrow(() -> new IllegalStateException("Item with ID " + itemId + " not found."));
            
        
        log.info("DELETE ATTEMPT: User '{}' with role '{}' is trying to delete item '{}' owned by '{}'.", // Mensaje de log para ver qué está pasando en tiempo real.
                 currentUser.getEmail(), 
                 currentUser.getRole(), 
                 item.getName(), 
                 (item.getOwner() != null ? item.getOwner().getName() : "N/A"));

        
        boolean isAdmin = "ROLE_ADMIN".equals(currentUser.getRole()); // comprueba si el usuario tiene el rol de administrador.
        boolean isOwner = item.getOwner() != null && item.getOwner().equals(currentUser); // comprueba si el usuario es el propietario del ítem.

        if (isAdmin || isOwner) { // La condición correcta: se permite la acción si es admin O si es el dueño
            log.info("PERMISSION GRANTED: Deleting item '{}'.", item.getName());
            itemRepo.delete(item);
        } else {
            log.warn("PERMISSION DENIED: User '{}' does not have permission.", currentUser.getEmail());
            throw new IllegalStateException("You do not have permission to delete this item.");
        }
    }
}