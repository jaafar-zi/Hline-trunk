package com.example.demo.config.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka topic configuration
 * Auto-creates topics on application startup
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka admin for topic management
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * User events topic
     */
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name("user-events")
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Order events topic
     */
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events")
                .partitions(5)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Payment events topic
     */
    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name("payment-events")
                .partitions(3)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Notification events topic
     */
    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("notification-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Dead Letter Queue topic
     */
    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name("dlq-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Database sync topic - propagates write events to read replica
     */
    @Bean
    public NewTopic dbSyncTopic() {
        return TopicBuilder.name("db-sync-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}