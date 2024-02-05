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
public class OrderProducer {

    private final NewTopic topic;
    
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private OrderProducer(NewTopic topic, KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(OrderEvent orderEvent) {
        log.info(String.format("Order event => {}", orderEvent.toString()));

        Message<OrderEvent> message = MessageBuilder
                .withPayload(orderEvent)
                .setHeader(KafkaHeaders.TOPIC, topic.name())
                .build();

        kafkaTemplate.send(message);
    }

}


    

