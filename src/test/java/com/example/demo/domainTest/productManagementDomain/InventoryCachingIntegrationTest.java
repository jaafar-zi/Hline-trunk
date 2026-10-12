// package com.example.demo.domainTest.productManagementDomain;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.cache.Cache;
// import org.springframework.cache.CacheManager;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// @Transactional
// class InventoryCachingIntegrationTest {

//     @Autowired
//     private InventoryCommandService inventoryCommandService;

//     @Autowired
//     private InventoryQueryService inventoryQueryService;

//     @Autowired
//     private InventoryRepository inventoryRepository;

//     @Autowired
//     private ProductRepository productRepository;

//     @Autowired
//     private CategoryRepository categoryRepository;

//     @Autowired
//     private CacheManager cacheManager;

//     private Product testProduct;
//     private Inventory testInventory;

//     @BeforeEach
//     void setup() {
//         // Create a test category
//         Category testCategory = new Category();
//         testCategory.setName("Test Category");
//         testCategory = categoryRepository.save(testCategory);

//         // Create a test product
//         testProduct = new Product();
//         testProduct.setName("Test Product");
//         testProduct.setCategory(testCategory);
//         testProduct = productRepository.save(testProduct);

//         // Create a test inventory
//         testInventory = new Inventory(
//                 testProduct,
//                 100,
//                 10,
//                 LocalDateTime.now()
//         );
//         testInventory = inventoryRepository.save(testInventory);
//     }

//     @Test
//     void testCacheableQueryService() {
//         Long productId = testProduct.getId();

//         // Clear all caches before test
//         cacheManager.getCacheNames().forEach(cacheName -> {
//             Cache cache = cacheManager.getCache(cacheName);
//             if (cache != null) {
//                 cache.clear();
//             }
//         });

//         // Verify which cache is being used
//         Cache dtoCache = cacheManager.getCache(CacheNames.DTO_INVENTORIES);
//         System.out.println("Cache type: " + dtoCache.getClass().getName());
//         System.out.println("Native cache: " + dtoCache.getNativeCache().getClass().getName());

//         // First call -> hits DB
//         System.out.println("=== FIRST CALL ===");
//         InventoryDTO firstCall = inventoryQueryService.getStockDetailsCached(productId);
//         System.out.println("First call instance: " + System.identityHashCode(firstCall));

//         // Check what's in cache
//         Cache.ValueWrapper cached = dtoCache.get("product:" + productId);
//         if (cached != null) {
//             System.out.println("Cached value instance: " + System.identityHashCode(cached.get()));
//             System.out.println("Are they same? " + (firstCall == cached.get()));
//         }

//         // Second call -> should hit L2 (Redis) cache
//         System.out.println("=== SECOND CALL ===");
//         InventoryDTO secondCall = inventoryQueryService.getStockDetailsCached(productId);
//         System.out.println("Second call instance: " + System.identityHashCode(secondCall));

//         System.out.println("=== COMPARISON ===");
//         System.out.println("firstCall == secondCall: " + (firstCall == secondCall));
//         System.out.println("firstCall.equals(secondCall): " + firstCall.equals(secondCall));

//         // The actual assertions
//         assertNotSame(firstCall, secondCall, "Should be different instances (L2 cache)");
//         assertEquals(firstCall, secondCall, "Data should be equal from cache");
//     }

//     @Test
//     void testCacheEvictionAfterUpdate() {
//         Long productId = testProduct.getId();

//         // Prime the cache
//         InventoryDTO cached = inventoryQueryService.getStockDetailsCached(productId);

//         // Update stock via command service
//         inventoryCommandService.updateStock(productId, 200);
//         inventoryRepository.flush(); // ensure DB is updated

//         // Fetch again via query service
//         InventoryDTO afterUpdate = inventoryQueryService.getStockDetailsCached(productId);

//         assertEquals(200, afterUpdate.getQuantityInStock(), "Quantity should reflect updated value");
//     }


//     @Test
//     void testCacheEvictionAfterDelete() {
//         Long productId = testProduct.getId();

//         // Prime the cache
//         inventoryQueryService.getStockDetailsCached(productId);

//         // Delete inventory via command service -> should evict caches
//         inventoryCommandService.deleteInventory(productId);

//         // Fetch again -> should throw exception
//         assertThrows(RuntimeException.class, () -> inventoryQueryService.getStockDetailsCached(productId));
//     }
// }
