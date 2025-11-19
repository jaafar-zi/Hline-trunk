package com.example.demo.domain.productManagementDomain.service;

import com.example.demo.common.base.service.AbstractBaseService;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.domain.productManagementDomain.dto.ProductAttributeDTO;
import com.example.demo.domain.productManagementDomain.entity.ProductAttribute;
import com.example.demo.domain.productManagementDomain.mapper.ProductAttributeMapper;
import com.example.demo.domain.productManagementDomain.repository.ProductAttributeRepository;
import com.example.demo.domain.productManagementDomain.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductAttributeService extends AbstractBaseService<ProductAttribute, Long> {

    private final ProductAttributeRepository productAttributeRepository;
    private final ProductRepository productRepository;
    private final ProductAttributeMapper productAttributeMapper;

    public ProductAttributeService(ProductAttributeRepository productAttributeRepository, ProductRepository productRepository, ProductAttributeMapper productAttributeMapper) {
        super(productAttributeRepository);
        this.productAttributeRepository = productAttributeRepository;
        this.productRepository = productRepository;
        this.productAttributeMapper = productAttributeMapper;
    }

    // ───────────────────────────────────────────────
    //  BASE CONFIG
    // ───────────────────────────────────────────────

    @Override
    protected String getCacheName() {
        return "productAttributes";
    }

    @Override
    protected Class<?> getEntityClass() {
        return ProductAttribute.class;
    }

    // ───────────────────────────────────────────────
    //  CREATE
    // ───────────────────────────────────────────────

    @Transactional
    public ProductAttributeDTO create(ProductAttributeDTO dto) {
        ProductAttribute entity = toEntity(dto);
        ProductAttribute saved = super.save(entity); // uses AbstractBaseService.save()
        log.info("Created ProductAttribute [id={}]", saved.getId());
        return productAttributeMapper.toDto(saved);
    }

    // ───────────────────────────────────────────────
    //  READ
    // ───────────────────────────────────────────────

    public Page<ProductAttributeDTO> findAll(Pageable pageable) {
        return productAttributeRepository.findAll(pageable)
                .map(productAttributeMapper::toDto);
    }

    public List<ProductAttributeDTO> findAllByProductAndLang(Long productId, String lang) {
        return productAttributeRepository.findProductAttributesByProductIdAndLang(productId, lang);
    }

    public ProductAttributeDTO findDtoById(Long id) {
        ProductAttribute entity = super.findByIdOrThrow(id);
        return productAttributeMapper.toDto(entity);
    }

    public List<ProductAttributeDTO> findByProductId(Long productId) {
        return productAttributeRepository.findByProductId(productId)
                .stream()
                .map(productAttributeMapper::toDto)
                .toList();
    }

    public Page<ProductAttributeDTO> findUnassignedToProduct(Long productId, Pageable pageable) {
        return productAttributeRepository.findAttributesNotAssignedToProduct(productId, pageable)
                .map(productAttributeMapper::toDto);
    }

    public List<ProductAttributeDTO> findByName(String attributeName) {
        return productAttributeRepository.findByAttributeName(attributeName)
                .stream()
                .map(productAttributeMapper::toDto)
                .toList();
    }

    public List<ProductAttributeDTO> findByNameAndValue(String name, String value) {
        return productAttributeRepository.findByAttributeNameAndAttributeValue(name, value)
                .stream()
                .map(productAttributeMapper::toDto)
                .toList();
    }

    public List<ProductAttributeDTO> findByProductIdAndName(Long productId, String attributeName) {
        return productAttributeRepository.findByProductIdAndAttributeName(productId, attributeName)
                .stream()
                .map(productAttributeMapper::toDto)
                .toList();
    }

    public Map<Long, List<ProductAttributeDTO>> findGroupedByProductIds(List<Long> productIds, String lang) {
        return productAttributeRepository.getAttributesByProductIds(productIds, lang)
                .stream()
                .collect(Collectors.groupingBy(ProductAttributeDTO::getProductId));
    }

    // ───────────────────────────────────────────────
    //  UPDATE
    // ───────────────────────────────────────────────

    @Transactional
    public ProductAttributeDTO update(Long id, ProductAttributeDTO dto) {
        ProductAttribute existing = super.findByIdOrThrow(id);

        existing.setAttributeName(dto.getAttributeName());
        existing.setAttributeValue(dto.getAttributeValue());
        existing.setLang(dto.getLang());

        ProductAttribute updated = super.update(id, existing);
        log.info("Updated ProductAttribute [id={}]", id);
        return productAttributeMapper.toDto(updated);
    }

    // ───────────────────────────────────────────────
    //  DELETE
    // ───────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        super.delete(id);
        log.info("Deleted ProductAttribute [id={}]", id);
    }

    // ───────────────────────────────────────────────
    //  INTERNAL MAPPING
    // ───────────────────────────────────────────────

    private ProductAttribute toEntity(ProductAttributeDTO dto) {
        ProductAttribute entity = new ProductAttribute();
        entity.setId(dto.getId());
        entity.setAttributeName(dto.getAttributeName());
        entity.setAttributeValue(dto.getAttributeValue());
        entity.setLang(dto.getLang());

        if (dto.getProductId() != null) {
            productRepository.findById(dto.getProductId())
                    .ifPresentOrElse(
                            entity::setProduct,
                            () -> { throw new EntityNotFoundException(getEntityClass(), dto.getProductId()); }
                    );
        }

        return entity;
    }

    // ───────────────────────────────────────────────
    //  POST-SAVE / DELETE HOOKS
    // ───────────────────────────────────────────────

    @Override
    protected void onAfterSave(ProductAttribute entity) {
        log.debug("Post-save hook triggered for ProductAttribute [id={}]", entity.getId());
    }

    @Override
    protected void onAfterDelete(Long id) {
        log.debug("Post-delete hook triggered for ProductAttribute [id={}]", id);
    }
}
