package com.example.demo.common.sync.consumer;

import com.example.demo.common.sync.event.DbSyncEvent;
import com.example.demo.common.sync.service.ReplicaSyncService;
import com.example.demo.config.database.DatabaseContextHolder;
import com.example.demo.config.database.DatabaseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Consumes db-sync-events from Kafka and applies them
 * to the read replica (SLAVE) database.
 *
 * Uses a dedicated consumer factory (syncKafkaListenerContainerFactory)
 * with explicit DbSyncEvent deserialization.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplicaSyncConsumer {

    private final ReplicaSyncService replicaSyncService;

    @KafkaListener(
            topics = "${kafka.topics.db-sync:db-sync-events}",
            groupId = "replica-sync-group",
            containerFactory = "syncKafkaListenerContainerFactory"
    )
    public void onSyncEvent(DbSyncEvent syncEvent, Acknowledgment acknowledgment) {
        DatabaseContextHolder.set(DatabaseType.SLAVE);

        try {
            log.info("📥 Received sync event: op={}, type={}, id={}",
                    syncEvent.getOperation(),
                    syncEvent.getAggregateType(),
                    syncEvent.getAggregateId());

            replicaSyncService.sync(syncEvent);

            acknowledgment.acknowledge();

            log.info("Replica synced: op={}, type={}, id={}",
                    syncEvent.getOperation(),
                    syncEvent.getAggregateType(),
                    syncEvent.getAggregateId());

        } catch (Exception e) {
            log.error("Sync failed: op={}, type={}, id={}, error={}",
                    syncEvent.getOperation(),
                    syncEvent.getAggregateType(),
                    syncEvent.getAggregateId(),
                    e.getMessage(), e);
            throw e;

        } finally {
            DatabaseContextHolder.clear();
        }
    }
}