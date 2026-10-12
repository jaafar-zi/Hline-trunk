package com.example.demo.modules.kitchen.application.query;

import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishType;
import com.example.demo.modules.kitchen.domain.repository.KitchenFinishTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class KitchenFinishTypeQueryService
        extends AbstractQueryService<KitchenFinishType, UUID> {

    private final KitchenFinishTypeRepository finishTypeRepository;

    public KitchenFinishTypeQueryService(
            KitchenFinishTypeRepository finishTypeRepository,
            NormalizationService normalizationService) {
        super(finishTypeRepository, normalizationService);
        this.finishTypeRepository = finishTypeRepository;
    }

    @Cacheable(cacheNames = "kitchen-finish-types", key = "'active-all'")
    public List<KitchenFinishType> findAllActive() {
        return finishTypeRepository.findAllActive();
    }

    @Cacheable(cacheNames = "kitchen-finish-types", key = "'code:' + #code")
    public Optional<KitchenFinishType> findByCode(String code) {
        return finishTypeRepository.findByCode(code);
    }

    @Override
    protected String getEntityCacheName() {
        return "kitchen-finish-types";
    }

    @Override
    protected Class<KitchenFinishType> getEntityClass() {
        return KitchenFinishType.class;
    }
}