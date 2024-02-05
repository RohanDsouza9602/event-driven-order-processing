package com.rohan.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    @Value("${flink.topic}")
    private String topic1Name;
    
    @Bean
    public NewTopic topic(){
        return TopicBuilder.name(topicName)
                .build();
    }

    @Bean
    public NewTopic topic1(){
        return TopicBuilder.name(topic1Name)
                .build();
    }
}
