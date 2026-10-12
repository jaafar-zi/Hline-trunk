package com.example.demo.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for JSON operations using Jackson
 *
 * Features:
 * - Object to JSON conversion
 * - JSON to Object conversion
 * - Pretty printing
 * - File I/O operations
 * - JSON validation
 * - JSON path operations
 * - Merging and manipulation
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class JsonUtils {

    // ========== ObjectMapper Instances ==========

    /**
     * Default ObjectMapper instance
     */
    private static final ObjectMapper OBJECT_MAPPER = createDefaultObjectMapper();

    /**
     * Pretty print ObjectMapper instance
     */
    private static final ObjectMapper PRETTY_MAPPER = createPrettyObjectMapper();

    /**
     * Lenient ObjectMapper instance (ignores unknown properties)
     */
    private static final ObjectMapper LENIENT_MAPPER = createLenientObjectMapper();

    // ========== ObjectMapper Configuration ==========

    /**
     * Create default ObjectMapper with standard configuration
     */
    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 date/time module
        mapper.registerModule(new JavaTimeModule());

        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Fail on unknown properties during deserialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Fail on empty beans
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // Include non-null values only
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Set default date format
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        // Configure for better null handling
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return mapper;
    }

    /**
     * Create pretty print ObjectMapper
     */
    private static ObjectMapper createPrettyObjectMapper() {
        ObjectMapper mapper = createDefaultObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }

    /**
     * Create lenient ObjectMapper (more forgiving)
     */
    private static ObjectMapper createLenientObjectMapper() {
        ObjectMapper mapper = createDefaultObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
        return mapper;
    }

    /**
     * Get default ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Get pretty print ObjectMapper
     */
    public static ObjectMapper getPrettyMapper() {
        return PRETTY_MAPPER;
    }

    /**
     * Get lenient ObjectMapper
     */
    public static ObjectMapper getLenientMapper() {
        return LENIENT_MAPPER;
    }

    // ========== Object to JSON Conversion ==========

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON: {}", obj.getClass().getName(), e);
            throw new JsonException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Convert object to pretty JSON string
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return PRETTY_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to pretty JSON: {}", obj.getClass().getName(), e);
            throw new JsonException("Failed to serialize object to pretty JSON", e);
        }
    }

    /**
     * Convert object to JSON bytes
     */
    public static byte[] toJsonBytes(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON bytes: {}", obj.getClass().getName(), e);
            throw new JsonException("Failed to serialize object to JSON bytes", e);
        }
    }

    /**
     * Convert object to JSON with custom mapper
     */
    public static String toJson(Object obj, ObjectMapper mapper) {
        if (obj == null) {
            return null;
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON with custom mapper", e);
            throw new JsonException("Failed to serialize object to JSON", e);
        }
    }

    // ========== JSON to Object Conversion ==========

    /**
     * Parse JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to {}: {}", clazz.getName(), json, e);
            throw new JsonException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * Parse JSON string to object with TypeReference
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON with TypeReference: {}", json, e);
            throw new JsonException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * Parse JSON bytes to object
     */
    public static <T> T fromJson(byte[] json, Class<T> clazz) {
        if (json == null || json.length == 0) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Failed to parse JSON bytes to {}", clazz.getName(), e);
            throw new JsonException("Failed to deserialize JSON bytes to object", e);
        }
    }

    /**
     * Parse JSON with custom mapper
     */
    public static <T> T fromJson(String json, Class<T> clazz, ObjectMapper mapper) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON with custom mapper", e);
            throw new JsonException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * Parse JSON to List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }

        try {
            CollectionType listType = OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, elementClass);
            return OBJECT_MAPPER.readValue(json, listType);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to List<{}>: {}", elementClass.getName(), json, e);
            throw new JsonException("Failed to deserialize JSON to List", e);
        }
    }

    /**
     * Parse JSON to Set
     */
    public static <T> Set<T> fromJsonToSet(String json, Class<T> elementClass) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptySet();
        }

        try {
            CollectionType setType = OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(Set.class, elementClass);
            return OBJECT_MAPPER.readValue(json, setType);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to Set<{}>: {}", elementClass.getName(), json, e);
            throw new JsonException("Failed to deserialize JSON to Set", e);
        }
    }

    /**
     * Parse JSON to Map
     */
    public static <K, V> Map<K, V> fromJsonToMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyMap();
        }

        try {
            MapType mapType = OBJECT_MAPPER.getTypeFactory()
                    .constructMapType(Map.class, keyClass, valueClass);
            return OBJECT_MAPPER.readValue(json, mapType);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to Map<{}, {}>: {}",
                    keyClass.getName(), valueClass.getName(), json, e);
            throw new JsonException("Failed to deserialize JSON to Map", e);
        }
    }

    /**
     * Parse JSON to Map with String keys
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        return fromJsonToMap(json, String.class, Object.class);
    }

    // ========== Safe Parsing (returns null on error) ==========

    /**
     * Safely parse JSON (returns null on error)
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return fromJson(json, clazz);
        } catch (Exception e) {
            log.warn("Failed to parse JSON safely, returning null", e);
            return null;
        }
    }

    /**
     * Safely parse JSON to List (returns empty list on error)
     */
    public static <T> List<T> fromJsonToListSafe(String json, Class<T> elementClass) {
        try {
            return fromJsonToList(json, elementClass);
        } catch (Exception e) {
            log.warn("Failed to parse JSON to List safely, returning empty list", e);
            return Collections.emptyList();
        }
    }

    /**
     * Safely parse JSON to Map (returns empty map on error)
     */
    public static <K, V> Map<K, V> fromJsonToMapSafe(String json, Class<K> keyClass, Class<V> valueClass) {
        try {
            return fromJsonToMap(json, keyClass, valueClass);
        } catch (Exception e) {
            log.warn("Failed to parse JSON to Map safely, returning empty map", e);
            return Collections.emptyMap();
        }
    }

    // ========== File I/O Operations ==========

    /**
     * Write object to JSON file
     */
    public static void toJsonFile(Object obj, File file) {
        if (obj == null || file == null) {
            throw new IllegalArgumentException("Object and file must not be null");
        }

        try {
            OBJECT_MAPPER.writeValue(file, obj);
            log.debug("Successfully wrote JSON to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write JSON to file: {}", file.getAbsolutePath(), e);
            throw new JsonException("Failed to write JSON to file", e);
        }
    }

    /**
     * Write object to JSON file (pretty format)
     */
    public static void toPrettyJsonFile(Object obj, File file) {
        if (obj == null || file == null) {
            throw new IllegalArgumentException("Object and file must not be null");
        }

        try {
            PRETTY_MAPPER.writeValue(file, obj);
            log.debug("Successfully wrote pretty JSON to file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write pretty JSON to file: {}", file.getAbsolutePath(), e);
            throw new JsonException("Failed to write pretty JSON to file", e);
        }
    }

    /**
     * Write object to JSON file using Path
     */
    public static void toJsonFile(Object obj, Path path) {
        toJsonFile(obj, path.toFile());
    }

    /**
     * Read object from JSON file
     */
    public static <T> T fromJsonFile(File file, Class<T> clazz) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must exist");
        }

        try {
            T result = OBJECT_MAPPER.readValue(file, clazz);
            log.debug("Successfully read JSON from file: {}", file.getAbsolutePath());
            return result;
        } catch (IOException e) {
            log.error("Failed to read JSON from file: {}", file.getAbsolutePath(), e);
            throw new JsonException("Failed to read JSON from file", e);
        }
    }

    /**
     * Read object from JSON file using Path
     */
    public static <T> T fromJsonFile(Path path, Class<T> clazz) {
        return fromJsonFile(path.toFile(), clazz);
    }

    /**
     * Read object from JSON file with TypeReference
     */
    public static <T> T fromJsonFile(File file, TypeReference<T> typeReference) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must exist");
        }

        try {
            return OBJECT_MAPPER.readValue(file, typeReference);
        } catch (IOException e) {
            log.error("Failed to read JSON from file: {}", file.getAbsolutePath(), e);
            throw new JsonException("Failed to read JSON from file", e);
        }
    }

    /**
     * Read JSON from InputStream
     */
    public static <T> T fromJsonStream(InputStream inputStream, Class<T> clazz) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream must not be null");
        }

        try {
            return OBJECT_MAPPER.readValue(inputStream, clazz);
        } catch (IOException e) {
            log.error("Failed to read JSON from InputStream", e);
            throw new JsonException("Failed to read JSON from InputStream", e);
        }
    }

    /**
     * Read JSON from Reader
     */
    public static <T> T fromJsonReader(Reader reader, Class<T> clazz) {
        if (reader == null) {
            throw new IllegalArgumentException("Reader must not be null");
        }

        try {
            return OBJECT_MAPPER.readValue(reader, clazz);
        } catch (IOException e) {
            log.error("Failed to read JSON from Reader", e);
            throw new JsonException("Failed to read JSON from Reader", e);
        }
    }

    // ========== JSON Validation ==========

    /**
     * Check if string is valid JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            log.debug("Invalid JSON: {}", json, e);
            return false;
        }
    }

    /**
     * Check if string is valid JSON object
     */
    public static boolean isValidJsonObject(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node.isObject();
        } catch (JsonProcessingException e) {
            log.debug("Invalid JSON object: {}", json, e);
            return false;
        }
    }

    /**
     * Check if string is valid JSON array
     */
    public static boolean isValidJsonArray(String json) {
        if (StringUtils.isBlank(json)) {
            return false;
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            return node.isArray();
        } catch (JsonProcessingException e) {
            log.debug("Invalid JSON array: {}", json, e);
            return false;
        }
    }

    /**
     * Validate and get error message
     */
    public static String validateJson(String json) {
        try {
            OBJECT_MAPPER.readTree(json);
            return null; // Valid JSON
        } catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }

    // ========== Object Conversion ==========

    /**
     * Convert object to another type
     */
    public static <T> T convertValue(Object obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.convertValue(obj, clazz);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert object to {}", clazz.getName(), e);
            throw new JsonException("Failed to convert object", e);
        }
    }

    /**
     * Convert object to another type with TypeReference
     */
    public static <T> T convertValue(Object obj, TypeReference<T> typeReference) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.convertValue(obj, typeReference);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert object with TypeReference", e);
            throw new JsonException("Failed to convert object", e);
        }
    }

    /**
     * Convert object to Map
     */
    public static Map<String, Object> toMap(Object obj) {
        return convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Convert Map to object
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return convertValue(map, clazz);
    }

    // ========== JsonNode Operations ==========

    /**
     * Parse JSON string to JsonNode
     */
    public static JsonNode parseToNode(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to JsonNode: {}", json, e);
            throw new JsonException("Failed to parse JSON to JsonNode", e);
        }
    }

    /**
     * Convert object to JsonNode
     */
    public static JsonNode toNode(Object obj) {
        if (obj == null) {
            return null;
        }

        return OBJECT_MAPPER.valueToTree(obj);
    }

    /**
     * Create empty ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * Create empty ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * Get value from JsonNode by path
     */
    public static JsonNode getNodeByPath(JsonNode node, String path) {
        if (node == null || StringUtils.isBlank(path)) {
            return null;
        }

        String[] parts = path.split("\\.");
        JsonNode current = node;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            // Handle array index (e.g., items[0])
            if (part.contains("[") && part.contains("]")) {
                String fieldName = part.substring(0, part.indexOf('['));
                int index = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.indexOf(']')));

                current = current.get(fieldName);
                if (current != null && current.isArray()) {
                    current = current.get(index);
                }
            } else {
                current = current.get(part);
            }
        }

        return current;
    }

    /**
     * Get string value from JsonNode by path
     */
    public static String getStringByPath(JsonNode node, String path) {
        JsonNode result = getNodeByPath(node, path);
        return result != null && !result.isNull() ? result.asText() : null;
    }

    /**
     * Get integer value from JsonNode by path
     */
    public static Integer getIntByPath(JsonNode node, String path) {
        JsonNode result = getNodeByPath(node, path);
        return result != null && !result.isNull() ? result.asInt() : null;
    }

    /**
     * Get long value from JsonNode by path
     */
    public static Long getLongByPath(JsonNode node, String path) {
        JsonNode result = getNodeByPath(node, path);
        return result != null && !result.isNull() ? result.asLong() : null;
    }

    /**
     * Get boolean value from JsonNode by path
     */
    public static Boolean getBooleanByPath(JsonNode node, String path) {
        JsonNode result = getNodeByPath(node, path);
        return result != null && !result.isNull() ? result.asBoolean() : null;
    }

    // ========== JSON Merging ==========

    /**
     * Merge two JSON objects (second overwrites first)
     */
    public static String mergeJson(String json1, String json2) {
        if (StringUtils.isBlank(json1)) {
            return json2;
        }
        if (StringUtils.isBlank(json2)) {
            return json1;
        }

        try {
            JsonNode node1 = OBJECT_MAPPER.readTree(json1);
            JsonNode node2 = OBJECT_MAPPER.readTree(json2);
            JsonNode merged = merge(node1, node2);
            return OBJECT_MAPPER.writeValueAsString(merged);
        } catch (JsonProcessingException e) {
            log.error("Failed to merge JSON objects", e);
            throw new JsonException("Failed to merge JSON", e);
        }
    }

    /**
     * Merge two JsonNodes (recursive)
     */
    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
        if (mainNode == null) {
            return updateNode;
        }
        if (updateNode == null) {
            return mainNode;
        }

        if (!mainNode.isObject()) {
            return updateNode;
        }

        ObjectNode merged = (ObjectNode) mainNode.deepCopy();

        updateNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode updateValue = entry.getValue();

            if (merged.has(fieldName)) {
                JsonNode mainValue = merged.get(fieldName);
                if (mainValue.isObject() && updateValue.isObject()) {
                    merged.set(fieldName, merge(mainValue, updateValue));
                } else {
                    merged.set(fieldName, updateValue);
                }
            } else {
                merged.set(fieldName, updateValue);
            }
        });

        return merged;
    }

    // ========== JSON Comparison ==========

    /**
     * Compare two JSON strings for equality
     */
    public static boolean jsonEquals(String json1, String json2) {
        if (Objects.equals(json1, json2)) {
            return true;
        }
        if (json1 == null || json2 == null) {
            return false;
        }

        try {
            JsonNode node1 = OBJECT_MAPPER.readTree(json1);
            JsonNode node2 = OBJECT_MAPPER.readTree(json2);
            return node1.equals(node2);
        } catch (JsonProcessingException e) {
            log.error("Failed to compare JSON strings", e);
            return false;
        }
    }

    /**
     * Deep compare two objects as JSON
     */
    public static boolean deepEquals(Object obj1, Object obj2) {
        if (Objects.equals(obj1, obj2)) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }

        JsonNode node1 = toNode(obj1);
        JsonNode node2 = toNode(obj2);
        return node1.equals(node2);
    }

    // ========== JSON Manipulation ==========

    /**
     * Pretty print JSON string
     */
    public static String prettify(String json) {
        if (StringUtils.isBlank(json)) {
            return json;
        }

        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return PRETTY_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to prettify JSON: {}", json, e);
            throw new JsonException("Failed to prettify JSON", e);
        }
    }

    /**
     * Minify JSON string (remove whitespace)
     */
    public static String minify(String json) {
        if (StringUtils.isBlank(json)) {
            return json;
        }

        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to minify JSON: {}", json, e);
            throw new JsonException("Failed to minify JSON", e);
        }
    }

    /**
     * Remove null values from JSON
     */
    public static String removeNulls(String json) {
        if (StringUtils.isBlank(json)) {
            return json;
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            removeNullNodes(node);
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("Failed to remove nulls from JSON", e);
            throw new JsonException("Failed to remove nulls from JSON", e);
        }
    }

    /**
     * Recursively remove null nodes
     */
    private static void removeNullNodes(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldsToRemove = new ArrayList<>();

            objectNode.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isNull()) {
                    fieldsToRemove.add(entry.getKey());
                } else {
                    removeNullNodes(value);
                }
            });

            fieldsToRemove.forEach(objectNode::remove);
        } else if (node.isArray()) {
            node.forEach(JsonUtils::removeNullNodes);
        }
    }

    /**
     * Clone object via JSON serialization
     */
    public static <T> T clone(T obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to clone object via JSON", e);
            throw new JsonException("Failed to clone object", e);
        }
    }

    /**
     * Deep clone using JSON (preserves type)
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T obj, TypeReference<T> typeReference) {
        if (obj == null) {
            return null;
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Failed to deep clone object via JSON", e);
            throw new JsonException("Failed to deep clone object", e);
        }
    }

    // ========== Utility Methods ==========

    /**
     * Get JSON size in bytes
     */
    public static int getJsonSize(Object obj) {
        if (obj == null) {
            return 0;
        }

        try {
            return OBJECT_MAPPER.writeValueAsBytes(obj).length;
        } catch (JsonProcessingException e) {
            log.error("Failed to calculate JSON size", e);
            return -1;
        }
    }

    /**
     * Get JSON size in human-readable format
     */
    public static String getJsonSizeFormatted(Object obj) {
        int bytes = getJsonSize(obj);
        return bytes >= 0 ? StringUtils.formatBytes(bytes) : "Unknown";
    }

    /**
     * Extract field names from JSON
     */
    public static Set<String> getFieldNames(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptySet();
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            if (!node.isObject()) {
                return Collections.emptySet();
            }

            Set<String> fieldNames = new HashSet<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            return fieldNames;
        } catch (JsonProcessingException e) {
            log.error("Failed to extract field names from JSON", e);
            return Collections.emptySet();
        }
    }

    /**
     * Get all field paths (nested)
     */
    public static Set<String> getAllFieldPaths(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptySet();
        }

        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            Set<String> paths = new HashSet<>();
            collectPaths(node, "", paths);
            return paths;
        } catch (JsonProcessingException e) {
            log.error("Failed to get field paths from JSON", e);
            return Collections.emptySet();
        }
    }

    /**
     * Recursively collect field paths
     */
    private static void collectPaths(JsonNode node, String currentPath, Set<String> paths) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String path = currentPath.isEmpty() ? entry.getKey() : currentPath + "." + entry.getKey();
                paths.add(path);
                collectPaths(entry.getValue(), path, paths);
            });
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String path = currentPath + "[" + i + "]";
                paths.add(path);
                collectPaths(node.get(i), path, paths);
            }
        }
    }

    /**
     * Convert JSON to URL query string
     */
    public static String toQueryString(Object obj) {
        if (obj == null) {
            return "";
        }

        Map<String, Object> map = toMap(obj);
        return map.entrySet().stream()
                .map(entry -> StringUtils.encodeUrl(entry.getKey()) + "=" +
                        StringUtils.encodeUrl(String.valueOf(entry.getValue())))
                .collect(java.util.stream.Collectors.joining("&"));
    }

    /**
     * Create JSON from varargs key-value pairs
     */
    public static String createJson(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0 || keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide even number of arguments (key-value pairs)");
        }

        ObjectNode node = createObjectNode();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = String.valueOf(keyValues[i]);
            Object value = keyValues[i + 1];

            if (value == null) {
                node.putNull(key);
            } else if (value instanceof String) {
                node.put(key, (String) value);
            } else if (value instanceof Integer) {
                node.put(key, (Integer) value);
            } else if (value instanceof Long) {
                node.put(key, (Long) value);
            } else if (value instanceof Double) {
                node.put(key, (Double) value);
            } else if (value instanceof Boolean) {
                node.put(key, (Boolean) value);
            } else {
                node.putPOJO(key, value);
            }
        }

        return toJson(node);
    }

    /**
     * Escape JSON string
     */
    public static String escape(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Unescape JSON string
     */
    public static String unescape(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        return str.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    // ========== Custom Exception ==========

    /**
     * Custom JSON processing exception
     */
    public static class JsonException extends RuntimeException {
        public JsonException(String message) {
            super(message);
        }

        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}