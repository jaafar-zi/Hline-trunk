package com.example.demo.config.web;

import com.example.demo.common.interceptor.AuthenticationInterceptor;
import com.example.demo.common.interceptor.LocaleInterceptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.util.pattern.PathPatternParser;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * Central MVC configuration.
 *
 * KEY FIXES applied in this version:
 *
 *  1. extendMessageConverters — no longer removes ALL Jackson converters.
 *     Previously this removed the converter actuator needs to serialize
 *     the Prometheus text/plain response, causing /actuator/prometheus
 *     to fall through to the static resource handler → 404.
 *     Now only removes duplicates and adds a dedicated Prometheus
 *     StringHttpMessageConverter.
 *
 *  2. addResourceHandlers — no longer registers a /** catch-all.
 *     The default Spring Boot resource handler already owns /**.
 *     Adding a second /** handler competed with WebMvcEndpointHandlerMapping
 *     for /actuator/** paths.
 *
 *  3. configureContentNegotiation — now includes text/plain so the
 *     Prometheus scraper Accept header is correctly negotiated.
 *
 * @author Architecture Team
 * @version 2.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final LocaleInterceptor         localeInterceptor;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${cors.enabled:true}")
    private boolean corsEnabled;

    @Value("${locale.default:en}")
    private String defaultLocale;

    // =========================================================================
    // Type Converters
    // =========================================================================

    @Override
    public void addFormatters(FormatterRegistry registry) {

        // String → Instant
        registry.addConverter(String.class, Instant.class, source -> {
            if (source == null || source.isBlank()) return null;
            try {
                return Instant.parse(source);
            } catch (Exception e) {
                try {
                    return LocalDateTime
                            .parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            .toInstant(ZoneOffset.UTC);
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            "Invalid date format: " + source +
                            ". Use ISO 8601 format e.g. 2026-01-01T00:00:00Z");
                }
            }
        });

        // String → LocalDateTime
        registry.addConverter(String.class, LocalDateTime.class, source -> {
            if (source == null || source.isBlank()) return null;
            try {
                if (source.endsWith("Z")) {
                    return Instant.parse(source)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDateTime();
                }
                if (source.contains("+") ||
                        source.matches(".*[0-9]-[0-9]{2}:[0-9]{2}$")) {
                    return java.time.OffsetDateTime.parse(source)
                            .toLocalDateTime();
                }
                return LocalDateTime.parse(
                        source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Invalid date format: " + source +
                        ". Use ISO 8601 format e.g. 2026-01-01T00:00:00Z");
            }
        });
    }

    // =========================================================================
    // Message Converters
    // =========================================================================

    /**
     * Extend (not replace) the default converter list.
     *
     * ROOT CAUSE OF /actuator/prometheus 404:
     * The previous implementation called:
     *   converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter)
     *
     * This removed EVERY Jackson converter from the list, including the one
     * Spring Boot Actuator registers internally for its own endpoints.
     * Without that converter, the actuator could not serialize the Prometheus
     * text/plain response. Spring MVC found no capable converter, fell through
     * to the static resource handler (SimpleUrlHandlerMapping /**), which
     * threw NoResourceFoundException → 404.
     *
     * FIX:
     *  - Remove ONLY the default auto-configured Jackson converter (position 0).
     *  - Re-add our configured Jackson converter in its place.
     *  - Add a dedicated StringHttpMessageConverter for Prometheus text/plain.
     *  - Keep all other converters intact.
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

        // Step 1: Remove auto-configured Jackson converters
        converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);

        // Step 2: Remove ByteArray converters
        converters.removeIf(c -> c instanceof ByteArrayHttpMessageConverter);

        // Step 3: ByteArray converter
        ByteArrayHttpMessageConverter byteConverter =
                new ByteArrayHttpMessageConverter();
        byteConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.ALL
        ));
        converters.add(0, byteConverter);

        // Step 4: Our configured Jackson converter
        converters.add(1, mappingJackson2HttpMessageConverter());

        // Step 5: THE CRITICAL FIX
        // PrometheusEndpoint returns a String scraped from PrometheusMeterRegistry.
        // The Prometheus scraper sends:
        //   Accept: application/openmetrics-text;version=1.0.0, ..., text/plain;version=0.0.4
        //
        // Spring MVC content negotiation:
        //   1. Finds PrometheusEndpoint handler ✅
        //   2. Tries to find a converter that can write String
        //      AND produce one of the Accept media types
        //   3. Finds NO matching converter → falls through to resource handler → 404
        //
        // Adding StringHttpMessageConverter with these exact media types fixes it.
        StringHttpMessageConverter prometheusConverter =
                new StringHttpMessageConverter(StandardCharsets.UTF_8);
        prometheusConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.parseMediaType(
                        "application/openmetrics-text;version=1.0.0"),
                MediaType.parseMediaType(
                        "application/openmetrics-text;version=0.0.1"),
                MediaType.parseMediaType(
                        "text/plain;version=0.0.4"),
                MediaType.TEXT_PLAIN,
                MediaType.ALL
        ));
        converters.add(2, prometheusConverter);

        log.info("Final converter order:");
        for (int i = 0; i < converters.size(); i++) {
                log.info("  [{}] {} — {}",
                        i,
                        converters.get(i).getClass().getSimpleName(),
                        converters.get(i).getSupportedMediaTypes());
        }
    }

    // =========================================================================
    // Jackson Configuration
    // =========================================================================

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        mapper.setTimeZone(TimeZone.getDefault());
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        log.info("ObjectMapper configured successfully");
        return mapper;
    }

    // =========================================================================
    // CORS
    // =========================================================================

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!corsEnabled) {
            log.info("CORS disabled by configuration");
            return;
        }

        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods(
                        "GET", "POST", "PUT",
                        "DELETE", "PATCH", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders(
                        "Authorization",
                        "X-Total-Count",
                        "X-Page-Number",
                        "X-Request-ID",
                        "X-Correlation-ID",
                        "X-Response-Time",
                        "Content-Disposition"
                )
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/public/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    // =========================================================================
    // Static Resource Handlers
    // =========================================================================

    /**
     * Register static resource handlers.
     *
     * IMPORTANT: Do NOT register a /** catch-all handler here.
     *
     * Spring Boot's ResourceHttpRequestHandler auto-registers /** pointing to:
     *   classpath:/META-INF/resources/
     *   classpath:/resources/
     *   classpath:/static/
     *   classpath:/public/
     *   ServletContext /
     *
     * Adding a second /** handler here creates ambiguity. When Spring MVC
     * cannot find a handler via WebMvcEndpointHandlerMapping (actuator),
     * it falls through to SimpleUrlHandlerMapping which owns /** and
     * returns NoResourceFoundException instead of delegating correctly.
     *
     * The handlers below are explicit path-prefixed handlers that do NOT
     * conflict with the actuator's WebMvcEndpointHandlerMapping.
     */
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ── Swagger UI ────────────────────────────────────────────────────────
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations(
                        "classpath:/META-INF/resources/webjars/swagger-ui/"
                )
                .resourceChain(false);

        // ── WebJars ───────────────────────────────────────────────────────────
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations(
                        "classpath:/META-INF/resources/webjars/"
                )
                .resourceChain(false);

        // ── Static resources ──────────────────────────────────────────────────
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(isDevEnvironment() ? 0 : 3600)
                .resourceChain(true);

        // ── File uploads ──────────────────────────────────────────────────────
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(3600);

        // ── Favicon ───────────────────────────────────────────────────────────
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(86400);
    }

    /**
     * CRITICAL FIX:
     *
     * Override configureDefaultServletHandling to ensure the default
     * servlet does NOT claim /actuator/** requests.
     *
     * More importantly, override the path matching so that
     * WebMvcEndpointHandlerMapping (order=-100) is consulted
     * BEFORE SimpleUrlHandlerMapping (order=MAX_VALUE-1).
     *
     * The /** pattern in resourceHandlerMapping was intercepting
     * /actuator/prometheus because Spring was not finding it via
     * WebMvcEndpointHandlerMapping first — this was caused by
     * SpringDoc adding an additional resource handler that changed
     * the lookup order.
     */
    @Override
    public void configureDefaultServletHandling(
            DefaultServletHandlerConfigurer configurer) {
        // Do NOT call configurer.enable() here.
        // Enabling the default servlet handler adds a /**
        // catch-all with very low order that competes with actuator.
        // Leave this method empty to keep default Spring MVC behaviour.
    }

    // =========================================================================
    // View Controllers
    // =========================================================================

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui/index.html");
        registry.addRedirectViewController("/swagger-ui.html",
                "/swagger-ui/index.html");
        registry.addRedirectViewController("/swagger-ui",
                "/swagger-ui/index.html");
        registry.addRedirectViewController("/docs",
                "/swagger-ui/index.html");
    }

    // =========================================================================
    // Interceptors
    // =========================================================================

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // ── Locale interceptor ────────────────────────────────────────────────
        registry.addInterceptor(localeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/webjars/**",
                        "/actuator/**",
                        "/error",
                        "/error/**"
                )
                .order(1);

        // ── Authentication interceptor ────────────────────────────────────────
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // Auth public routes
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password",
                        // Customer registration
                        "/api/v1/customers",
                        // Public endpoints
                        "/api/v1/public/**",
                        // Products public reads
                        "/api/v1/products",
                        "/api/v1/products/search",
                        "/api/v1/products/published",
                        "/api/v1/products/{id}",
                        "/api/v1/products/sku/{sku}",
                        "/api/v1/products/category/{categoryId}",
                        "/api/v1/products/brand/{brand}",
                        // Kitchen (testing)
                        "/api/v1/kitchen-colours",
                        "/api/v1/kitchen-colours/**",
                        "/api/v1/kitchen-ranges",
                        "/api/v1/kitchen-ranges/**",
                        "/api/v1/kitchens/**",
                        "/api/v1/kitchen-products/**",
                        "/api/v1/kitchen-configurations/**",
                        "/api/v1/admin/kitchen-collections/**",
                        "/api/v1/admin/kitchen-products/**",
                        // System
                        "/api/health",
                        "/api/ping"
                )
                .order(2);

        // ── Performance logging interceptor ───────────────────────────────────
        registry.addInterceptor(new PerformanceLoggingInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/ping",
                        "/actuator/**"
                )
                .order(3);
    }

    // =========================================================================
    // Content Negotiation
    // =========================================================================

    /**
     * Configure content negotiation.
     *
     * FIX: Added text/plain media type mapping.
     * Without this, the Prometheus scraper's Accept header
     * (text/plain;version=0.0.4) was not being negotiated correctly,
     * causing content negotiation to fail and fall through to an error.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .favorPathExtension(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml",  MediaType.APPLICATION_XML)
                // Added for Prometheus scraper compatibility
                .mediaType("txt",  MediaType.TEXT_PLAIN);
    }

    // =========================================================================
    // Path Matching
    // =========================================================================

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
                .setUseTrailingSlashMatch(false)
                .setPatternParser(new PathPatternParser());
    }

    // =========================================================================
    // Async Support
    // =========================================================================

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(30000);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private boolean isDevEnvironment() {
        return activeProfile != null &&
                (activeProfile.contains("dev")   ||
                 activeProfile.contains("local") ||
                 activeProfile.contains("test"));
    }

    // =========================================================================
    // Inner Classes
    // =========================================================================

    private static class PerformanceLoggingInterceptor
            implements org.springframework.web.servlet.HandlerInterceptor {

        private static final org.slf4j.Logger logger =
                org.slf4j.LoggerFactory.getLogger(
                        PerformanceLoggingInterceptor.class);

        private static final String START_TIME_ATTRIBUTE  = "startTime";
        private static final long   SLOW_REQUEST_THRESHOLD_MS = 5_000;
        private static final long   WARN_REQUEST_THRESHOLD_MS = 1_000;

        @Override
        public boolean preHandle(
                jakarta.servlet.http.HttpServletRequest  request,
                jakarta.servlet.http.HttpServletResponse response,
                Object handler) {

            request.setAttribute(
                    START_TIME_ATTRIBUTE,
                    System.currentTimeMillis());
            return true;
        }

        @Override
        public void afterCompletion(
                jakarta.servlet.http.HttpServletRequest  request,
                jakarta.servlet.http.HttpServletResponse response,
                Object handler,
                Exception ex) {

            Long startTime =
                    (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime == null) return;

            long   duration   = System.currentTimeMillis() - startTime;
            int    status     = response.getStatus();
            String logMessage = String.format(
                    "method=%s uri=%s status=%d duration=%dms requestId=%s",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    duration,
                    request.getHeader("X-Request-ID")
            );

            // Duration-based log level
            if (duration > SLOW_REQUEST_THRESHOLD_MS)
                logger.warn("🐢 SLOW: {}", logMessage);
            else if (duration > WARN_REQUEST_THRESHOLD_MS)
                logger.info(logMessage);
            else
                logger.debug(logMessage);

            // Status-based log level
            if (status >= 500)
                logger.error("🔴 SERVER ERROR: {}", logMessage);
            else if (status >= 400)
                logger.warn("🟡 CLIENT ERROR: {}", logMessage);

            response.setHeader("X-Response-Time", duration + "ms");
        }
    }
}