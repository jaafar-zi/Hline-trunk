package com.example.demo.modules.user.application.mapper;

import com.example.demo.modules.user.application.dto.request.UpdateUserProfileRequest;
import com.example.demo.modules.user.domain.model.vo.UserProfile;
import org.mapstruct.*;

/**
 * Mapper for UserProfile value object
 *
 * @author Architecture Team
 * @version 1.0
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface UserProfileMapper {

    /**
     * Map UpdateUserProfileRequest to UserProfile
     */
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    UserProfile toUserProfile(UpdateUserProfileRequest request);

    /**
     * Update existing UserProfile from request
     */
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    UserProfile updateFromRequest(UpdateUserProfileRequest request, @MappingTarget UserProfile profile);
}