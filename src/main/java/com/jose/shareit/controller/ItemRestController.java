package com.jose.shareit.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // <-- IMPORTANTE
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jose.shareit.model.Item;
import com.jose.shareit.model.User;
import com.jose.shareit.repository.UserRepository;
import com.jose.shareit.service.BarcodeService; 
import com.jose.shareit.service.ItemService;

@RestController
@RequestMapping("/api/items")  
public class ItemRestController {

    private final ItemService itemService;
    private final UserRepository userRepository;
    private final BarcodeService barcodeService; // Se añade el servicio de barcode

    // Se añade BarcodeService al constructor
    public ItemRestController(ItemService itemService, UserRepository userRepository, BarcodeService barcodeService) {
        this.itemService = itemService;
        this.userRepository = userRepository;
        this.barcodeService = barcodeService;
    }

    @GetMapping
    public List<Item> getAllItems() {
        return itemService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable UUID id) {
        return itemService.findById(id)
                .map(item -> ResponseEntity.ok(item))
                .orElse(ResponseEntity.notFound().build());
    }

    private static class RentRequest {
        private String barcode;
        public String getBarcode() { return barcode; }
    }

    @PostMapping("/{id}/rent")
    public ResponseEntity<Map<String, String>> rentItemAsync(
            @PathVariable UUID id,
            @RequestBody RentRequest rentRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "You must be logged in to rent an item."));
        }
        
        User renter = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

        try {
            itemService.rentItem(id, renter, rentRequest.getBarcode());
            return ResponseEntity.ok(Collections.singletonMap("message", "Item successfully rented!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    @GetMapping(value = "/{id}/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBarcodeImage(@PathVariable UUID id) {
        Item item = itemService.findById(id).orElse(null);

        if (item == null || item.getBarcode() == null || item.getBarcode().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Delega la generación de la imagen al BarcodeService
        byte[] imageBytes = barcodeService.generateBarcodeImage(item.getBarcode());

        // Devuelve los bytes de la imagen con el Content-Type correcto
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
    }
}