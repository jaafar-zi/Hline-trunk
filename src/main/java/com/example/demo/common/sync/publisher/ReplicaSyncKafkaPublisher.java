package com.example.demo.common.sync.publisher;

import com.example.demo.common.event.EntityBulkCreatedEvent;
import com.example.demo.common.event.EntityBulkDeletedEvent;
import com.example.demo.common.event.EntityBulkUpdatedEvent;
import com.example.demo.common.event.EntityCreatedEvent;
import com.example.demo.common.event.EntityDeletedEvent;
import com.example.demo.common.event.EntityRestoredEvent;
import com.example.demo.common.event.EntityUpdatedEvent;
import com.example.demo.common.sync.event.DbSyncEvent;
import com.example.demo.common.sync.event.DbSyncEvent.SyncOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens to base entity events published by AbstractCommandService
 * and sends them to Kafka for replica synchronization.
 *
 * Fires AFTER_COMMIT only:
 * - If master transaction rolls back → no event published → replica stays consistent
 * - If master transaction commits    → event published    → replica gets updated
 *
 * Uses aggregateId as Kafka partition key → guarantees ordering per entity.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplicaSyncKafkaPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.db-sync:db-sync-events}")
    private String dbSyncTopic;

    // ── Single Entity Events ──────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityCreated(EntityCreatedEvent<?> event) {
        publish(
                DbSyncEvent.builder()
                        .operation(SyncOperation.CREATE)
                        .aggregateType(event.getEntityType())
                        .aggregateId(String.valueOf(event.getEntityId()))
                        .payload(event.getEntity())
                        .triggeredBy(event.getTriggeredBy())
                        .build(),
                String.valueOf(event.getEntityId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityUpdated(EntityUpdatedEvent<?> event) {
        publish(
                DbSyncEvent.builder()
                        .operation(SyncOperation.UPDATE)
                        .aggregateType(event.getEntityType())
                        .aggregateId(String.valueOf(event.getEntityId()))
                        .payload(event.getEntity())
                        .triggeredBy(event.getTriggeredBy())
                        .build(),
                String.valueOf(event.getEntityId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityDeleted(EntityDeletedEvent event) {
        publish(
                DbSyncEvent.builder()
                        .operation(SyncOperation.DELETE)
                        .aggregateType(event.getEntityType())
                        .aggregateId(String.valueOf(event.getEntityId()))
                        .payload(null)
                        .triggeredBy(event.getTriggeredBy())
                        .build(),
                String.valueOf(event.getEntityId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityRestored(EntityRestoredEvent event) {
        // Restore has no entity payload
        // The actual restore is handled as UPDATE
        // because restore() calls update() in the command service
        // This listener is a safety net
        log.debug("EntityRestoredEvent received: type={} id={} — " +
                  "handled via EntityUpdatedEvent",
                event.getEntityType(), event.getEntityId());
    }

    // ── Bulk Entity Events ────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityBulkCreated(EntityBulkCreatedEvent<?> event) {
        for (int i = 0; i < event.getEntities().size(); i++) {
            Object entity = event.getEntities().get(i);
            String id = i < event.getEntityIds().size()
                    ? String.valueOf(event.getEntityIds().get(i))
                    : "unknown-" + i;

            publish(
                    DbSyncEvent.builder()
                            .operation(SyncOperation.CREATE)
                            .aggregateType(event.getEntityType())
                            .aggregateId(id)
                            .payload(entity)
                            .triggeredBy(event.getTriggeredBy())
                            .build(),
                    id
            );
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityBulkUpdated(EntityBulkUpdatedEvent<?> event) {
        for (int i = 0; i < event.getEntities().size(); i++) {
            Object entity = event.getEntities().get(i);
            String id = i < event.getEntityIds().size()
                    ? String.valueOf(event.getEntityIds().get(i))
                    : "unknown-" + i;

            publish(
                    DbSyncEvent.builder()
                            .operation(SyncOperation.UPDATE)
                            .aggregateType(event.getEntityType())
                            .aggregateId(id)
                            .payload(entity)
                            .triggeredBy(event.getTriggeredBy())
                            .build(),
                    id
            );
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityBulkDeleted(EntityBulkDeletedEvent event) {
        for (Object entityId : event.getEntityIds()) {
            String id = String.valueOf(entityId);

            publish(
                    DbSyncEvent.builder()
                            .operation(SyncOperation.DELETE)
                            .aggregateType(event.getEntityType())
                            .aggregateId(id)
                            .payload(null)
                            .triggeredBy(event.getTriggeredBy())
                            .build(),
                    id
            );
        }
    }

    // ── Kafka Send ────────────────────────────────────────────────────

    private void publish(DbSyncEvent syncEvent, String partitionKey) {
        kafkaTemplate.send(dbSyncTopic, partitionKey, syncEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Replica sync failed to publish: " +
                                  "type={}, id={}, op={}, error={}",
                                syncEvent.getAggregateType(),
                                syncEvent.getAggregateId(),
                                syncEvent.getOperation(),
                                ex.getMessage(), ex);
                    } else {
                        log.info("Replica sync published: " +
                                 "type={}, id={}, op={}, " +
                                 "partition={}, offset={}",
                                syncEvent.getAggregateType(),
                                syncEvent.getAggregateId(),
                                syncEvent.getOperation(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}