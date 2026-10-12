package com.example.demo.modules.user.domain.model.vo;

import java.util.Arrays;
import java.util.List;

import com.example.demo.modules.user.domain.model.Permission;

public enum DefaultPermission {

    // User Management
    READ_USER,
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,
    MANAGE_USER_STATUS,
    RESET_USER_PASSWORD,
    ASSIGN_USER_ROLE,
    VIEW_USER_LOGS,

    // Product Management
    READ_PRODUCT,
    CREATE_PRODUCT,
    UPDATE_PRODUCT,
    DELETE_PRODUCT,
    MANAGE_PRODUCT_STOCK,
    PUBLISH_PRODUCT,
    ARCHIVE_PRODUCT,

    // Role & Rights Management
    READ_ROLE,
    CREATE_ROLE,
    UPDATE_ROLE,
    DELETE_ROLE,
    ASSIGN_ROLE_PERMISSION,
    MANAGE_ROLE_USERS,

    // User Group Management
    READ_USER_GROUP,
    CREATE_USER_GROUP,
    UPDATE_USER_GROUP,
    DELETE_USER_GROUP,
    ASSIGN_USER_TO_GROUP,
    REMOVE_USER_FROM_GROUP,

    // System & Settings
    ACCESS_ADMIN_DASHBOARD,
    VIEW_AUDIT_LOGS,
    MANAGE_SYSTEM_SETTINGS,
    MANAGE_NOTIFICATIONS,
    VIEW_STATISTICS,

    // Data Access / Import-Export
    EXPORT_USER_DATA,
    EXPORT_PRODUCT_DATA,
    IMPORT_USER_DATA,
    IMPORT_PRODUCT_DATA;

    public static List<String> getDefaultPermissions() {
        return Arrays.stream(values())
                .map(Enum::name)
                .toList();
    }

    public static List<Permission> mapToPermissionEntities() {
        return Arrays.stream(values())
                .map(permissionEnum -> Permission.fromEnum(permissionEnum))
                .toList();
    }
}
