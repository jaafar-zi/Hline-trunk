package com.example.demo.modules.user.application.query;

import com.example.demo.common.annotation.ReadOnly;
import com.example.demo.common.base.dto.PageResponse;
import com.example.demo.common.base.service.query.AbstractQueryService;
import com.example.demo.common.constant.CacheConstants;
import com.example.demo.common.exception.EntityNotFoundException;
import com.example.demo.common.normalization.NormalizationService;
import com.example.demo.modules.user.application.dto.response.UserGroupResponse;
import com.example.demo.modules.user.application.mapper.UserGroupMapper;
import com.example.demo.modules.user.domain.model.UserGroup;
import com.example.demo.modules.user.domain.repository.UserGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Query service for UserGroup aggregate
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class UserGroupQueryService extends AbstractQueryService<UserGroup, UUID> {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupMapper userGroupMapper;

    public UserGroupQueryService(UserGroupRepository userGroupRepository, NormalizationService normalizationService, UserGroupMapper userGroupMapper) {
        super(userGroupRepository, normalizationService);
        this.userGroupRepository = userGroupRepository;
        this.userGroupMapper = userGroupMapper;
    }

    @Override
    protected String getEntityCacheName() {
        return CacheConstants.CACHE_USER;
    }

    @Override
    protected Class<UserGroup> getEntityClass() {
        return UserGroup.class;
    }

    /**
     * Find user group by ID
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER, key = "'group:' + #id", unless = "#result == null")
    public UserGroupResponse findUserGroupById(UUID id) {
        log.debug("Finding user group by ID: {}", id);

        UserGroup userGroup = findById(id);
        return userGroupMapper.toResponse(userGroup);
    }

    /**
     * Find user group by code
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER, key = "'group:code:' + #code", unless = "#result == null")
    public UserGroupResponse findByCode(String code) {
        log.debug("Finding user group by code: {}", code);

        UserGroup userGroup = userGroupRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass(),code));

        return userGroupMapper.toResponse(userGroup);
    }

    /**
     * Get all user groups
     */
    @ReadOnly
    public PageResponse<UserGroupResponse> getAllUserGroups(Pageable pageable) {
        log.debug("Getting all user groups with pagination");

        Page<UserGroup> groups = findAll(pageable);
        return PageResponse.from(groups, userGroupMapper::toResponse);
    }

    /**
     * Get all active user groups
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER, key = "'groups:active'")
    public List<UserGroupResponse> getActiveUserGroups() {
        log.debug("Getting all active user groups");

        List<UserGroup> groups = userGroupRepository.findAllActive();
        return groups.stream()
                .map(userGroupMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all inactive user groups
     */
    @ReadOnly
    public List<UserGroupResponse> getInactiveUserGroups() {
        log.debug("Getting all inactive user groups");

        List<UserGroup> groups = userGroupRepository.findAllInactive();
        return groups.stream()
                .map(userGroupMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get root user groups (no parent)
     */
    @ReadOnly
    @Cacheable(cacheNames = CacheConstants.CACHE_USER, key = "'groups:root'")
    public List<UserGroupResponse> getRootUserGroups() {
        log.debug("Getting root user groups");

        List<UserGroup> groups = userGroupRepository.findRootGroups();
        return groups.stream()
                .map(userGroupMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get child groups of parent
     */
    @ReadOnly
    public List<UserGroupResponse> getChildGroups(UUID parentId) {
        log.debug("Getting child groups of parent: {}", parentId);

        List<UserGroup> groups = userGroupRepository.findByParentGroupId(parentId);
        return groups.stream()
                .map(userGroupMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Count users in group
     */
    @ReadOnly
    public long countUsersInGroup(UUID groupId) {
        log.debug("Counting users in group: {}", groupId);
        return userGroupRepository.countUsersByGroupId(groupId);
    }

    /**
     * Count active users in group
     */
    @ReadOnly
    public long countActiveUsersInGroup(UUID groupId) {
        log.debug("Counting active users in group: {}", groupId);
        return userGroupRepository.countActiveUsersByGroupId(groupId);
    }

    /**
     * Get user group hierarchy
     */
    @ReadOnly
    public List<UserGroupHierarchyResponse> getUserGroupHierarchy() {
        log.debug("Getting user group hierarchy");

        List<UserGroup> rootGroups = userGroupRepository.findRootGroups();

        return rootGroups.stream()
                .map(this::buildHierarchy)
                .collect(Collectors.toList());
    }

    private UserGroupHierarchyResponse buildHierarchy(UserGroup group) {
        UserGroupHierarchyResponse response = new UserGroupHierarchyResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setCode(group.getCode());
        response.setActive(group.getActive());
        response.setUserCount(group.getUserCount());

        if (!group.getChildGroups().isEmpty()) {
            response.setChildren(
                    group.getChildGroups().stream()
                            .map(this::buildHierarchy)
                            .collect(Collectors.toList())
            );
        }

        return response;
    }

    /**
     * User group hierarchy response
     */
    @lombok.Data
    public static class UserGroupHierarchyResponse {
        private UUID id;
        private String name;
        private String code;
        private Boolean active;
        private Integer userCount;
        private List<UserGroupHierarchyResponse> children;
    }
}