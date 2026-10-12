package com.example.demo.common.sync.service;

import com.example.demo.common.annotation.IgnoreRouting;
import com.example.demo.common.sync.event.DbSyncEvent;
import com.example.demo.config.database.DatabaseContextHolder;
import com.example.demo.config.database.DatabaseType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Applies sync events to the read replica.
 *
 * Handles all cases:
 * - CREATE  → merge() [upsert]
 * - UPDATE  → merge() [upsert, covers soft delete + restore]
 * - DELETE  → find() + remove(), native SQL fallback for @SQLRestriction
 */
@Slf4j
@Service
public class ReplicaSyncServiceImpl implements ReplicaSyncService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @IgnoreRouting
    @Transactional
    public void sync(DbSyncEvent syncEvent) {
        if (DatabaseContextHolder.get() != DatabaseType.SLAVE) {
            log.error("SAFETY VIOLATION: forcing SLAVE context");
            DatabaseContextHolder.set(DatabaseType.SLAVE);
        }

        switch (syncEvent.getOperation()) {
            case CREATE, UPDATE -> handleUpsert(syncEvent);
            case DELETE         -> handleDelete(syncEvent);
        }
    }

    // ── UPSERT ────────────────────────────────────────────────────────

    private void handleUpsert(DbSyncEvent syncEvent) {
        Object payload = syncEvent.getPayload();

        if (payload == null) {
            log.warn("Upsert skipped — null payload: type={} id={}",
                    syncEvent.getAggregateType(),
                    syncEvent.getAggregateId());
            return;
        }

        entityManager.merge(payload);
        entityManager.flush();

        log.info("Upserted to replica: type={} id={} op={}",
                syncEvent.getAggregateType(),
                syncEvent.getAggregateId(),
                syncEvent.getOperation());
    }

    // ── DELETE ────────────────────────────────────────────────────────

    private void handleDelete(DbSyncEvent syncEvent) {
        String aggregateType = syncEvent.getAggregateType();
        String aggregateId = syncEvent.getAggregateId();

        if (aggregateId == null || "null".equals(aggregateId)) {
            log.warn("Delete skipped — null aggregateId: type={}",
                    aggregateType);
            return;
        }

        try {
            Class<?> entityClass = resolveEntityClass(aggregateType);
            Object id = resolveId(aggregateId);
            String tableName = resolveTableName(entityClass);

            // Step 1: JPA find + remove
            // Works for entities NOT hidden by @SQLRestriction
            Object managed = entityManager.find(entityClass, id);

            if (managed != null) {
                entityManager.remove(managed);
                entityManager.flush();
                log.info("Deleted from replica: type={} id={}",
                        aggregateType, aggregateId);
                return;
            }

            // Step 2: Native SQL fallback
            // Handles entities hidden by @SQLRestriction("deleted = false")
            // e.g., soft-deleted entities that need hard deletion
            int deleted = entityManager
                    .createNativeQuery(
                            "DELETE FROM " + tableName + " WHERE id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            if (deleted > 0) {
                log.info("Deleted from replica via native SQL: type={} id={}",
                        aggregateType, aggregateId);
            } else {
                log.warn("Delete skipped — not found in replica: type={} id={}",
                        aggregateType, aggregateId);
            }

        } catch (ClassNotFoundException e) {
            log.error("Cannot resolve entity class: type={}", aggregateType);
        } catch (Exception e) {
            log.error("Delete failed: type={} id={} error={}",
                    aggregateType, aggregateId, e.getMessage(), e);
            throw e;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    /**
     * Resolve entity class from aggregate type name.
     * Add all your domain model packages here.
     */
    private Class<?> resolveEntityClass(String aggregateType)
            throws ClassNotFoundException {

        String[] packages = {
                "com.example.demo.modules.kitchen.domain.model",
                "com.example.demo.modules.order.domain.model",
                "com.example.demo.modules.user.domain.model",
                "com.example.demo.modules.payment.domain.model",
                // Add new domain packages here as needed
        };

        for (String pkg : packages) {
            try {
                return Class.forName(pkg + "." + aggregateType);
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException(
                "Entity class not found for: " + aggregateType +
                ". Add its package to ReplicaSyncServiceImpl.resolveEntityClass()");
    }

    /**
     * Resolve table name from @Table annotation or class name.
     */
    private String resolveTableName(Class<?> entityClass) {
        jakarta.persistence.Table tableAnn =
                entityClass.getAnnotation(jakarta.persistence.Table.class);

        if (tableAnn != null && !tableAnn.name().isEmpty()) {
            return tableAnn.name();
        }

        // CamelCase → snake_case
        return entityClass.getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    /**
     * Parse ID — tries UUID first, then Long, then raw String.
     */
    private Object resolveId(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e1) {
            try {
                return Long.parseLong(id);
            } catch (NumberFormatException e2) {
                return id;
            }
        }
    }
}