package com.jose.shareit.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jose.shareit.model.Item;
import com.jose.shareit.model.User;
import com.jose.shareit.repository.UserRepository;
import com.jose.shareit.service.FileStorageService;
import com.jose.shareit.service.ItemService;

@Controller
public class ObjectController {

    @Autowired private ItemService itemService;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private UserRepository userRepository;

    private User getFullUser(UserDetails userDetails) {
        if (userDetails == null) { return null; }
        return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
    }
    
    @GetMapping("/allObjects")
    public String viewDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAllAttributes(itemService.getCategorizedItemsForUser(getFullUser(userDetails)));
        model.addAttribute("user", getFullUser(userDetails));
        return "allObjects";
    }

    @GetMapping("/newObject")
    public String showNewItemForm(Model model) {
        model.addAttribute("item", new Item());
        return "newObject";
    }

    @PostMapping("/newObject")
    public String createItem(@ModelAttribute Item item, @RequestParam("imageFile") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        item.setPicture("/uploads/" + fileStorageService.storeFile(file));
        itemService.createNewItem(item, getFullUser(userDetails));
        return "redirect:/allObjects";
    }
    
    @GetMapping("/item/{id}")
    public String viewItem(@PathVariable UUID id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("item", itemService.findById(id).orElse(null));
        model.addAttribute("user", getFullUser(userDetails));
        return "itemDetail";
    }
    
    @GetMapping("/delete-item")
    public String showDeleteItemPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getFullUser(userDetails);
        List<Item> allItems = itemService.findAll();
        List<Item> deletableItems;
        boolean isAdmin = userDetails.getAuthorities().stream()
                                     .map(GrantedAuthority::getAuthority)
                                     .anyMatch(role -> role.equals("ROLE_ADMIN"));
        if (isAdmin) {
            deletableItems = allItems;
        } else {
            deletableItems = allItems.stream()
                .filter(i -> i.getOwner() != null && i.getOwner().equals(currentUser))
                .collect(Collectors.toList());
        }
        model.addAttribute("deletableItems", deletableItems);
        return "deleteItem";
    }

    @PostMapping("/delete-item/{id}")
    public String processDeleteItem(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        User currentUser = getFullUser(userDetails);
        try {
            itemService.deleteItem(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully deleted.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/delete-item";
    }

    @GetMapping("/scanner")
    public String showScannerToRent(@RequestParam UUID id, Model model) {
        model.addAttribute("itemId", id);
        return "scanner";
    }

    @GetMapping("/renter/return-item/{id}")
    public String showReturnPageForRenter(@PathVariable UUID id, Model model) {
        model.addAttribute("item", itemService.findById(id).orElse(null));
        model.addAttribute("pageTitle", "Give Item Back");
        model.addAttribute("formAction", "/renter/confirm-return/" + id);
        return "returnBarcode";
    }

    @PostMapping("/renter/confirm-return/{id}")
    public String confirmReturnByRenter(@PathVariable UUID id) {
        itemService.makeAvailable(id);
        return "redirect:/allObjects";
    }

    @GetMapping("/owner/give-item/{id}")
    public String showOfferPageForOwner(@PathVariable UUID id, Model model) {
        model.addAttribute("item", itemService.findById(id).orElse(null));
        model.addAttribute("pageTitle", "Give Item: " + itemService.findById(id).get().getName());
        model.addAttribute("confirmAction", "/owner/confirm-rented/" + id);
        model.addAttribute("generateAction", "/owner/generate-new-barcode/" + id);
        return "barcode";
    }
    
    @PostMapping("/owner/confirm-rented/{id}")
    public String confirmItemIsGiven(@PathVariable UUID id) {
        itemService.markAsLent(id);
        return "redirect:/allObjects";
    }
    
    @PostMapping("/owner/generate-new-barcode/{id}")
    public String generateNewBarcodeForOwner(@PathVariable UUID id) {
        itemService.generateNewBarcode(id);
        return "redirect:/owner/give-item/" + id;
    }

    @GetMapping("/owner/get-item-back/{id}")
    public String showGetBackPageForOwner(@PathVariable UUID id, Model model) {
        model.addAttribute("item", itemService.findById(id).orElse(null));
        model.addAttribute("pageTitle", "Get Item Back");
        model.addAttribute("formAction", "/owner/confirm-get-back/" + id);
        return "returnBarcode";
    }
    
    @PostMapping("/owner/confirm-get-back/{id}")
    public String confirmGetBackByOwner(@PathVariable UUID id) {
        itemService.makeAvailable(id);
        return "redirect:/allObjects";
    }
}