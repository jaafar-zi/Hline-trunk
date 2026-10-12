package com.example.demo.modules.kitchen.application.query;

import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenConfiguration;
import com.example.demo.modules.kitchen.domain.model.KitchenConfigurationItem;
import com.example.demo.modules.kitchen.domain.model.vo.KitchenConfigurationStatus;
import com.example.demo.modules.kitchen.domain.repository.KitchenConfigurationItemRepository;
import com.example.demo.modules.kitchen.domain.repository.KitchenConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kitchen Configuration Query Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KitchenConfigurationQueryService
        extends AbstractQueryService<KitchenConfiguration, UUID> {

    private final KitchenConfigurationRepository configurationRepository;
    private final KitchenConfigurationItemRepository itemRepository;

    public KitchenConfigurationQueryService(
            KitchenConfigurationRepository configurationRepository,
            KitchenConfigurationItemRepository itemRepository,
            NormalizationService normalizationService) {
        super(configurationRepository, normalizationService);
        this.configurationRepository = configurationRepository;
        this.itemRepository = itemRepository;
    }

    // ====================================
    // Configuration - Find
    // ====================================

    public KitchenConfiguration findByIdWithAll(UUID configId) {
        return configurationRepository.findByIdWithAll(configId)
            .orElseThrow(() -> new EntityNotFoundException(KitchenConfiguration.class, configId));
    }

    public Optional<KitchenConfiguration> findByIdAndCustomer(UUID configId, UUID customerId) {
        return configurationRepository.findByIdAndCustomerId(configId, customerId);
    }

    // ====================================
    // Configuration - Find by Customer
    // ====================================

    public List<KitchenConfiguration> findByCustomer(UUID customerId) {
        return configurationRepository.findByCustomerId(customerId);
    }

    public Page<KitchenConfiguration> findByCustomer(UUID customerId, Pageable pageable) {
        return configurationRepository.findByCustomerId(customerId, pageable);
    }

    public List<KitchenConfiguration> findDraftsByCustomer(UUID customerId) {
        return configurationRepository.findDraftsByCustomerId(customerId);
    }

    public List<KitchenConfiguration> findByCustomerAndStatus(
            UUID customerId,
            KitchenConfigurationStatus status) {
        return configurationRepository.findByCustomerIdAndStatus(customerId, status);
    }

    // ====================================
    // Configuration - Find by Session (guests)
    // ====================================

    public Optional<KitchenConfiguration> findActiveDraftBySession(String sessionId) {
        return configurationRepository.findActiveDraftBySessionId(sessionId);
    }

    public List<KitchenConfiguration> findBySession(String sessionId) {
        return configurationRepository.findBySessionId(sessionId);
    }

    // ====================================
    // Items
    // ====================================

    public List<KitchenConfigurationItem> findItemsByConfiguration(UUID configId) {
        return itemRepository.findByConfigurationIdWithDetails(configId);
    }

    public List<KitchenConfigurationItem> findNonOverriddenItems(UUID configId) {
        return itemRepository.findNonOverriddenByConfigurationId(configId);
    }

    public List<KitchenConfigurationItem> findColourOverriddenItems(UUID configId) {
        return itemRepository.findColourOverriddenByConfigurationId(configId);
    }

    // ====================================
    // Statistics
    // ====================================

    public long countDraftsByCustomer(UUID customerId) {
        return configurationRepository.countDraftsByCustomerId(customerId);
    }

    public long countByStatus(KitchenConfigurationStatus status) {
        return configurationRepository.countByStatus(status);
    }

    public Long sumQuantityByConfiguration(UUID configId) {
        return itemRepository.sumQuantityByConfigurationId(configId);
    }

    public java.math.BigDecimal sumTotalByConfiguration(UUID configId) {
        return itemRepository.sumTotalByConfigurationId(configId);
    }

    // ====================================
    // AbstractQueryService
    // ====================================

    @Override
    protected String getEntityCacheName() {
        return "kitchen-configurations";
    }

    @Override
    protected Class<KitchenConfiguration> getEntityClass() {
        return KitchenConfiguration.class;
    }
}