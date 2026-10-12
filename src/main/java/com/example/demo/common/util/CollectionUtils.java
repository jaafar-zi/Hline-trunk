package com.example.demo.common.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for Collection operations
 *
 * Features:
 * - Null-safe collection operations
 * - Collection validation and checking
 * - Collection transformation
 * - Collection manipulation
 * - Set operations (union, intersection, difference)
 * - Partitioning and grouping
 * - Finding and filtering
 * - Aggregation operations
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@UtilityClass
public class CollectionUtils {

    // ========== Null/Empty Checking ==========

    /**
     * Check if collection is null or empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Check if collection is not null and not empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Check if map is null or empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Check if map is not null and not empty
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Check if array is null or empty
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Check if array is not null and not empty
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    /**
     * Get size safely (null-safe)
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Get size safely for map (null-safe)
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    /**
     * Get length safely for array (null-safe)
     */
    public static <T> int length(T[] array) {
        return array == null ? 0 : array.length;
    }

    // ========== Default Values ==========

    /**
     * Return empty list if null
     */
    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * Return empty set if null
     */
    public static <T> Set<T> emptyIfNull(Set<T> set) {
        return set == null ? Collections.emptySet() : set;
    }

    /**
     * Return empty map if null
     */
    public static <K, V> Map<K, V> emptyIfNull(Map<K, V> map) {
        return map == null ? Collections.emptyMap() : map;
    }

    /**
     * Return default collection if null or empty
     */
    public static <T> Collection<T> defaultIfEmpty(Collection<T> collection, Collection<T> defaultCollection) {
        return isEmpty(collection) ? defaultCollection : collection;
    }

    // ========== List Creation ==========

    /**
     * Create mutable list from elements
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        if (elements == null || elements.length == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(elements));
    }

    /**
     * Create immutable list from elements
     */
    @SafeVarargs
    public static <T> List<T> immutableListOf(T... elements) {
        if (elements == null || elements.length == 0) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    /**
     * Create mutable set from elements
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        if (elements == null || elements.length == 0) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(elements));
    }

    /**
     * Create immutable set from elements
     */
    @SafeVarargs
    public static <T> Set<T> immutableSetOf(T... elements) {
        if (elements == null || elements.length == 0) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(elements)));
    }

    /**
     * Create map from key-value pairs
     */
    public static <K, V> Map<K, V> mapOf(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0 || keyValues.length % 2 != 0) {
            return new HashMap<>();
        }

        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            @SuppressWarnings("unchecked")
            K key = (K) keyValues[i];
            @SuppressWarnings("unchecked")
            V value = (V) keyValues[i + 1];
            map.put(key, value);
        }
        return map;
    }

    // ========== Collection Conversion ==========

    /**
     * Convert collection to list
     */
    public static <T> List<T> toList(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(collection);
    }

    /**
     * Convert collection to set
     */
    public static <T> Set<T> toSet(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    /**
     * Convert array to list
     */
    @SafeVarargs
    public static <T> List<T> arrayToList(T... array) {
        if (array == null || array.length == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(array));
    }

    /**
     * Convert array to set
     */
    @SafeVarargs
    public static <T> Set<T> arrayToSet(T... array) {
        if (array == null || array.length == 0) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(array));
    }

    /**
     * Convert list to array
     */
    public static <T> T[] listToArray(List<T> list, Class<T> clazz) {
        if (isEmpty(list)) {
            @SuppressWarnings("unchecked")
            T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, 0);
            return array;
        }

        @SuppressWarnings("unchecked")
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

    /**
     * Convert iterable to list
     */
    public static <T> List<T> iterableToList(Iterable<T> iterable) {
        if (iterable == null) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    // ========== Adding/Removing Elements ==========

    /**
     * Add element to list (null-safe)
     */
    public static <T> List<T> add(List<T> list, T element) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(element);
        return list;
    }

    /**
     * Add all elements to list (null-safe)
     */
    public static <T> List<T> addAll(List<T> list, Collection<T> elements) {
        if (list == null) {
            list = new ArrayList<>();
        }
        if (isNotEmpty(elements)) {
            list.addAll(elements);
        }
        return list;
    }

    /**
     * Remove element from collection (null-safe)
     */
    public static <T> boolean remove(Collection<T> collection, T element) {
        if (isEmpty(collection)) {
            return false;
        }
        return collection.remove(element);
    }

    /**
     * Remove all matching elements
     */
    public static <T> boolean removeAll(Collection<T> collection, Collection<T> elements) {
        if (isEmpty(collection) || isEmpty(elements)) {
            return false;
        }
        return collection.removeAll(elements);
    }

    /**
     * Remove elements matching predicate
     */
    public static <T> boolean removeIf(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return false;
        }
        return collection.removeIf(predicate);
    }

    /**
     * Retain only elements matching predicate
     */
    public static <T> List<T> retainAll(Collection<T> collection, Collection<T> retain) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(collection);
        if (isNotEmpty(retain)) {
            result.retainAll(retain);
        }
        return result;
    }

    // ========== Finding Elements ==========

    /**
     * Find first element matching predicate
     */
    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(predicate)
                .findFirst();
    }

    /**
     * Find any element matching predicate
     */
    public static <T> Optional<T> findAny(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(predicate)
                .findAny();
    }

    /**
     * Find all elements matching predicate
     */
    public static <T> List<T> findAll(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Check if any element matches predicate
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return false;
        }
        return collection.stream().anyMatch(predicate);
    }

    /**
     * Check if all elements match predicate
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return false;
        }
        return collection.stream().allMatch(predicate);
    }

    /**
     * Check if no elements match predicate
     */
    public static <T> boolean noneMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return true;
        }
        return collection.stream().noneMatch(predicate);
    }

    /**
     * Check if collection contains element
     */
    public static <T> boolean contains(Collection<T> collection, T element) {
        if (isEmpty(collection)) {
            return false;
        }
        return collection.contains(element);
    }

    /**
     * Check if collection contains all elements
     */
    public static <T> boolean containsAll(Collection<T> collection, Collection<T> elements) {
        if (isEmpty(collection) || isEmpty(elements)) {
            return false;
        }
        return collection.containsAll(elements);
    }

    /**
     * Check if collection contains any element
     */
    public static <T> boolean containsAny(Collection<T> collection, Collection<T> elements) {
        if (isEmpty(collection) || isEmpty(elements)) {
            return false;
        }
        return elements.stream().anyMatch(collection::contains);
    }

    // ========== Transformation ==========

    /**
     * Map collection to another type
     */
    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection) || mapper == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * FlatMap collection
     */
    public static <T, R> List<R> flatMap(Collection<T> collection,
                                         Function<T, Collection<R>> mapper) {
        if (isEmpty(collection) || mapper == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .flatMap(item -> mapper.apply(item).stream())
                .collect(Collectors.toList());
    }

    /**
     * Filter collection
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Filter and map
     */
    public static <T, R> List<R> filterAndMap(Collection<T> collection,
                                              Predicate<T> predicate,
                                              Function<T, R> mapper) {
        if (isEmpty(collection) || predicate == null || mapper == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(predicate)
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Remove nulls from collection
     */
    public static <T> List<T> removeNulls(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Remove duplicates from collection
     */
    public static <T> List<T> removeDuplicates(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(collection));
    }

    /**
     * Distinct by key extractor
     */
    public static <T> List<T> distinctByKey(Collection<T> collection, Function<T, ?> keyExtractor) {
        if (isEmpty(collection) || keyExtractor == null) {
            return new ArrayList<>();
        }

        Set<Object> seen = new HashSet<>();
        return collection.stream()
                .filter(item -> seen.add(keyExtractor.apply(item)))
                .collect(Collectors.toList());
    }

    // ========== Sorting ==========

    /**
     * Sort collection
     */
    public static <T extends Comparable<T>> List<T> sort(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        List<T> sorted = new ArrayList<>(collection);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Sort collection with comparator
     */
    public static <T> List<T> sort(Collection<T> collection, Comparator<T> comparator) {
        if (isEmpty(collection) || comparator == null) {
            return new ArrayList<>(collection);
        }
        List<T> sorted = new ArrayList<>(collection);
        sorted.sort(comparator);
        return sorted;
    }

    /**
     * Sort by key extractor
     */
    public static <T, U extends Comparable<U>> List<T> sortBy(Collection<T> collection,
                                                              Function<T, U> keyExtractor) {
        if (isEmpty(collection) || keyExtractor == null) {
            return new ArrayList<>(collection);
        }
        return collection.stream()
                .sorted(Comparator.comparing(keyExtractor))
                .collect(Collectors.toList());
    }

    /**
     * Reverse collection
     */
    public static <T> List<T> reverse(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        List<T> reversed = new ArrayList<>(collection);
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * Shuffle collection
     */
    public static <T> List<T> shuffle(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        List<T> shuffled = new ArrayList<>(collection);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    // ========== Partitioning & Grouping ==========

    /**
     * Partition collection into two lists based on predicate
     */
    public static <T> Map<Boolean, List<T>> partition(Collection<T> collection,
                                                      Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return Map.of(true, new ArrayList<>(), false, new ArrayList<>());
        }
        return collection.stream()
                .collect(Collectors.partitioningBy(predicate));
    }

    /**
     * Group by key extractor
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> collection,
                                                 Function<T, K> keyExtractor) {
        if (isEmpty(collection) || keyExtractor == null) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(keyExtractor));
    }

    /**
     * Split collection into batches
     */
    public static <T> List<List<T>> batch(Collection<T> collection, int batchSize) {
        if (isEmpty(collection) || batchSize <= 0) {
            return new ArrayList<>();
        }

        List<List<T>> batches = new ArrayList<>();
        List<T> currentBatch = new ArrayList<>();

        for (T item : collection) {
            currentBatch.add(item);
            if (currentBatch.size() == batchSize) {
                batches.add(new ArrayList<>(currentBatch));
                currentBatch.clear();
            }
        }

        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        return batches;
    }

    /**
     * Chunk collection into N parts
     */
    public static <T> List<List<T>> chunk(Collection<T> collection, int numberOfChunks) {
        if (isEmpty(collection) || numberOfChunks <= 0) {
            return new ArrayList<>();
        }

        int totalSize = collection.size();
        int chunkSize = (int) Math.ceil((double) totalSize / numberOfChunks);
        return batch(collection, chunkSize);
    }

    // ========== Set Operations ==========

    /**
     * Union of two collections
     */
    public static <T> Set<T> union(Collection<T> collection1, Collection<T> collection2) {
        Set<T> result = new HashSet<>();
        if (isNotEmpty(collection1)) {
            result.addAll(collection1);
        }
        if (isNotEmpty(collection2)) {
            result.addAll(collection2);
        }
        return result;
    }

    /**
     * Intersection of two collections
     */
    public static <T> Set<T> intersection(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1) || isEmpty(collection2)) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(collection1);
        result.retainAll(collection2);
        return result;
    }

    /**
     * Difference of two collections (elements in first but not in second)
     */
    public static <T> Set<T> difference(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1)) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(collection1);
        if (isNotEmpty(collection2)) {
            result.removeAll(collection2);
        }
        return result;
    }

    /**
     * Symmetric difference (elements in either collection but not in both)
     */
    public static <T> Set<T> symmetricDifference(Collection<T> collection1,
                                                 Collection<T> collection2) {
        Set<T> diff1 = difference(collection1, collection2);
        Set<T> diff2 = difference(collection2, collection1);
        return union(diff1, diff2);
    }

    /**
     * Check if collections are disjoint (no common elements)
     */
    public static <T> boolean isDisjoint(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1) || isEmpty(collection2)) {
            return true;
        }
        return Collections.disjoint(collection1, collection2);
    }

    // ========== Aggregation ==========

    /**
     * Sum integers in collection
     */
    public static Integer sum(Collection<Integer> collection) {
        if (isEmpty(collection)) {
            return 0;
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Sum by mapper
     */
    public static <T> Integer sumBy(Collection<T> collection, ToIntFunction<T> mapper) {
        if (isEmpty(collection) || mapper == null) {
            return 0;
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .mapToInt(mapper)
                .sum();
    }

    /**
     * Average of integers
     */
    public static Double average(Collection<Integer> collection) {
        if (isEmpty(collection)) {
            return 0.0;
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Average by mapper
     */
    public static <T> Double averageBy(Collection<T> collection, ToIntFunction<T> mapper) {
        if (isEmpty(collection) || mapper == null) {
            return 0.0;
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .mapToInt(mapper)
                .average()
                .orElse(0.0);
    }

    /**
     * Get minimum element
     */
    public static <T extends Comparable<T>> Optional<T> min(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());
    }

    /**
     * Get minimum by comparator
     */
    public static <T> Optional<T> min(Collection<T> collection, Comparator<T> comparator) {
        if (isEmpty(collection) || comparator == null) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .min(comparator);
    }

    /**
     * Get minimum by key extractor
     */
    public static <T, U extends Comparable<U>> Optional<T> minBy(Collection<T> collection,
                                                                 Function<T, U> keyExtractor) {
        if (isEmpty(collection) || keyExtractor == null) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .min(Comparator.comparing(keyExtractor));
    }

    /**
     * Get maximum element
     */
    public static <T extends Comparable<T>> Optional<T> max(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
    }

    /**
     * Get maximum by comparator
     */
    public static <T> Optional<T> max(Collection<T> collection, Comparator<T> comparator) {
        if (isEmpty(collection) || comparator == null) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .max(comparator);
    }

    /**
     * Get maximum by key extractor
     */
    public static <T, U extends Comparable<U>> Optional<T> maxBy(Collection<T> collection,
                                                                 Function<T, U> keyExtractor) {
        if (isEmpty(collection) || keyExtractor == null) {
            return Optional.empty();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparing(keyExtractor));
    }

    /**
     * Count elements matching predicate
     */
    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return 0;
        }
        return collection.stream()
                .filter(predicate)
                .count();
    }

    // ========== Taking/Skipping Elements ==========

    /**
     * Take first N elements
     */
    public static <T> List<T> take(Collection<T> collection, int n) {
        if (isEmpty(collection) || n <= 0) {
            return new ArrayList<>();
        }
        return collection.stream()
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Skip first N elements
     */
    public static <T> List<T> skip(Collection<T> collection, int n) {
        if (isEmpty(collection) || n <= 0) {
            return new ArrayList<>(collection);
        }
        return collection.stream()
                .skip(n)
                .collect(Collectors.toList());
    }

    /**
     * Take while predicate is true
     */
    public static <T> List<T> takeWhile(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return new ArrayList<>();
        }
        return collection.stream()
                .takeWhile(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Skip while predicate is true
     */
    public static <T> List<T> skipWhile(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            return new ArrayList<>(collection);
        }
        return collection.stream()
                .dropWhile(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Get first element
     */
    public static <T> Optional<T> first(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }
        return collection.stream().findFirst();
    }

    /**
     * Get last element
     */
    public static <T> Optional<T> last(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }
        if (collection instanceof List) {
            List<T> list = (List<T>) collection;
            return Optional.of(list.get(list.size() - 1));
        }
        return collection.stream()
                .reduce((first, second) -> second);
    }

    /**
     * Get element at index (null-safe)
     */
    public static <T> Optional<T> get(List<T> list, int index) {
        if (isEmpty(list) || index < 0 || index >= list.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index));
    }

    // ========== Joining ==========

    /**
     * Join collection to string
     */
    public static <T> String join(Collection<T> collection, String delimiter) {
        if (isEmpty(collection)) {
            return "";
        }
        return collection.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Join with prefix and suffix
     */
    public static <T> String join(Collection<T> collection, String delimiter,
                                  String prefix, String suffix) {
        if (isEmpty(collection)) {
            return prefix + suffix;
        }
        return collection.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /**
     * Join with mapper
     */
    public static <T> String joinWith(Collection<T> collection, String delimiter,
                                      Function<T, String> mapper) {
        if (isEmpty(collection) || mapper == null) {
            return "";
        }
        return collection.stream()
                .map(mapper)
                .collect(Collectors.joining(delimiter));
    }

    // ========== Zipping ==========

    /**
     * Zip two lists into pairs
     */
    public static <A, B> List<Pair<A, B>> zip(List<A> list1, List<B> list2) {
        if (isEmpty(list1) || isEmpty(list2)) {
            return new ArrayList<>();
        }

        int size = Math.min(list1.size(), list2.size());
        List<Pair<A, B>> result = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            result.add(new Pair<>(list1.get(i), list2.get(i)));
        }

        return result;
    }

    /**
     * Zip with index
     */
    public static <T> List<Pair<Integer, T>> zipWithIndex(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }

        List<Pair<Integer, T>> result = new ArrayList<>();
        int index = 0;
        for (T item : collection) {
            result.add(new Pair<>(index++, item));
        }
        return result;
    }

    // ========== Frequency & Counting ==========

    /**
     * Count frequency of each element
     */
    public static <T> Map<T, Long> frequency(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new HashMap<>();
        }
        return collection.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    /**
     * Get most frequent element
     */
    public static <T> Optional<T> mostFrequent(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }

        return frequency(collection).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    /**
     * Get least frequent element
     */
    public static <T> Optional<T> leastFrequent(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }

        return frequency(collection).entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    // ========== Map Operations ==========

    /**
     * Merge two maps
     */
    public static <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> result = new HashMap<>();
        if (isNotEmpty(map1)) {
            result.putAll(map1);
        }
        if (isNotEmpty(map2)) {
            result.putAll(map2);
        }
        return result;
    }

    /**
     * Merge maps with merge function
     */
    public static <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2,
                                             BinaryOperator<V> mergeFunction) {
        if (isEmpty(map1)) {
            return new HashMap<>(emptyIfNull(map2));
        }
        if (isEmpty(map2)) {
            return new HashMap<>(map1);
        }

        Map<K, V> result = new HashMap<>(map1);
        map2.forEach((key, value) ->
                result.merge(key, value, mergeFunction)
        );
        return result;
    }

    /**
     * Filter map by key predicate
     */
    public static <K, V> Map<K, V> filterKeys(Map<K, V> map, Predicate<K> predicate) {
        if (isEmpty(map) || predicate == null) {
            return new HashMap<>();
        }
        return map.entrySet().stream()
                .filter(entry -> predicate.test(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Filter map by value predicate
     */
    public static <K, V> Map<K, V> filterValues(Map<K, V> map, Predicate<V> predicate) {
        if (isEmpty(map) || predicate == null) {
            return new HashMap<>();
        }
        return map.entrySet().stream()
                .filter(entry -> predicate.test(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Map values in map
     */
    public static <K, V, R> Map<K, R> mapValues(Map<K, V> map, Function<V, R> mapper) {
        if (isEmpty(map) || mapper == null) {
            return new HashMap<>();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapper.apply(entry.getValue())
                ));
    }

    /**
     * Map keys in map
     */
    public static <K, V, R> Map<R, V> mapKeys(Map<K, V> map, Function<K, R> mapper) {
        if (isEmpty(map) || mapper == null) {
            return new HashMap<>();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> mapper.apply(entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    /**
     * Invert map (swap keys and values)
     */
    public static <K, V> Map<V, K> invertMap(Map<K, V> map) {
        if (isEmpty(map)) {
            return new HashMap<>();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * Get value from map safely with default
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (isEmpty(map)) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }

    // ========== Utility Classes ==========

    /**
     * Pair class for zipping operations
     */
    public static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }

    // ========== Miscellaneous ==========

    /**
     * Create a safe copy of collection
     */
    public static <T> List<T> safeCopy(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(collection);
    }

    /**
     * Create immutable copy
     */
    public static <T> List<T> immutableCopy(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(collection));
    }

    /**
     * Concatenate collections
     */
    @SafeVarargs
    public static <T> List<T> concat(Collection<T>... collections) {
        if (collections == null || collections.length == 0) {
            return new ArrayList<>();
        }
        return Stream.of(collections)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Flatten nested collections
     */
    public static <T> List<T> flatten(Collection<? extends Collection<T>> collections) {
        if (isEmpty(collections)) {
            return new ArrayList<>();
        }
        return collections.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Rotate collection left by N positions
     */
    public static <T> List<T> rotateLeft(List<T> list, int positions) {
        if (isEmpty(list) || positions == 0) {
            return new ArrayList<>(list);
        }

        int size = list.size();
        positions = positions % size;
        if (positions < 0) {
            positions += size;
        }

        List<T> result = new ArrayList<>(size);
        result.addAll(list.subList(positions, size));
        result.addAll(list.subList(0, positions));
        return result;
    }

    /**
     * Rotate collection right by N positions
     */
    public static <T> List<T> rotateRight(List<T> list, int positions) {
        return rotateLeft(list, -positions);
    }

    /**
     * Random element from collection
     */
    public static <T> Optional<T> random(Collection<T> collection) {
        if (isEmpty(collection)) {
            return Optional.empty();
        }

        int index = new Random().nextInt(collection.size());
        if (collection instanceof List) {
            return Optional.of(((List<T>) collection).get(index));
        }

        return collection.stream()
                .skip(index)
                .findFirst();
    }

    /**
     * Random N elements from collection
     */
    public static <T> List<T> randomSample(Collection<T> collection, int n) {
        if (isEmpty(collection) || n <= 0) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>(collection);
        Collections.shuffle(list);
        return take(list, n);
    }
}