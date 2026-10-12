package com.example.demo.modules.user.application.mapper;

import com.example.demo.common.mapper.BaseMapper;
import com.example.demo.modules.user.application.dto.request.CreateUserGroupRequest;
import com.example.demo.modules.user.application.dto.request.UpdateUserGroupRequest;
import com.example.demo.modules.user.application.dto.response.UserGroupResponse;
import com.example.demo.modules.user.domain.model.UserGroup;
import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for UserGroup entity <-> DTOs
 *
 * Features:
 * - Bidirectional mapping (Entity ↔ DTO)
 * - User count calculations (total and active)
 * - Hierarchical group support (parent/child relationships)
 * - Role assignment handling
 * - Partial updates (PATCH operations)
 * - Null-safe operations
 * - Tree structure utilities
 *
 * @author Architecture Team
 * @version 1.0
 */
@Mapper(
        componentModel = "spring",
        uses = {RoleMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public abstract class UserGroupMapper implements BaseMapper<UserGroup, UserGroupResponse> {

    // ====================================
    // Entity to Response Mappings
    // ====================================

    /**
     * Map UserGroup entity to UserGroupResponse (summary view)
     * Implementation of BaseMapper.toDto()
     */
    @Override
    @Mapping(target = "userCount", expression = "java(getUserCount(userGroup))")
    @Mapping(target = "activeUserCount", expression = "java(getActiveUserCount(userGroup))")
    @Mapping(target = "roleCount", expression = "java(getRoleCount(userGroup))")
    @Mapping(target = "childGroupCount", expression = "java(getChildGroupCount(userGroup))")
    @Mapping(target = "hasParent", expression = "java(hasParent(userGroup))")
    @Mapping(target = "parentGroupId", expression = "java(getParentGroupId(userGroup))")
    @Mapping(target = "parentGroupName", expression = "java(getParentGroupName(userGroup))")
    @Mapping(target = "depth", expression = "java(calculateDepth(userGroup))")
    public abstract UserGroupResponse toDto(UserGroup userGroup);

    /**
     * Alias for toDto() - for better readability in services
     */
    public UserGroupResponse toResponse(UserGroup userGroup) {
        return toDto(userGroup);
    }

    /**
     * Map list of UserGroup entities to list of UserGroupResponses
     */
    public List<UserGroupResponse> toResponseList(List<UserGroup> userGroups) {
        return toDtoList(userGroups);
    }

    /**
     * Map set of UserGroup entities to set of UserGroupResponses
     */
    public Set<UserGroupResponse> toResponseSet(Set<UserGroup> userGroups) {
        return toDtoSet(userGroups);
    }

    // ====================================
    // Request to Entity Mappings
    // ====================================

    /**
     * Map CreateUserGroupRequest to UserGroup entity
     */
    @Mapping(target = "code", qualifiedByName = "normalizeGroupCode", source = "code")
    @Mapping(target = "name", qualifiedByName = "normalizeGroupName", source = "name")
    @Mapping(target = "description", qualifiedByName = "normalizeGroupDescription", source = "description")
    @Mapping(target = "active", expression = "java(java.util.Optional.ofNullable(request.getActive()).orElse(true))")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "parentGroup", ignore = true)
    @Mapping(target = "childGroups", ignore = true)
    public abstract UserGroup fromCreateRequest(CreateUserGroupRequest request);

    /**
     * Implementation of BaseMapper.toEntity(UserGroupResponse)
     * Not supported - cannot create entity from response DTO
     */
    @Override
    public UserGroup toEntity(UserGroupResponse dto) {
        throw new UnsupportedOperationException("Cannot create UserGroup entity from UserGroupResponse. Use fromCreateRequest() instead.");
    }

    // ====================================
    // Update Mappings
    // ====================================

    /**
     * Update existing UserGroup entity from UpdateUserGroupRequest (PATCH)
     * Implementation of BaseMapper.updateEntityFromDto
     */
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Code is immutable
    @Mapping(target = "name", qualifiedByName = "normalizeGroupName")
    @Mapping(target = "description", qualifiedByName = "normalizeGroupDescription")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "parentGroup", ignore = true)
    @Mapping(target = "childGroups", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract UserGroup updateEntityFromDto(UpdateUserGroupRequest dto, @MappingTarget UserGroup entity);

    /**
     * Update user group with name only
     */
    public UserGroup withName(UserGroup userGroup, String name) {
        if (userGroup == null) {
            return null;
        }
        UserGroup updated = toShallowCopy(userGroup);
        updated.setName(normalizeGroupName(name));
        return updated;
    }

    /**
     * Update user group with description only
     */
    public UserGroup withDescription(UserGroup userGroup, String description) {
        if (userGroup == null) {
            return null;
        }
        UserGroup updated = toShallowCopy(userGroup);
        updated.setDescription(normalizeGroupDescription(description));
        return updated;
    }

    /**
     * Update user group with active status only
     */
    public UserGroup withActiveStatus(UserGroup userGroup, Boolean active) {
        if (userGroup == null) {
            return null;
        }
        UserGroup updated = toShallowCopy(userGroup);
        updated.setActive(active);
        return updated;
    }

    /**
     * Update user group with priority only
     */
    public UserGroup withPriority(UserGroup userGroup, Integer priority) {
        if (userGroup == null) {
            return null;
        }
        UserGroup updated = toShallowCopy(userGroup);
        updated.setPriority(priority);
        return updated;
    }

    // ====================================
    // Helper Methods (Getters)
    // ====================================

    /**
     * Get code from user group
     */
    protected String getCode(UserGroup userGroup) {
        return userGroup != null ? userGroup.getCode() : null;
    }

    /**
     * Get name from user group
     */
    protected String getName(UserGroup userGroup) {
        return userGroup != null ? userGroup.getName() : null;
    }

    /**
     * Get description from user group
     */
    protected String getDescription(UserGroup userGroup) {
        return userGroup != null ? userGroup.getDescription() : null;
    }

    /**
     * Get user count from user group
     */
    protected Integer getUserCount(UserGroup userGroup) {
        if (userGroup == null) {
            return 0;
        }
        return userGroup.getUserCount();
    }

    /**
     * Get active user count from user group
     */
    protected Long getActiveUserCount(UserGroup userGroup) {
        if (userGroup == null) {
            return 0L;
        }
        return userGroup.getActiveUserCount();
    }

    /**
     * Get role count from user group
     */
    protected Integer getRoleCount(UserGroup userGroup) {
        if (userGroup == null || userGroup.getRoles() == null) {
            return 0;
        }
        return userGroup.getRoles().size();
    }

    /**
     * Get child group count
     */
    protected Integer getChildGroupCount(UserGroup userGroup) {
        if (userGroup == null || userGroup.getChildGroups() == null) {
            return 0;
        }
        return userGroup.getChildGroups().size();
    }

    /**
     * Check if user group has parent
     */
    protected Boolean hasParent(UserGroup userGroup) {
        if (userGroup == null) {
            return false;
        }
        return userGroup.getParentGroup() != null;
    }

    /**
     * Get parent group ID
     */
    protected UUID getParentGroupId(UserGroup userGroup) {
        if (userGroup == null || userGroup.getParentGroup() == null) {
            return null;
        }
        return userGroup.getParentGroup().getId();
    }

    /**
     * Get parent group name
     */
    protected String getParentGroupName(UserGroup userGroup) {
        if (userGroup == null || userGroup.getParentGroup() == null) {
            return null;
        }
        return userGroup.getParentGroup().getName();
    }

    /**
     * Calculate depth in hierarchy (0 for root groups)
     */
    protected Integer calculateDepth(UserGroup userGroup) {
        if (userGroup == null) {
            return 0;
        }

        int depth = 0;
        UserGroup current = userGroup;

        // Prevent infinite loops with a max depth
        int maxDepth = 100;
        while (current.getParentGroup() != null && depth < maxDepth) {
            depth++;
            current = current.getParentGroup();
        }

        return depth;
    }

    // ====================================
    // Normalization Methods (with @Named)
    // ====================================

    /**
     * Normalize group code (uppercase, trimmed, underscores)
     * Used exclusively for UserGroup.code field
     */
    @Named("normalizeGroupCode")
    protected String normalizeGroupCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Group code cannot be null or empty");
        }
        return code.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    /**
     * Normalize group name (trimmed, preserve case)
     * Used exclusively for UserGroup.name field
     */
    @Named("normalizeGroupName")
    protected String normalizeGroupName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Normalize group description (trimmed, preserve case)
     * Used exclusively for UserGroup.description field
     */
    @Named("normalizeGroupDescription")
    protected String normalizeGroupDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Create shallow copy of user group (for immutable updates)
     */
    private UserGroup toShallowCopy(UserGroup userGroup) {
        return UserGroup.builder()
                .code(userGroup.getCode())
                .name(userGroup.getName())
                .description(userGroup.getDescription())
                .active(userGroup.getActive())
                .priority(userGroup.getPriority())
                .metadata(userGroup.getMetadata())
                .build();
    }

    // ====================================
    // Mapping to Nested DTOs
    // ====================================

    /**
     * Map UserGroup to minimal info (id, code, name only)
     */
    public UserGroupMinimalInfo toMinimalInfo(UserGroup userGroup) {
        if (userGroup == null) {
            return null;
        }

        return UserGroupMinimalInfo.builder()
                .id(userGroup.getId())
                .code(userGroup.getCode())
                .name(userGroup.getName())
                .active(userGroup.getActive())
                .parentGroupId(getParentGroupId(userGroup))
                .build();
    }

    /**
     * Map list to minimal info list
     */
    public List<UserGroupMinimalInfo> toMinimalInfoList(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .map(this::toMinimalInfo)
                .collect(Collectors.toList());
    }

    /**
     * Map UserGroup to tree node (for hierarchical display)
     */
    public UserGroupTreeNode toTreeNode(UserGroup userGroup) {
        if (userGroup == null) {
            return null;
        }

        UserGroupTreeNode node = UserGroupTreeNode.builder()
                .id(userGroup.getId())
                .code(userGroup.getCode())
                .name(userGroup.getName())
                .description(userGroup.getDescription())
                .active(userGroup.getActive())
                .priority(userGroup.getPriority())
                .userCount(getUserCount(userGroup))
                .activeUserCount(getActiveUserCount(userGroup))
                .depth(calculateDepth(userGroup))
                .hasChildren(getChildGroupCount(userGroup) > 0)
                .build();

        // Recursively map child groups
        if (userGroup.getChildGroups() != null && !userGroup.getChildGroups().isEmpty()) {
            List<UserGroupTreeNode> children = userGroup.getChildGroups().stream()
                    .map(this::toTreeNode)
                    .sorted(Comparator.comparing((UserGroupTreeNode n) ->
                                    Optional.ofNullable(n.getPriority()).orElse(Integer.MAX_VALUE))
                            .thenComparing(UserGroupTreeNode::getName))
                    .collect(Collectors.toList());
            node.setChildren(children);
        }

        return node;
    }

    /**
     * Map list to tree node list (only root groups)
     */
    public List<UserGroupTreeNode> toTreeNodeList(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        // Only include root groups (no parent)
        return userGroups.stream()
                .filter(ug -> ug.getParentGroup() == null)
                .map(this::toTreeNode)
                .sorted(Comparator.comparing((UserGroupTreeNode n) ->
                                Optional.ofNullable(n.getPriority()).orElse(Integer.MAX_VALUE))
                        .thenComparing(UserGroupTreeNode::getName))
                .collect(Collectors.toList());
    }

    // ====================================
    // Nested DTOs
    // ====================================

    /**
     * Minimal user group information DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserGroupMinimalInfo implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private UUID id;
        private String code;
        private String name;
        private Boolean active;
        private UUID parentGroupId;
    }

    /**
     * User group tree node DTO (for hierarchical display)
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserGroupTreeNode implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private UUID id;
        private String code;
        private String name;
        private String description;
        private Boolean active;
        private Integer priority;
        private Integer userCount;
        private Long activeUserCount;
        private Integer depth;
        private Boolean hasChildren;
        private List<UserGroupTreeNode> children;
    }

    // ... (rest of the code remains exactly the same)

    // ====================================
    // Hierarchical Operations
    // ====================================

    /**
     * Get all ancestor groups (parent, grandparent, etc.)
     */
    public List<UserGroupMinimalInfo> getAncestors(UserGroup userGroup) {
        if (userGroup == null) {
            return Collections.emptyList();
        }

        List<UserGroupMinimalInfo> ancestors = new ArrayList<>();
        UserGroup current = userGroup.getParentGroup();

        // Prevent infinite loops
        int maxDepth = 100;
        int depth = 0;

        while (current != null && depth < maxDepth) {
            ancestors.add(toMinimalInfo(current));
            current = current.getParentGroup();
            depth++;
        }

        return ancestors;
    }

    /**
     * Get all descendant groups (children, grandchildren, etc.)
     */
    public List<UserGroupMinimalInfo> getDescendants(UserGroup userGroup) {
        if (userGroup == null) {
            return Collections.emptyList();
        }

        List<UserGroupMinimalInfo> descendants = new ArrayList<>();
        collectDescendants(userGroup, descendants);

        return descendants;
    }

    /**
     * Helper method to recursively collect descendants
     */
    private void collectDescendants(UserGroup userGroup, List<UserGroupMinimalInfo> descendants) {
        if (userGroup == null || userGroup.getChildGroups() == null) {
            return;
        }

        for (UserGroup child : userGroup.getChildGroups()) {
            descendants.add(toMinimalInfo(child));
            collectDescendants(child, descendants);
        }
    }

    /**
     * Get path from root to this group
     */
    public List<UserGroupMinimalInfo> getPath(UserGroup userGroup) {
        if (userGroup == null) {
            return Collections.emptyList();
        }

        List<UserGroupMinimalInfo> path = new ArrayList<>(getAncestors(userGroup));
        Collections.reverse(path); // Root first
        path.add(toMinimalInfo(userGroup)); // Add current group at the end

        return path;
    }

    /**
     * Get path as string (e.g., "Root > Department > Team")
     */
    public String getPathAsString(UserGroup userGroup) {
        if (userGroup == null) {
            return "";
        }

        List<UserGroupMinimalInfo> path = getPath(userGroup);

        return path.stream()
                .map(UserGroupMinimalInfo::getName)
                .collect(Collectors.joining(" > "));
    }

    /**
     * Check if group is root (no parent)
     */
    public boolean isRoot(UserGroup userGroup) {
        return userGroup != null && userGroup.getParentGroup() == null;
    }

    /**
     * Check if group is leaf (no children)
     */
    public boolean isLeaf(UserGroup userGroup) {
        return userGroup != null &&
                (userGroup.getChildGroups() == null || userGroup.getChildGroups().isEmpty());
    }

    /**
     * Check if group is ancestor of another group
     */
    public boolean isAncestorOf(UserGroup potentialAncestor, UserGroup userGroup) {
        if (potentialAncestor == null || userGroup == null) {
            return false;
        }

        UserGroup current = userGroup.getParentGroup();

        // Prevent infinite loops
        int maxDepth = 100;
        int depth = 0;

        while (current != null && depth < maxDepth) {
            if (current.getId().equals(potentialAncestor.getId())) {
                return true;
            }
            current = current.getParentGroup();
            depth++;
        }

        return false;
    }

    /**
     * Check if group is descendant of another group
     */
    public boolean isDescendantOf(UserGroup potentialDescendant, UserGroup userGroup) {
        return isAncestorOf(userGroup, potentialDescendant);
    }

    // ====================================
    // Collection Utilities
    // ====================================

    /**
     * Extract user group IDs from collection
     */
    public Set<UUID> extractGroupIds(Set<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptySet();
        }

        return userGroups.stream()
                .map(UserGroup::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Extract user group codes from collection
     */
    public Set<String> extractGroupCodes(Set<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptySet();
        }

        return userGroups.stream()
                .map(UserGroup::getCode)
                .collect(Collectors.toSet());
    }

    /**
     * Filter active groups
     */
    public List<UserGroupResponse> filterActiveGroups(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .filter(ug -> Boolean.TRUE.equals(ug.getActive()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter inactive groups
     */
    public List<UserGroupResponse> filterInactiveGroups(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .filter(ug -> !Boolean.TRUE.equals(ug.getActive()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter root groups (no parent)
     */
    public List<UserGroupResponse> filterRootGroups(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .filter(this::isRoot)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter leaf groups (no children)
     */
    public List<UserGroupResponse> filterLeafGroups(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .filter(this::isLeaf)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter groups by parent ID
     */
    public List<UserGroupResponse> filterByParentId(List<UserGroup> userGroups, UUID parentId) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        if (parentId == null) {
            return filterRootGroups(userGroups);
        }

        return userGroups.stream()
                .filter(ug -> ug.getParentGroup() != null &&
                        parentId.equals(ug.getParentGroup().getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter groups by depth level
     */
    public List<UserGroupResponse> filterByDepth(List<UserGroup> userGroups, int depth) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .filter(ug -> calculateDepth(ug) == depth)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Sorting Utilities
    // ====================================

    /**
     * Sort groups by priority
     */
    public List<UserGroupResponse> sortByPriority(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .sorted(Comparator
                        .comparing((UserGroup ug) -> Optional.ofNullable(ug.getPriority()).orElse(Integer.MAX_VALUE))
                        .thenComparing(UserGroup::getName))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort groups by name
     */
    public List<UserGroupResponse> sortByName(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .sorted(Comparator.comparing(UserGroup::getName))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort groups by code
     */
    public List<UserGroupResponse> sortByCode(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .sorted(Comparator.comparing(UserGroup::getCode))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort groups by user count (descending)
     */
    public List<UserGroupResponse> sortByUserCount(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .sorted(Comparator
                        .comparing((UserGroup ug) -> getUserCount(ug))
                        .reversed()
                        .thenComparing(UserGroup::getName))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Sort groups hierarchically (breadth-first)
     */
    public List<UserGroupResponse> sortHierarchically(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        return userGroups.stream()
                .sorted(Comparator
                        .comparing(this::calculateDepth)
                        .thenComparing((UserGroup ug) ->
                                Optional.ofNullable(ug.getPriority()).orElse(Integer.MAX_VALUE))
                        .thenComparing(UserGroup::getName))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ====================================
    // Grouping Utilities
    // ====================================

    /**
     * Group by parent group
     */
    public Map<UUID, List<UserGroupResponse>> groupByParent(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyMap();
        }

        return userGroups.stream()
                .collect(Collectors.groupingBy(
                        ug -> Optional.ofNullable(ug.getParentGroup())
                                .map(UserGroup::getId)
                                .orElse(null),
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));
    }

    /**
     * Group by depth level
     */
    public Map<Integer, List<UserGroupResponse>> groupByDepth(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyMap();
        }

        return userGroups.stream()
                .collect(Collectors.groupingBy(
                        this::calculateDepth,
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));
    }

    /**
     * Group by active status
     */
    public Map<Boolean, List<UserGroupResponse>> groupByActiveStatus(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyMap();
        }

        return userGroups.stream()
                .collect(Collectors.groupingBy(
                        ug -> Boolean.TRUE.equals(ug.getActive()),
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));
    }

    // ====================================
    // Validation Helpers
    // ====================================

    /**
     * Validate group code format (uppercase letters, numbers, underscores)
     */
    protected boolean isValidGroupCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        // Must be uppercase letters, numbers, and underscores only
        return code.matches("^[A-Z0-9_]+$");
    }

    /**
     * Check if group can be deleted
     */
    protected boolean canDeleteGroup(UserGroup userGroup) {
        if (userGroup == null) {
            return false;
        }

        // Cannot delete if has users
        if (getUserCount(userGroup) > 0) {
            return false;
        }

        // Cannot delete if has child groups
        if (getChildGroupCount(userGroup) > 0) {
            return false;
        }

        return true;
    }

    /**
     * Check if setting parent would create circular reference
     */
    protected boolean wouldCreateCircularReference(UserGroup userGroup, UserGroup potentialParent) {
        if (userGroup == null || potentialParent == null) {
            return false;
        }

        // Cannot set self as parent
        if (userGroup.getId().equals(potentialParent.getId())) {
            return true;
        }

        // Cannot set descendant as parent
        return isDescendantOf(potentialParent, userGroup);
    }

    // ====================================
    // Statistics and Aggregations
    // ====================================

    /**
     * Calculate total users across all groups
     */
    public Integer calculateTotalUsers(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return 0;
        }

        return userGroups.stream()
                .mapToInt(this::getUserCount)
                .sum();
    }

    /**
     * Calculate total active users across all groups
     */
    public Long calculateTotalActiveUsers(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return 0L;
        }

        return userGroups.stream()
                .mapToLong(this::getActiveUserCount)
                .sum();
    }

    /**
     * Get group with most users
     */
    public UserGroupResponse getGroupWithMostUsers(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return null;
        }

        return userGroups.stream()
                .max(Comparator.comparing(this::getUserCount))
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     * Get maximum depth in hierarchy
     */
    public Integer getMaxDepth(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return 0;
        }

        return userGroups.stream()
                .mapToInt(this::calculateDepth)
                .max()
                .orElse(0);
    }

    /**
     * Count root groups
     */
    public Long countRootGroups(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return 0L;
        }

        return userGroups.stream()
                .filter(this::isRoot)
                .count();
    }

    /**
     * Count leaf groups
     */
    public Long countLeafGroups(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return 0L;
        }

        return userGroups.stream()
                .filter(this::isLeaf)
                .count();
    }

    // ====================================
    // Utility Methods for UI/Display
    // ====================================

    /**
     * Create flat list with indentation markers for tree display
     */
    public List<UserGroupFlatView> toFlatView(List<UserGroup> userGroups) {
        if (userGroups == null || userGroups.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserGroupFlatView> flatView = new ArrayList<>();

        // Start with root groups
        userGroups.stream()
                .filter(this::isRoot)
                .sorted(Comparator.comparing(ug ->
                        Optional.ofNullable(ug.getPriority()).orElse(Integer.MAX_VALUE)))
                .forEach(root -> addToFlatView(root, flatView, 0));

        return flatView;
    }

    /**
     * Helper method to recursively add groups to flat view
     */
    private void addToFlatView(UserGroup userGroup, List<UserGroupFlatView> flatView, int depth) {
        flatView.add(UserGroupFlatView.builder()
                .id(userGroup.getId())
                .code(userGroup.getCode())
                .name(userGroup.getName())
                .depth(depth)
                .indentation("  ".repeat(depth))
                .hasChildren(!isLeaf(userGroup))
                .userCount(getUserCount(userGroup))
                .active(userGroup.getActive())
                .build());

        // Add children recursively
        if (userGroup.getChildGroups() != null) {
            userGroup.getChildGroups().stream()
                    .sorted(Comparator.comparing(ug ->
                            Optional.ofNullable(ug.getPriority()).orElse(Integer.MAX_VALUE)))
                    .forEach(child -> addToFlatView(child, flatView, depth + 1));
        }
    }

    /**
     * Flat view DTO for tree-like display in tables
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserGroupFlatView implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private UUID id;
        private String code;
        private String name;
        private Integer depth;
        private String indentation;
        private Boolean hasChildren;
        private Integer userCount;
        private Boolean active;
    }

    /**
     * Create breadcrumb trail for navigation
     */
    public List<BreadcrumbItem> toBreadcrumbs(UserGroup userGroup) {
        if (userGroup == null) {
            return Collections.emptyList();
        }

        List<UserGroupMinimalInfo> path = getPath(userGroup);

        return path.stream()
                .map(info -> BreadcrumbItem.builder()
                        .id(info.getId())
                        .code(info.getCode())
                        .name(info.getName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Breadcrumb item DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BreadcrumbItem implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private UUID id;
        private String code;
        private String name;
    }
}