package com.app.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;
import tools.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

@Configuration
public class NotificationKafkaConfig {

    Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return properties;
    }

    @Bean
    ConsumerFactory getConsumerFactory() {
        return new DefaultKafkaConsumerFactory(getConsumerProperties());
    }

    //OBJECT MAPPER
    @Bean("transactionObjectMapper")
    ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
