package com.example.demo.common.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
/**
 * Custom Health Indicator
 *
 * Features:
 * - Database health check
 * - Redis health check
 * - Cache health check
 * - Kafka health check
 * - Disk space check
 * - Memory usage check
 * - Custom service checks
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component("customHealth")
public class CustomHealthIndicator implements HealthIndicator {

    private final DataSource masterDataSource;
    private final DataSource slaveDataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final CacheManager cacheManager;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    private static final long MAX_RESPONSE_TIME_MS = 1000;
    private static final long MEMORY_THRESHOLD_PERCENTAGE = 90;
    private static final long KAFKA_TIMEOUT_SECONDS = 5;

    public CustomHealthIndicator(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource,
            RedisConnectionFactory redisConnectionFactory,
            CacheManager cacheManager) {
        this.masterDataSource = masterDataSource;
        this.slaveDataSource = slaveDataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.cacheManager = cacheManager;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new LinkedHashMap<>();
        boolean isHealthy = true;
        List<String> issues = new ArrayList<>();

        // ── Master Database ───────────────────────────────────────────
        HealthCheckResult masterHealth = checkDatabase(masterDataSource, "master");
        details.put("database-master", masterHealth.getDetails());
        if (!masterHealth.isHealthy()) {
            isHealthy = false;
            issues.add("Master DB: " + masterHealth.getMessage());
        }

        // ── Slave Database ────────────────────────────────────────────
        HealthCheckResult slaveHealth = checkDatabase(slaveDataSource, "slave");
        details.put("database-slave", slaveHealth.getDetails());
        if (!slaveHealth.isHealthy()) {
            // Slave being down is degraded, not fatal
            issues.add("Slave DB: " + slaveHealth.getMessage());
        }

        // ── Replication Sync Check ────────────────────────────────────
        HealthCheckResult syncHealth = checkReplicationSync();
        details.put("replication-sync", syncHealth.getDetails());
        if (!syncHealth.isHealthy()) {
            issues.add("Replication: " + syncHealth.getMessage());
        }

        // ── Redis ─────────────────────────────────────────────────────
        HealthCheckResult redisHealth = checkRedis();
        details.put("redis", redisHealth.getDetails());
        if (!redisHealth.isHealthy()) {
            isHealthy = false;
            issues.add("Redis: " + redisHealth.getMessage());
        }

        // ── Cache ─────────────────────────────────────────────────────
        HealthCheckResult cacheHealth = checkCache();
        details.put("cache", cacheHealth.getDetails());
        if (!cacheHealth.isHealthy()) {
            issues.add("Cache: " + cacheHealth.getMessage());
        }

        // ── Kafka ─────────────────────────────────────────────────────
        HealthCheckResult kafkaHealth = checkKafka();
        details.put("kafka", kafkaHealth.getDetails());
        if (!kafkaHealth.isHealthy()) {
            isHealthy = false;
            issues.add("Kafka: " + kafkaHealth.getMessage());
        }

        // ── Memory ────────────────────────────────────────────────────
        HealthCheckResult memoryHealth = checkMemory();
        details.put("memory", memoryHealth.getDetails());
        if (!memoryHealth.isHealthy()) {
            issues.add("Memory: " + memoryHealth.getMessage());
        }

        // ── Disk ──────────────────────────────────────────────────────
        HealthCheckResult diskHealth = checkDiskSpace();
        details.put("disk", diskHealth.getDetails());
        if (!diskHealth.isHealthy()) {
            issues.add("Disk: " + diskHealth.getMessage());
        }

        // ── Summary ───────────────────────────────────────────────────
        details.put("timestamp", Instant.now().toString());
        details.put("status", isHealthy ? "UP" : "DOWN");

        if (!issues.isEmpty()) {
            details.put("issues", issues);
        }

        return isHealthy
                ? Health.up().withDetails(details).build()
                : Health.down().withDetails(details).build();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Database Checks
    // ═══════════════════════════════════════════════════════════════════

    private HealthCheckResult checkDatabase(DataSource dataSource, String name) {
        Map<String, Object> details = new LinkedHashMap<>();

        try {
            long startTime = System.currentTimeMillis();

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.execute("SELECT 1");

            long responseTime = System.currentTimeMillis() - startTime;

            details.put("name", name);
            details.put("status", "UP");
            details.put("responseTimeMs", responseTime);

            if (responseTime > MAX_RESPONSE_TIME_MS) {
                details.put("status", "DEGRADED");
                return HealthCheckResult.degraded(
                        name + " response time is slow: " + responseTime + "ms",
                        details);
            }

            return HealthCheckResult.healthy(name + " is healthy", details);

        } catch (Exception e) {
            log.error("{} database health check failed", name, e);
            details.put("name", name);
            details.put("status", "DOWN");
            details.put("error", e.getMessage());
            return HealthCheckResult.unhealthy(
                    name + " connection failed: " + e.getMessage(), details);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Replication Sync Check
    // ═══════════════════════════════════════════════════════════════════

    private HealthCheckResult checkReplicationSync() {
        Map<String, Object> details = new LinkedHashMap<>();

        try {
            JdbcTemplate masterJdbc = new JdbcTemplate(masterDataSource);
            JdbcTemplate slaveJdbc = new JdbcTemplate(slaveDataSource);

            // Count a well-known table on both databases
            // Use a table that exists in all environments
            Long masterCount = masterJdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'public'",
                    Long.class);

            Long slaveCount = slaveJdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'public'",
                    Long.class);

            details.put("masterTableCount", masterCount);
            details.put("slaveTableCount", slaveCount);
            details.put("schemasInSync", Objects.equals(masterCount, slaveCount));

            if (!Objects.equals(masterCount, slaveCount)) {
                details.put("status", "DEGRADED");
                return HealthCheckResult.degraded(
                        "Schema mismatch: master=" + masterCount +
                        " slave=" + slaveCount,
                        details);
            }

            details.put("status", "UP");
            return HealthCheckResult.healthy("Replication in sync", details);

        } catch (Exception e) {
            log.error("Replication sync check failed", e);
            details.put("status", "DEGRADED");
            details.put("error", e.getMessage());
            return HealthCheckResult.degraded(
                    "Replication check failed: " + e.getMessage(), details);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Redis Check
    // ═══════════════════════════════════════════════════════════════════

    private HealthCheckResult checkRedis() {
        Map<String, Object> details = new LinkedHashMap<>();

        try {
            long startTime = System.currentTimeMillis();

            RedisConnection connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();

            // Get Redis info
            Properties redisInfo = connection.serverCommands().info("memory");
            connection.close();

            long responseTime = System.currentTimeMillis() - startTime;

            details.put("status", "UP");
            details.put("responseTimeMs", responseTime);
            details.put("ping", pong);

            if (redisInfo != null) {
                details.put("usedMemory",
                        redisInfo.getProperty("used_memory_human"));
                details.put("maxMemory",
                        redisInfo.getProperty("maxmemory_human"));
                details.put("connectedClients",
                        redisInfo.getProperty("connected_clients"));
            }

            if (responseTime > MAX_RESPONSE_TIME_MS) {
                details.put("status", "DEGRADED");
                return HealthCheckResult.degraded(
                        "Redis response time is slow: " + responseTime + "ms",
                        details);
            }

            return HealthCheckResult.healthy("Redis is healthy", details);

        } catch (Exception e) {
            log.error("Redis health check failed", e);
            details.put("status", "DOWN");
            details.put("error", e.getMessage());
            return HealthCheckResult.unhealthy(
                    "Redis connection failed", details);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Cache Check
    // ═══════════════════════════════════════════════════════════════════

    private HealthCheckResult checkCache() {
        Map<String, Object> details = new LinkedHashMap<>();

        try {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            details.put("cacheNames", cacheNames);
            details.put("cacheCount", cacheNames.size());
            details.put("status", "UP");

            return HealthCheckResult.healthy("Cache is healthy", details);

        } catch (Exception e) {
            log.error("Cache health check failed", e);
            details.put("status", "DOWN");
            details.put("error", e.getMessage());
            return HealthCheckResult.unhealthy("Cache check failed", details);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Kafka Check (Real Check)
    // ═══════════════════════════════════════════════════════════════════

    private HealthCheckResult checkKafka() {
        Map<String, Object> details = new LinkedHashMap<>();

        try (AdminClient adminClient = AdminClient.create(
                Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                        kafkaBootstrapServers))) {

            long startTime = System.currentTimeMillis();

            // Check cluster
            DescribeClusterResult cluster = adminClient.describeCluster();
            String clusterId = cluster.clusterId()
                    .get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            int nodeCount = cluster.nodes()
                    .get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS).size();

            // List topics
            Set<String> topics = adminClient.listTopics()
                    .names()
                    .get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Check consumer group lag
            Map<String, Object> syncGroupInfo =
                    checkConsumerGroupLag(adminClient, "replica-sync-group");

            long responseTime = System.currentTimeMillis() - startTime;

            details.put("status", "UP");
            details.put("responseTimeMs", responseTime);
            details.put("clusterId", clusterId);
            details.put("nodeCount", nodeCount);
            details.put("topicCount", topics.size());
            details.put("topics", topics);
            details.put("bootstrapServers", kafkaBootstrapServers);
            details.put("replicaSyncGroup", syncGroupInfo);

            // Check if db-sync-events topic exists
            boolean syncTopicExists = topics.contains("db-sync-events");
            details.put("syncTopicExists", syncTopicExists);

            if (!syncTopicExists) {
                return HealthCheckResult.degraded(
                        "db-sync-events topic does not exist", details);
            }

            if (responseTime > MAX_RESPONSE_TIME_MS) {
                details.put("status", "DEGRADED");
                return HealthCheckResult.degraded(
                        "Kafka response time is slow: " + responseTime + "ms",
                        details);
            }

            return HealthCheckResult.healthy("Kafka is healthy", details);

        } catch (Exception e) {
            log.error("Kafka health check failed", e);
            details.put("status", "DOWN");
            details.put("error", e.getMessage());
            details.put("bootstrapServers", kafkaBootstrapServers);
            return HealthCheckResult.unhealthy(
                    "Kafka connection failed: " + e.getMessage(), details);
        }
    }

    /**
     * Check consumer group lag for replica sync.
     * High lag means replica is falling behind.
     */
    private Map<String, Object> checkConsumerGroupLag(
            AdminClient adminClient, String groupId) {

        Map<String, Object> groupInfo = new LinkedHashMap<>();

        try {
            var offsets = adminClient
                    .listConsumerGroupOffsets(groupId)
                    .partitionsToOffsetAndMetadata()
                    .get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            long totalLag = 0;
            Map<String, Long> partitionLags = new LinkedHashMap<>();

            for (var entry : offsets.entrySet()) {
                String partition = entry.getKey().topic() +
                                   "-" + entry.getKey().partition();
                long committedOffset = entry.getValue().offset();

                // Get end offset for comparison
                var endOffsets = adminClient
                        .listOffsets(Map.of(
                                entry.getKey(),
                                org.apache.kafka.clients.admin
                                        .OffsetSpec.latest()))
                        .all()
                        .get(KAFKA_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                long endOffset = endOffsets.get(entry.getKey()).offset();
                long lag = endOffset - committedOffset;

                partitionLags.put(partition, lag);
                totalLag += lag;
            }

            groupInfo.put("status", totalLag == 0 ? "CAUGHT_UP" : "LAGGING");
            groupInfo.put("totalLag", totalLag);
            groupInfo.put("partitionLags", partitionLags);
            groupInfo.put("healthy", totalLag < 1000); // threshold

        } catch (Exception e) {
            groupInfo.put("status", "UNKNOWN");
            groupInfo.put("error", e.getMessage());
        }

        return groupInfo;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Memory Check
    // ═══════════════════════════════════════════════════════════════════

    private HealthCheckResult checkMemory() {
        Map<String, Object> details = new LinkedHashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long usedPercentage = (usedMemory * 100) / maxMemory;

            details.put("max", formatBytes(maxMemory));
            details.put("total", formatBytes(totalMemory));
            details.put("used", formatBytes(usedMemory));
            details.put("free", formatBytes(freeMemory));
            details.put("usedPercentage", usedPercentage + "%");

            if (usedPercentage >= MEMORY_THRESHOLD_PERCENTAGE) {
                details.put("status", "WARNING");
                return HealthCheckResult.degraded(
                        "Memory usage is high: " + usedPercentage + "%",
                        details);
            }

            details.put("status", "UP");
            return HealthCheckResult.healthy("Memory usage is normal", details);

        } catch (Exception e) {
            log.error("Memory check failed", e);
            details.put("status", "ERROR");
            details.put("error", e.getMessage());
            return HealthCheckResult.unhealthy("Memory check failed", details);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Disk Check
    // ═══════════════════════════════════════════════════════════════════

    private HealthCheckResult checkDiskSpace() {
        Map<String, Object> details = new LinkedHashMap<>();

        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            long usedPercentage = (usedSpace * 100) / totalSpace;

            details.put("total", formatBytes(totalSpace));
            details.put("free", formatBytes(freeSpace));
            details.put("used", formatBytes(usedSpace));
            details.put("usedPercentage", usedPercentage + "%");

            if (usedPercentage >= 90) {
                details.put("status", "WARNING");
                return HealthCheckResult.degraded(
                        "Disk space is low: " + usedPercentage + "% used",
                        details);
            }

            details.put("status", "UP");
            return HealthCheckResult.healthy("Disk space is adequate", details);

        } catch (Exception e) {
            log.error("Disk space check failed", e);
            details.put("status", "ERROR");
            details.put("error", e.getMessage());
            return HealthCheckResult.unhealthy(
                    "Disk space check failed", details);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class HealthCheckResult {
        private boolean healthy;
        private String message;
        private Map<String, Object> details;

        public static HealthCheckResult healthy(
                String message, Map<String, Object> details) {
            return new HealthCheckResult(true, message, details);
        }

        public static HealthCheckResult unhealthy(
                String message, Map<String, Object> details) {
            return new HealthCheckResult(false, message, details);
        }

        public static HealthCheckResult degraded(
                String message, Map<String, Object> details) {
            details.put("degraded", true);
            return new HealthCheckResult(true, message, details);
        }
    }
}