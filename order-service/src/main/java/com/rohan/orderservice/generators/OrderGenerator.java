package com.rohan.orderservice.generators;

import com.rohan.orderservice.event.OrderEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

public class OrderGenerator {

    private static final List<String> emailList = List.of(
            "rohan@gmail.com",
            "krishna@gmail.com",
            "atharva@gmail.com",
            "suraj@gmail.com"
    );

    private static final List<String> skuList = List.of(
            "iphone_13",
            "iphone_14",
            "iphone_15"
    );

    private static final List<BigDecimal> priceList = List.of(
            BigDecimal.valueOf(0),
            BigDecimal.valueOf(41000),
            BigDecimal.valueOf(56000),
            BigDecimal.valueOf(65000)
    );

    private static final Random random = new Random();

    private static final String TOPIC_NAME = "notificationId";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            int a = 0;
            for (int i = 0; i < 1000000; i++) {
                OrderEvent orderEvent = generateOrderEvent();
                String orderEventJson = convertOrderEventToJson(orderEvent);
                producer.send(new ProducerRecord<>(TOPIC_NAME, orderEventJson));
                System.out.println("Sent order event to Kafka: " + orderEventJson);
                a = i+1;
            }
            System.out.println("Total orders processed: "+a);
        }
    }

    private static OrderEvent generateOrderEvent() {
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderNumber(UUID.randomUUID().toString());
        orderEvent.setOrderEmail(getRandomEmail());
        orderEvent.setTotalOrderValue(getRandomPrice());
        orderEvent.setOrderStatus(getRandomStatus(orderEvent.getTotalOrderValue()));
        return orderEvent;
    }

    private static String getRandomEmail() {
        return emailList.get(random.nextInt(emailList.size()));
    }

    private static BigDecimal getRandomPrice() {
        return priceList.get(random.nextInt(priceList.size()));
    }

    private static String getRandomStatus(BigDecimal totalOrderValue) {
        return totalOrderValue.compareTo(BigDecimal.ZERO) > 0 ? "CONFIRMED" : "FAILED";
    }

    private static String convertOrderEventToJson(OrderEvent orderEvent) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{")
                .append("\"orderNumber\":\"").append(orderEvent.getOrderNumber()).append("\",")
                .append("\"orderEmail\":\"").append(orderEvent.getOrderEmail()).append("\",")
                .append("\"totalOrderValue\":").append(orderEvent.getTotalOrderValue()).append(",")
                .append("\"orderStatus\":\"").append(orderEvent.getOrderStatus()).append("\"")
                .append("}");
        return jsonBuilder.toString();
    }
}
