package com.example.demo.config.messaging;

import com.example.demo.common.sync.event.DbSyncEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated Kafka consumer configuration for replica sync.
 *
 * Separated from the main KafkaConsumerConfig so that:
 * - Sync messages are deserialized to DbSyncEvent explicitly
 * - Sync consumer group is independent from business consumers
 * - Error handling and retry policy can differ from business consumers
 * - Does not affect existing Kafka consumers
 */
@Slf4j
@Configuration
public class KafkaSyncConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, DbSyncEvent> syncConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "replica-sync-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        // Explicit typed deserializer — no LinkedHashMap issue
        JsonDeserializer<DbSyncEvent> jsonDeserializer =
                new JsonDeserializer<>(DbSyncEvent.class);
        jsonDeserializer.addTrustedPackages("com.example.demo.*");
        jsonDeserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DbSyncEvent>
            syncKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, DbSyncEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(syncConsumerFactory());

        // Single thread — preserves ordering per partition
        factory.setConcurrency(1);

        // Manual ack — commit offset only after successful sync
        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.MANUAL);

        // Retry 3 times with 2s delay, then skip (logged by error handler)
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(
                        (record, exception) -> {
                            log.error("❌ Sync consumer failed after retries: " +
                                      "topic={}, partition={}, offset={}, error={}",
                                    record.topic(),
                                    record.partition(),
                                    record.offset(),
                                    exception.getMessage(), exception);
                        },
                        new FixedBackOff(2000L, 3L)
                )
        );

        return factory;
    }
}