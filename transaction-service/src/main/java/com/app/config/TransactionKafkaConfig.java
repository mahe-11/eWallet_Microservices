package com.app.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import tools.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

@Configuration
public class TransactionKafkaConfig {

    //PRODUCER RELATED METHODS
    Properties getProducerProperties(){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);

        return properties;
    }

    ProducerFactory getProducerFactory(){
        return new DefaultKafkaProducerFactory(getProducerProperties());
    }

    //CONSUMER RELATED METHODS
    Properties getConsumerProperties(){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);

        return properties;
    }

    ConsumerFactory getConsumerFactory(){
        return new DefaultKafkaConsumerFactory(getConsumerProperties());
    }


     //KAFKA TEMPLATE FOR PRODUCER
    @Bean
    KafkaTemplate getKafkaTemplate(){
        return new KafkaTemplate(getProducerFactory());
    }

    //OBJECT MAPPER
    @Bean("transactionObjectMapper")
    ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }
}
