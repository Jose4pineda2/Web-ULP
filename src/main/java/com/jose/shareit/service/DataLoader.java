package com.jose.shareit.service;

import java.util.Arrays;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jose.shareit.model.Item;
import com.jose.shareit.model.User;
import com.jose.shareit.repository.ItemRepository;
import com.jose.shareit.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {
        // only executed if there are no users in the database.
        // This prevents creating duplicate items every time we restart the server.
        if (userRepository.count() == 0) {
            log.info("Database is empty. Loading initial data...");

            // 1. Create a default "owner" user for the sample items
            User systemOwner = new User();
            systemOwner.setName("Platform Owner");
            systemOwner.setEmail("system@shareit.com");
            systemOwner.setPassword(passwordEncoder.encode("system")); // Simple password for the system owner
            systemOwner.setRole("ROLE_USER"); // Can be a regular user
            userRepository.save(systemOwner);
            log.info("'Platform Owner' user created.");


            // 2. Create a list of sample items
            Item item1 = new Item();
            item1.setName("Bosch Hammer Drill");
            item1.setDescription("Powerful drill for home DIY projects. Includes a set of drill bits.");
            item1.setCategory("Tools");
            item1.setPicture("/uploads/Default_Items/Electric_Drill.jpg"); 
            item1.setOwner(systemOwner);
            item1.setStatus("Available");
            item1.setRented(false);
            item1.setBarcode(UUID.randomUUID().toString().substring(0, 12));

            Item item2 = new Item();
            item2.setName("4-Person Camping Tent");
            item2.setDescription("Water-resistant and easy to assemble. Ideal for weekend getaways.");
            item2.setCategory("Camping Gear");
            item2.setPicture("/uploads/Default_Items/Camping_tent.jpg");
            item2.setOwner(systemOwner);
            item2.setStatus("Available");
            item2.setRented(false);
            item2.setBarcode(UUID.randomUUID().toString().substring(0, 12));

            Item item3 = new Item();
            item3.setName("Catan Board Game");
            item3.setDescription("Strategy and resource management game. Complete and in perfect condition.");
            item3.setCategory("Games");
            item3.setPicture("/uploads/Default_Items/catan.jpg");
            item3.setOwner(systemOwner);
            item3.setStatus("Available");
            item3.setRented(false);
            item3.setBarcode(UUID.randomUUID().toString().substring(0, 12));
            
            Item item4 = new Item();
            item4.setName("Full HD Video Projector");
            item4.setDescription("Projector with HDMI and USB connection. Perfect for movie nights.");
            item4.setCategory("Equipment");
            item4.setPicture("/uploads/Default_Items/projector.jpg");
            item4.setOwner(systemOwner);
            item4.setStatus("Available");
            item4.setRented(false);
            item4.setBarcode(UUID.randomUUID().toString().substring(0, 12));


            // 3. Save all items to the database
            itemRepository.saveAll(Arrays.asList(item1, item2, item3, item4));
            log.info("Loaded {} sample items into the database.", itemRepository.count());

        } else {
            log.info("Database already contains data. No initial data will be loaded.");
        }
    }
}