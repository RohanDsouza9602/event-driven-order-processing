package com.rohan.flinkservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
public class FlinkServiceApplication {

	public static void main(String[] args) throws Exception {

		SpringApplication.run(FlinkServiceApplication.class, args);

	}

	@KafkaListener(
			topics = "${flink.topic}",
			groupId = "${spring.kafka.consumer.group-id}"
	)
	public void handleLog(String orderNumberProcessed){
		System.out.println(orderNumberProcessed);
	}

}
