package com.example.demo.common.constant;

/**
 * Kafka topics and messaging constants
 *
 * Defines Kafka topic names, consumer groups, partitions,
 * and messaging configuration
 *
 * @author Architecture Team
 * @version 1.0
 */
public final class KafkaTopics {

    private KafkaTopics() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========== Topic Naming Convention ==========
    // Format: {domain}.{entity}.{action}.{version}
    // Example: user.account.created.v1

    // ========== User Domain Topics ==========

    /**
     * User registration and authentication events
     */
    public static final String TOPIC_USER_REGISTERED = "user.account.registered.v1";
    public static final String TOPIC_USER_UPDATED = "user.account.updated.v1";
    public static final String TOPIC_USER_DELETED = "user.account.deleted.v1";
    public static final String TOPIC_USER_ACTIVATED = "user.account.activated.v1";
    public static final String TOPIC_USER_DEACTIVATED = "user.account.deactivated.v1";
    public static final String TOPIC_USER_LOCKED = "user.account.locked.v1";
    public static final String TOPIC_USER_UNLOCKED = "user.account.unlocked.v1";

    /**
     * User authentication events
     */
    public static final String TOPIC_USER_LOGIN = "user.auth.login.v1";
    public static final String TOPIC_USER_LOGOUT = "user.auth.logout.v1";
    public static final String TOPIC_USER_PASSWORD_CHANGED = "user.auth.password-changed.v1";
    public static final String TOPIC_USER_PASSWORD_RESET = "user.auth.password-reset.v1";

    /**
     * User profile events
     */
    public static final String TOPIC_USER_PROFILE_UPDATED = "user.profile.updated.v1";
    public static final String TOPIC_USER_PREFERENCES_UPDATED = "user.preferences.updated.v1";

    // ========== Order Domain Topics ==========

    /**
     * Order lifecycle events
     */
    public static final String TOPIC_ORDER_CREATED = "order.order.created.v1";
    public static final String TOPIC_ORDER_UPDATED = "order.order.updated.v1";
    public static final String TOPIC_ORDER_CANCELLED = "order.order.cancelled.v1";
    public static final String TOPIC_ORDER_CONFIRMED = "order.order.confirmed.v1";
    public static final String TOPIC_ORDER_SHIPPED = "order.order.shipped.v1";
    public static final String TOPIC_ORDER_DELIVERED = "order.order.delivered.v1";
    public static final String TOPIC_ORDER_RETURNED = "order.order.returned.v1";

    /**
     * Order payment events
     */
    public static final String TOPIC_ORDER_PAYMENT_PENDING = "order.payment.pending.v1";
    public static final String TOPIC_ORDER_PAYMENT_COMPLETED = "order.payment.completed.v1";
    public static final String TOPIC_ORDER_PAYMENT_FAILED = "order.payment.failed.v1";
    public static final String TOPIC_ORDER_REFUND_INITIATED = "order.payment.refund-initiated.v1";
    public static final String TOPIC_ORDER_REFUND_COMPLETED = "order.payment.refund-completed.v1";

    // ========== Product Domain Topics ==========

    /**
     * Product events
     */
    public static final String TOPIC_PRODUCT_CREATED = "product.product.created.v1";
    public static final String TOPIC_PRODUCT_UPDATED = "product.product.updated.v1";
    public static final String TOPIC_PRODUCT_DELETED = "product.product.deleted.v1";
    public static final String TOPIC_PRODUCT_PRICE_CHANGED = "product.product.price-changed.v1";

    /**
     * Inventory events
     */
    public static final String TOPIC_INVENTORY_UPDATED = "product.inventory.updated.v1";
    public static final String TOPIC_INVENTORY_LOW_STOCK = "product.inventory.low-stock.v1";
    public static final String TOPIC_INVENTORY_OUT_OF_STOCK = "product.inventory.out-of-stock.v1";
    public static final String TOPIC_INVENTORY_RESTOCKED = "product.inventory.restocked.v1";

    // ========== Notification Topics ==========

    /**
     * Email notifications
     */
    public static final String TOPIC_EMAIL_SEND = "notification.email.send.v1";
    public static final String TOPIC_EMAIL_SENT = "notification.email.sent.v1";
    public static final String TOPIC_EMAIL_FAILED = "notification.email.failed.v1";

    /**
     * SMS notifications
     */
    public static final String TOPIC_SMS_SEND = "notification.sms.send.v1";
    public static final String TOPIC_SMS_SENT = "notification.sms.sent.v1";
    public static final String TOPIC_SMS_FAILED = "notification.sms.failed.v1";

    /**
     * Push notifications
     */
    public static final String TOPIC_PUSH_SEND = "notification.push.send.v1";
    public static final String TOPIC_PUSH_SENT = "notification.push.sent.v1";
    public static final String TOPIC_PUSH_FAILED = "notification.push.failed.v1";

    // ========== Payment Topics ==========

    /**
     * Payment events
     */
    public static final String TOPIC_PAYMENT_INITIATED = "payment.transaction.initiated.v1";
    public static final String TOPIC_PAYMENT_PROCESSING = "payment.transaction.processing.v1";
    public static final String TOPIC_PAYMENT_SUCCESS = "payment.transaction.success.v1";
    public static final String TOPIC_PAYMENT_FAILED = "payment.transaction.failed.v1";
    public static final String TOPIC_PAYMENT_REFUNDED = "payment.transaction.refunded.v1";

    // ========== Analytics Topics ==========

    /**
     * Analytics and tracking events
     */
    public static final String TOPIC_ANALYTICS_EVENT = "analytics.event.tracked.v1";
    public static final String TOPIC_ANALYTICS_PAGE_VIEW = "analytics.page.viewed.v1";
    public static final String TOPIC_ANALYTICS_CLICK = "analytics.click.tracked.v1";
    public static final String TOPIC_ANALYTICS_CONVERSION = "analytics.conversion.tracked.v1";

    // ========== Audit Topics ==========

    /**
     * Audit log events
     */
    public static final String TOPIC_AUDIT_LOG = "audit.log.created.v1";
    public static final String TOPIC_AUDIT_SECURITY = "audit.security.event.v1";
    public static final String TOPIC_AUDIT_DATA_CHANGE = "audit.data.changed.v1";

    // ========== System Topics ==========

    /**
     * System events
     */
    public static final String TOPIC_SYSTEM_HEALTH = "system.health.check.v1";
    public static final String TOPIC_SYSTEM_ERROR = "system.error.occurred.v1";
    public static final String TOPIC_SYSTEM_ALERT = "system.alert.raised.v1";

    // ========== Dead Letter Topics ==========

    /**
     * Dead letter queues for failed message processing
     */
    public static final String TOPIC_DLQ_USER = "dlq.user.v1";
    public static final String TOPIC_DLQ_ORDER = "dlq.order.v1";
    public static final String TOPIC_DLQ_PAYMENT = "dlq.payment.v1";
    public static final String TOPIC_DLQ_NOTIFICATION = "dlq.notification.v1";

    // ========== Consumer Groups ==========

    /**
     * Consumer group IDs
     */
    public static final String GROUP_USER_SERVICE = "user-service-group";
    public static final String GROUP_ORDER_SERVICE = "order-service-group";
    public static final String GROUP_PAYMENT_SERVICE = "payment-service-group";
    public static final String GROUP_NOTIFICATION_SERVICE = "notification-service-group";
    public static final String GROUP_ANALYTICS_SERVICE = "analytics-service-group";
    public static final String GROUP_AUDIT_SERVICE = "audit-service-group";
    public static final String GROUP_EMAIL_SERVICE = "email-service-group";
    public static final String GROUP_SMS_SERVICE = "sms-service-group";

    // ========== Partition Configuration ==========

    /**
     * Default partition counts for topics
     */
    public static final int PARTITIONS_DEFAULT = 3;
    public static final int PARTITIONS_HIGH_VOLUME = 10;
    public static final int PARTITIONS_LOW_VOLUME = 1;

    /**
     * Specific topic partitions
     */
    public static final int PARTITIONS_USER_EVENTS = 5;
    public static final int PARTITIONS_ORDER_EVENTS = 10;
    public static final int PARTITIONS_NOTIFICATION_EVENTS = 5;
    public static final int PARTITIONS_ANALYTICS_EVENTS = 10;
    public static final int PARTITIONS_AUDIT_EVENTS = 3;

    // ========== Replication Configuration ==========

    /**
     * Replication factors
     */
    public static final short REPLICATION_FACTOR_DEFAULT = 3;
    public static final short REPLICATION_FACTOR_CRITICAL = 3;
    public static final short REPLICATION_FACTOR_NON_CRITICAL = 2;

    // ========== Retention Configuration ==========

    /**
     * Message retention periods (milliseconds)
     */
    public static final long RETENTION_ONE_HOUR = 3600000L;
    public static final long RETENTION_ONE_DAY = 86400000L;
    public static final long RETENTION_ONE_WEEK = 604800000L;
    public static final long RETENTION_ONE_MONTH = 2592000000L;
    public static final long RETENTION_THREE_MONTHS = 7776000000L;
    public static final long RETENTION_ONE_YEAR = 31536000000L;

    /**
     * Specific retention periods
     */
    public static final long RETENTION_USER_EVENTS = RETENTION_ONE_MONTH;
    public static final long RETENTION_ORDER_EVENTS = RETENTION_ONE_YEAR;
    public static final long RETENTION_PAYMENT_EVENTS = RETENTION_ONE_YEAR;
    public static final long RETENTION_NOTIFICATION_EVENTS = RETENTION_ONE_WEEK;
    public static final long RETENTION_ANALYTICS_EVENTS = RETENTION_THREE_MONTHS;
    public static final long RETENTION_AUDIT_EVENTS = RETENTION_ONE_YEAR;

    // ========== Producer Configuration ==========

    /**
     * Producer acknowledgment modes
     */
    public static final String ACKS_ALL = "all";
    public static final String ACKS_LEADER = "1";
    public static final String ACKS_NONE = "0";

    /**
     * Compression types
     */
    public static final String COMPRESSION_NONE = "none";
    public static final String COMPRESSION_GZIP = "gzip";
    public static final String COMPRESSION_SNAPPY = "snappy";
    public static final String COMPRESSION_LZ4 = "lz4";
    public static final String COMPRESSION_ZSTD = "zstd";

    /**
     * Default compression
     */
    public static final String DEFAULT_COMPRESSION = COMPRESSION_SNAPPY;

    // ========== Consumer Configuration ==========

    /**
     * Auto offset reset strategies
     */
    public static final String OFFSET_RESET_EARLIEST = "earliest";
    public static final String OFFSET_RESET_LATEST = "latest";
    public static final String OFFSET_RESET_NONE = "none";

    /**
     * Enable auto commit
     */
    public static final boolean ENABLE_AUTO_COMMIT = false;

    /**
     * Max poll records
     */
    public static final int MAX_POLL_RECORDS_DEFAULT = 500;
    public static final int MAX_POLL_RECORDS_BATCH = 1000;
    public static final int MAX_POLL_RECORDS_SINGLE = 1;

    // ========== Error Handling ==========

    /**
     * Retry configuration
     */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final long RETRY_BACKOFF_MS = 1000L;
    public static final long RETRY_MAX_BACKOFF_MS = 10000L;

    /**
     * DLQ configuration
     */
    public static final boolean ENABLE_DLQ = true;
    public static final int DLQ_MAX_ATTEMPTS = 3;

    // ========== Message Headers ==========

    /**
     * Standard message headers
     */
    public static final String HEADER_MESSAGE_ID = "messageId";
    public static final String HEADER_CORRELATION_ID = "correlationId";
    public static final String HEADER_TIMESTAMP = "timestamp";
    public static final String HEADER_SOURCE = "source";
    public static final String HEADER_VERSION = "version";
    public static final String HEADER_EVENT_TYPE = "eventType";
    public static final String HEADER_USER_ID = "userId";
    public static final String HEADER_TENANT_ID = "tenantId";
    public static final String HEADER_RETRY_COUNT = "retryCount";

    // ========== Helper Methods ==========

    /**
     * Get DLQ topic name for a given topic
     */
    public static String getDlqTopic(String originalTopic) {
        return "dlq." + originalTopic;
    }

    /**
     * Get retry topic name
     */
    public static String getRetryTopic(String originalTopic, int retryNumber) {
        return originalTopic + ".retry." + retryNumber;
    }

    /**
     * Check if topic is a DLQ topic
     */
    public static boolean isDlqTopic(String topic) {
        return topic != null && topic.startsWith("dlq.");
    }
}