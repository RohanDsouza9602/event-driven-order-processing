package com.rohan.orderservice.kafka;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.messaging.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.rohan.orderservice.event.OrderEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlinkProducer {

    private final NewTopic topic1;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private FlinkProducer(NewTopic topic1, KafkaTemplate<String, String> kafkaTemplate) {
        this.topic1 = topic1;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String orderNumber) {
        log.info(String.format("Order number => {}", orderNumber));

        Message<String> message = MessageBuilder
                .withPayload(orderNumber)
                .setHeader(KafkaHeaders.TOPIC, topic1.name())
                .build();

        kafkaTemplate.send(message);
    }
}
