package com.rohan.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
	}

	@KafkaListener(
		topics = "${spring.kafka.template.default-topic}",
		groupId = "${spring.kafka.consumer.group-id}")
	public void handleNotification(OrderEvent orderEvent){
		System.out.println("Received Notification for Order - "+orderEvent.getOrderNumber());
	}

}
