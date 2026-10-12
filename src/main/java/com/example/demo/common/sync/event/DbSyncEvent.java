package com.example.demo.common.sync.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka message for replica synchronization.
 *
 * Carries the operation type and entity payload.
 * CREATE/UPDATE → payload contains the full entity
 * DELETE        → payload is null, uses aggregateType + aggregateId
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DbSyncEvent {

    private SyncOperation operation;
    private String aggregateType;
    private String aggregateId;
    private String triggeredBy;

    /**
     * The full entity payload.
     * @JsonTypeInfo preserves the concrete class during serialization
     * so Jackson can deserialize it back to the correct entity type.
     */
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,
            include = JsonTypeInfo.As.PROPERTY,
            property = "@class"
    )
    private Object payload;

    public enum SyncOperation {
        CREATE,
        UPDATE,
        DELETE
    }
}