package com.jose.shareit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jose.shareit.model.Item;

public interface ItemRepository extends JpaRepository<Item, UUID> { 
    List<Item> findByRentedFalse();
}