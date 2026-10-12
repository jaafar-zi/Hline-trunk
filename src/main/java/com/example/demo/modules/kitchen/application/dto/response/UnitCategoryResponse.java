package com.example.demo.modules.kitchen.application.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitCategoryResponse {
        private UUID id;
        private String code;
        private String name;
        private String slug;
        private String description;
        private String icon;
        private String imageUrl;
        private Boolean active;
        private Integer sortOrder;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        private Long version;
}
