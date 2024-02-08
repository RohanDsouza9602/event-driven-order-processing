package com.rohan.flinkservice.flink.serialization;

import com.rohan.flinkservice.OrderEvent;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nullable;


public class OrderEventSerializer implements KafkaRecordSerializationSchema<OrderEvent> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private String topic;

    public OrderEventSerializer(String topic) {
        this.topic = topic;
    }

    @Nullable
    @Override
    public ProducerRecord<byte[], byte[]> serialize(OrderEvent orderEvent, KafkaSinkContext kafkaSinkContext, Long aLong) {
        try {
            byte[] value = objectMapper.writeValueAsBytes(orderEvent);
            byte[] key = orderEvent.getOrderNumber().getBytes();
            return new ProducerRecord<>(topic, key, value);
        } catch (Exception e) {
            return null;
        }
    }
}
