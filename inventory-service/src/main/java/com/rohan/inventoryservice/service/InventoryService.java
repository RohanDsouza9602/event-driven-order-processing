package com.rohan.inventoryservice.service;

import com.rohan.inventoryservice.dto.InventoryResponse;
import com.rohan.inventoryservice.model.Inventory;
import com.rohan.inventoryservice.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode){

        List<InventoryResponse> responses = new ArrayList<>();
        for(String code : skuCode) {
            boolean isInStock = inventoryRepository.existsBySkuCode(code);

            if(isInStock) {
                Inventory inventory = inventoryRepository.findBySkuCode(code);
                isInStock = inventory.getQuantity() > 0;
            }

            InventoryResponse response = InventoryResponse.builder()
                    .skuCode(code)
                    .isInStock(isInStock)
                    .build();

            responses.add(response);
        }
        return responses;
    }
}
