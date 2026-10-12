package com.example.demo.common.validator.validators;

import com.example.demo.common.validator.constraints.UniqueField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Validator for @UniqueField annotation
 *
 * Checks if a field value is unique in the database
 * Supports excluding current entity during updates
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
public class UniqueFieldValidator implements ConstraintValidator<UniqueField, Object> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApplicationContext applicationContext;

    private Class<?> entityClass;
    private String fieldName;
    private String repositoryName;
    private boolean excludeId;
    private String idFieldName;
    private boolean caseSensitive;
    private boolean allowNull;
    private String queryMethod;

    @Override
    public void initialize(UniqueField constraintAnnotation) {
        this.entityClass = constraintAnnotation.entity();
        this.fieldName = constraintAnnotation.field();
        this.repositoryName = constraintAnnotation.repository();
        this.excludeId = constraintAnnotation.excludeId();
        this.idFieldName = constraintAnnotation.idField();
        this.caseSensitive = constraintAnnotation.caseSensitive();
        this.allowNull = constraintAnnotation.allowNull();
        this.queryMethod = constraintAnnotation.queryMethod();

        log.debug("UniqueFieldValidator initialized for entity: {}, field: {}",
                entityClass.getSimpleName(), fieldName);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Allow null if configured
        if (value == null) {
            return allowNull;
        }

        try {
            // Use custom query method if provided
            if (!queryMethod.isEmpty()) {
                return validateWithCustomQuery(value, context);
            }

            // Get entity ID if excluding current entity
            Object entityId = null;
            if (excludeId) {
                entityId = extractIdFromRequest();
            }

            // Check uniqueness
            boolean exists = checkFieldExists(value, entityId);

            if (exists) {
                buildConstraintViolation(context,
                        String.format("Value '%s' already exists for field '%s'", value, fieldName));
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating unique field: {}", fieldName, e);
            buildConstraintViolation(context, "Unable to validate field uniqueness");
            return false;
        }
    }

    /**
     * Check if field value exists in database
     */
    private boolean checkFieldExists(Object value, Object excludeId) {
        try {
            StringBuilder jpql = new StringBuilder();
            jpql.append("SELECT COUNT(e) FROM ")
                    .append(entityClass.getSimpleName())
                    .append(" e WHERE ");

            // Case sensitivity
            if (caseSensitive) {
                jpql.append("e.").append(fieldName).append(" = :value");
            } else {
                jpql.append("LOWER(CAST(e.").append(fieldName).append(" AS string)) = LOWER(:value)");
            }

            // Exclude current entity
            if (excludeId != null) {
                jpql.append(" AND e.id != :excludeId");
            }

            TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
            query.setParameter("value", value.toString());

            if (excludeId != null) {
                query.setParameter("excludeId", excludeId);
            }

            Long count = query.getSingleResult();
            return count > 0;

        } catch (Exception e) {
            log.error("Error checking field existence", e);
            return false;
        }
    }

    /**
     * Validate using custom repository query method
     */
    private boolean validateWithCustomQuery(Object value, ConstraintValidatorContext context) {
        try {
            // Try to get repository bean
            JpaRepository<?, ?> repository = getRepository();
            if (repository == null) {
                log.warn("Repository not found for entity: {}", entityClass.getSimpleName());
                return true; // Skip validation if repository not found
            }

            // Use reflection to invoke custom query method
            Method method = findMethod(repository.getClass(), queryMethod, value.getClass());
            if (method == null) {
                log.warn("Query method '{}' not found in repository", queryMethod);
                return true;
            }

            Object result = method.invoke(repository, value);

            // Handle Optional return type
            if (result instanceof Optional) {
                return ((Optional<?>) result).isEmpty();
            }

            // Handle boolean return type
            if (result instanceof Boolean) {
                return !(Boolean) result;
            }

            return result == null;

        } catch (Exception e) {
            log.error("Error invoking custom query method: {}", queryMethod, e);
            return true; // Skip validation on error
        }
    }

    /**
     * Get repository bean
     */
    private JpaRepository<?, ?> getRepository() {
        try {
            if (!repositoryName.isEmpty()) {
                return (JpaRepository<?, ?>) applicationContext.getBean(repositoryName);
            }

            // Try to auto-detect repository
            String repoName = entityClass.getSimpleName() + "Repository";
            String repoNameLower = Character.toLowerCase(repoName.charAt(0)) + repoName.substring(1);

            try {
                return (JpaRepository<?, ?>) applicationContext.getBean(repoNameLower);
            } catch (Exception e) {
                // Try with capitalized name
                return (JpaRepository<?, ?>) applicationContext.getBean(repoName);
            }

        } catch (Exception e) {
            log.debug("Repository not found: {}", repositoryName, e);
            return null;
        }
    }

    /**
     * Extract entity ID from HTTP request path variable or request body
     */
    private Object extractIdFromRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return null;
            }

            HttpServletRequest request = attributes.getRequest();

            // Try to extract ID from path variable (e.g., /users/{id})
            String uri = request.getRequestURI();
            String[] pathSegments = uri.split("/");

            // Check if last segment is a valid ID (UUID or number)
            if (pathSegments.length > 0) {
                String lastSegment = pathSegments[pathSegments.length - 1];

                // Check if it's a UUID
                if (lastSegment.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                    return lastSegment;
                }

                // Check if it's a number
                if (lastSegment.matches("\\d+")) {
                    return Long.parseLong(lastSegment);
                }
            }

            return null;

        } catch (Exception e) {
            log.debug("Could not extract ID from request", e);
            return null;
        }
    }

    /**
     * Find method by name and parameter type
     */
    private Method findMethod(Class<?> clazz, String methodName, Class<?> paramType) {
        try {
            // Try exact match first
            return clazz.getMethod(methodName, paramType);
        } catch (NoSuchMethodException e) {
            // Try to find method with compatible parameter type
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName) &&
                        method.getParameterCount() == 1) {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (parameterType.isAssignableFrom(paramType) ||
                            paramType.isAssignableFrom(parameterType)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Build custom constraint violation message
     */
    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}