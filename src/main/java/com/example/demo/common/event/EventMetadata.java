package com.example.demo.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Event metadata information
 *
 * @author Architecture Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Event ID
     */
    private String eventId;

    /**
     * Event type
     */
    private String eventType;

    /**
     * Event source
     */
    private String source;

    /**
     * Event version
     */
    private String version;

    /**
     * When event occurred
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime occurredAt;

    /**
     * Correlation ID
     */
    private String correlationId;

    /**
     * Causation ID
     */
    private String causationId;

    /**
     * User who triggered the event
     */
    private String triggeredBy;

    /**
     * Additional metadata
     */
    @Builder.Default
    private Map<String, Object> additionalData = new HashMap<>();

    /**
     * Add additional data
     */
    public void addData(String key, Object value) {
        if (this.additionalData == null) {
            this.additionalData = new HashMap<>();
        }
        this.additionalData.put(key, value);
    }
}