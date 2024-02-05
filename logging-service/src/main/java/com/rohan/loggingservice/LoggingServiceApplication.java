package com.rohan.loggingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
public class LoggingServiceApplication {

	public static void main(String[] args) throws Exception {

		SpringApplication.run(LoggingServiceApplication.class, args);

	}

	@KafkaListener(topics = "${flink.topic}", groupId = "${spring.kafka.consumer.group-id}")
	public void handleLog(String orderNumber){
		System.out.println(orderNumber);
	}

}
