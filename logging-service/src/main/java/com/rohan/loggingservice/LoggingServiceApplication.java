package com.rohan.loggingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
public class LoggingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoggingServiceApplication.class, args);
	}

	@KafkaListener(
		topics = "${spring.kafka.template.default-topic}",
		groupId = "${spring.kafka.consumer.group-id}")
	public void handleNotification(OrderEvent orderEvent){
		System.out.println("Received Notification for Order - "+orderEvent);
	}

}
