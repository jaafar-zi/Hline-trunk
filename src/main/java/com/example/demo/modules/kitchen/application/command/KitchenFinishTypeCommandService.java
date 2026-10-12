package com.example.demo.modules.kitchen.application.command;

import com.example.demo.common.base.service.command.AbstractCommandService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.kitchen.domain.model.KitchenFinishType;
import com.example.demo.modules.kitchen.domain.repository.KitchenFinishTypeRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class KitchenFinishTypeCommandService
        extends AbstractCommandService<KitchenFinishType, UUID> {

    private final KitchenFinishTypeRepository finishTypeRepository;

    public KitchenFinishTypeCommandService(
            KitchenFinishTypeRepository finishTypeRepository,
            CacheManager cacheManager,
            ApplicationEventPublisher eventPublisher,
            Validator validator,
            NormalizationService normalizationService) {
        super(finishTypeRepository, cacheManager, eventPublisher, validator, normalizationService);
        this.finishTypeRepository = finishTypeRepository;
    }

    public KitchenFinishType createFinishType(String code, String name, String description) {
        log.debug("Creating finish type: code={}, name={}", code, name);

        if (finishTypeRepository.existsByCode(code.toUpperCase())) {
            throw new IllegalStateException("Finish type code already exists: " + code);
        }

        KitchenFinishType finishType = KitchenFinishType.create(code, name, description);
        return create(finishType);
    }

    public KitchenFinishType updateFinishType(UUID id, String name, String description) {
        KitchenFinishType finishType = finishTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishType.class, id));

        finishType.updateDetails(name, description);
        return update(id, finishType);
    }

    public KitchenFinishType activate(UUID id) {
        KitchenFinishType finishType = finishTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishType.class, id));

        finishType.activate();
        return update(id, finishType);
    }

    public KitchenFinishType deactivate(UUID id) {
        KitchenFinishType finishType = finishTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishType.class, id));

        finishType.deactivate();
        return update(id, finishType);
    }

    public void softDeleteFinishType(UUID id, String deletedBy) {
        KitchenFinishType finishType = finishTypeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(KitchenFinishType.class, id));

        finishType.delete(deletedBy);
        update(id, finishType);
    }

    @Override
    protected List<String> getCacheNames() {
        return List.of("kitchen-finish-types");
    }

    @Override
    protected Class<KitchenFinishType> getEntityClass() {
        return KitchenFinishType.class;
    }
}