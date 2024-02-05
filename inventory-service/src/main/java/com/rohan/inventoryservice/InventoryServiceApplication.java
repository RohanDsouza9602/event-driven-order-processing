package com.rohan.inventoryservice;

import com.rohan.inventoryservice.model.Inventory;
import com.rohan.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository){
		return args -> {
			Inventory inventory = new Inventory();
			inventory.setSkuCode("iphone_13");
			inventory.setQuantity(10);

			Inventory inventory1 = new Inventory();
			inventory1.setSkuCode("iphone_14");
			inventory1.setQuantity(0);

			Inventory inventory2 = new Inventory();
			inventory2.setSkuCode("iphone_15");
			inventory2.setQuantity(5);

			inventoryRepository.save(inventory);
			inventoryRepository.save(inventory1);
			inventoryRepository.save(inventory2);

		};
	}



}
