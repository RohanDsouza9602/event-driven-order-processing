package com.rohan.inventoryservice.repository;

import com.rohan.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {


    List<Inventory> findBySkuCodeIn(List<String> skuCode);

    boolean existsBySkuCode(String code);

    Inventory findBySkuCode(String code);
}
