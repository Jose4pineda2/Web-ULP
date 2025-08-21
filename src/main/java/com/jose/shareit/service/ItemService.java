package com.jose.shareit.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.jose.shareit.model.Item;
import com.jose.shareit.model.User;

public interface ItemService {

    List<Item> findAll();
    Optional<Item> findById(UUID id);
    Map<String, Object> getCategorizedItemsForUser(User currentUser);
    Item createNewItem(Item item, User owner);
    Item rentItem(UUID itemId, User renter, String barcode);
    Item makeAvailable(UUID itemId);
    Item generateNewBarcode(UUID itemId);
    Item markAsLent(UUID itemId);

    
    /**
     * Borra un ítem si el usuario actual tiene permiso.
     * @param itemId El ID del ítem a borrar.
     * @param currentUser El usuario que realiza la solicitud.
     * @throws IllegalStateException si el usuario no tiene permiso para borrar el ítem.
     */
    void deleteItem(UUID itemId, User currentUser);
}