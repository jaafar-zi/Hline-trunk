package com.example.demo.common.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.*;

/**
 * Locale and Internationalization Interceptor
 *
 * Features:
 * - Multi-source locale detection (header, cookie, session, parameter)
 * - Locale resolution priority
 * - Locale persistence
 * - Supported locale validation
 * - Default locale fallback
 * - Locale change tracking
 * - Cookie-based locale storage
 * - Session-based locale storage
 * - User preference integration
 *
 * @author Architecture Team
 * @version 1.0
 */
@Slf4j
@Component
public class LocaleInterceptor implements HandlerInterceptor {

    @Value("${locale.default:en}")
    private String defaultLocale;

    @Value("${locale.cookie.name:LOCALE}")
    private String localeCookieName;

    @Value("${locale.cookie.max-age:31536000}")
    private int cookieMaxAge; // 1 year

    @Value("${locale.param.name:lang}")
    private String localeParamName;

    // Supported locales
    private static final Set<String> SUPPORTED_LOCALES = new HashSet<>(Arrays.asList(
            "en", "en_US", "en_GB",
            "es", "es_ES", "es_MX",
            "fr", "fr_FR", "fr_CA",
            "de", "de_DE",
            "it", "it_IT",
            "pt", "pt_BR", "pt_PT",
            "zh", "zh_CN", "zh_TW",
            "ja", "ja_JP",
            "ko", "ko_KR",
            "ar", "ar_SA",
            "ru", "ru_RU",
            "hi", "hi_IN"
    ));

    // Locale resolution sources in priority order
    private static final String SOURCE_PARAMETER = "PARAMETER";
    private static final String SOURCE_COOKIE = "COOKIE";
    private static final String SOURCE_SESSION = "SESSION";
    private static final String SOURCE_HEADER = "HEADER";
    private static final String SOURCE_USER_PREFERENCE = "USER_PREFERENCE";
    private static final String SOURCE_DEFAULT = "DEFAULT";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Resolve locale from various sources
        LocaleResolutionResult result = resolveLocale(request);

        Locale locale = result.getLocale();

        // Set locale in LocaleContextHolder
        LocaleContextHolder.setLocale(locale);

        // Set locale in request attribute
        request.setAttribute("currentLocale", locale);
        request.setAttribute("localeSource", result.getSource());

        // Persist locale if it was explicitly changed
        if (shouldPersistLocale(result.getSource())) {
            persistLocale(request, response, locale);
        }

        // Add locale info to response headers
        response.setHeader("Content-Language", locale.toLanguageTag());

        log.debug("Locale resolved: {} (source: {})", locale, result.getSource());

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {

        // Add locale information to model if ModelAndView exists
        if (modelAndView != null && !modelAndView.wasCleared()) {
            Locale currentLocale = LocaleContextHolder.getLocale();
            modelAndView.addObject("currentLocale", currentLocale);
            modelAndView.addObject("supportedLocales", getSupportedLocalesList());
            modelAndView.addObject("localeDisplayName", currentLocale.getDisplayName(currentLocale));
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // Clear locale from thread-local after request completion
        LocaleContextHolder.resetLocaleContext();
    }

    /**
     * Resolve locale from multiple sources with priority
     */
    private LocaleResolutionResult resolveLocale(HttpServletRequest request) {
        Locale locale;
        String source;

        // 1. Check query parameter (highest priority for explicit changes)
        locale = getLocaleFromParameter(request);
        if (locale != null) {
            source = SOURCE_PARAMETER;
            log.debug("Locale from parameter: {}", locale);
            return new LocaleResolutionResult(locale, source);
        }

        // 2. Check user preferences (if user is authenticated)
        locale = getLocaleFromUserPreference(request);
        if (locale != null) {
            source = SOURCE_USER_PREFERENCE;
            log.debug("Locale from user preference: {}", locale);
            return new LocaleResolutionResult(locale, source);
        }

        // 3. Check cookie
        locale = getLocaleFromCookie(request);
        if (locale != null) {
            source = SOURCE_COOKIE;
            log.debug("Locale from cookie: {}", locale);
            return new LocaleResolutionResult(locale, source);
        }

        // 4. Check session
        locale = getLocaleFromSession(request);
        if (locale != null) {
            source = SOURCE_SESSION;
            log.debug("Locale from session: {}", locale);
            return new LocaleResolutionResult(locale, source);
        }

        // 5. Check Accept-Language header
        locale = getLocaleFromHeader(request);
        if (locale != null) {
            source = SOURCE_HEADER;
            log.debug("Locale from header: {}", locale);
            return new LocaleResolutionResult(locale, source);
        }

        // 6. Use default locale
        locale = parseLocale(defaultLocale);
        source = SOURCE_DEFAULT;
        log.debug("Using default locale: {}", locale);
        return new LocaleResolutionResult(locale, source);
    }

    /**
     * Get locale from query parameter
     */
    private Locale getLocaleFromParameter(HttpServletRequest request) {
        String localeParam = request.getParameter(localeParamName);

        if (StringUtils.hasText(localeParam)) {
            Locale locale = parseLocale(localeParam);
            if (isLocaleSupported(locale)) {
                return locale;
            }
        }

        return null;
    }

    /**
     * Get locale from user preferences (database)
     */
    private Locale getLocaleFromUserPreference(HttpServletRequest request) {
        // Get authenticated user's locale preference
        // This is a placeholder - implement based on your user management system

        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr != null) {
            // Fetch user's preferred locale from database
            String userLocale = fetchUserLocalePreference((Long) userIdAttr);
            if (StringUtils.hasText(userLocale)) {
                Locale locale = parseLocale(userLocale);
                if (isLocaleSupported(locale)) {
                    return locale;
                }
            }
        }

        return null;
    }

    /**
     * Get locale from cookie
     */
    private Locale getLocaleFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (localeCookieName.equals(cookie.getName())) {
                    String localeValue = cookie.getValue();
                    if (StringUtils.hasText(localeValue)) {
                        Locale locale = parseLocale(localeValue);
                        if (isLocaleSupported(locale)) {
                            return locale;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get locale from session
     */
    private Locale getLocaleFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Object localeAttr = session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            if (localeAttr instanceof Locale) {
                Locale locale = (Locale) localeAttr;
                if (isLocaleSupported(locale)) {
                    return locale;
                }
            }
        }

        return null;
    }

    /**
     * Get locale from Accept-Language header
     */
    private Locale getLocaleFromHeader(HttpServletRequest request) {
        Enumeration<Locale> locales = request.getLocales();

        while (locales.hasMoreElements()) {
            Locale locale = locales.nextElement();

            // Try exact match first
            if (isLocaleSupported(locale)) {
                return locale;
            }

            // Try language-only match
            Locale languageOnlyLocale = new Locale(locale.getLanguage());
            if (isLocaleSupported(languageOnlyLocale)) {
                return languageOnlyLocale;
            }
        }

        return null;
    }

    /**
     * Parse locale string to Locale object
     */
    private Locale parseLocale(String localeString) {
        if (!StringUtils.hasText(localeString)) {
            return null;
        }

        // Handle underscore and hyphen separators
        localeString = localeString.replace("-", "_");

        String[] parts = localeString.split("_");

        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length >= 3) {
            return new Locale(parts[0], parts[1], parts[2]);
        }

        return null;
    }

    /**
     * Check if locale is supported
     */
    private boolean isLocaleSupported(Locale locale) {
        if (locale == null) {
            return false;
        }

        String localeString = locale.toString();
        String languageOnly = locale.getLanguage();

        return SUPPORTED_LOCALES.contains(localeString) ||
                SUPPORTED_LOCALES.contains(languageOnly);
    }

    /**
     * Check if locale should be persisted
     */
    private boolean shouldPersistLocale(String source) {
        // Persist locale if it was explicitly set via parameter or user preference
        return SOURCE_PARAMETER.equals(source) || SOURCE_USER_PREFERENCE.equals(source);
    }

    /**
     * Persist locale to cookie and session
     */
    private void persistLocale(HttpServletRequest request,
                               HttpServletResponse response,
                               Locale locale) {
        // Save to cookie
        Cookie localeCookie = new Cookie(localeCookieName, locale.toString());
        localeCookie.setPath("/");
        localeCookie.setMaxAge(cookieMaxAge);
        localeCookie.setHttpOnly(true);
        localeCookie.setSecure(request.isSecure());
        response.addCookie(localeCookie);

        // Save to session
        HttpSession session = request.getSession(true);
        session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);

        log.debug("Persisted locale: {}", locale);
    }

    /**
     * Get list of supported locales
     */
    private List<LocaleInfo> getSupportedLocalesList() {
        List<LocaleInfo> locales = new ArrayList<>();

        for (String localeString : SUPPORTED_LOCALES) {
            Locale locale = parseLocale(localeString);
            if (locale != null) {
                locales.add(new LocaleInfo(
                        locale.toString(),
                        locale.getDisplayName(locale),
                        locale.getDisplayName(Locale.ENGLISH),
                        locale.getLanguage()
                ));
            }
        }

        // Sort by display name
        locales.sort(Comparator.comparing(LocaleInfo::getDisplayName));

        return locales;
    }

    /**
     * Fetch user's locale preference from database
     * Placeholder - implement based on your user management
     */
    private String fetchUserLocalePreference(Long userId) {
        // Implement database lookup
        return null;
    }

    /**
     * Locale resolution result
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class LocaleResolutionResult {
        private Locale locale;
        private String source;
    }

    /**
     * Locale information for UI
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class LocaleInfo {
        private String code;
        private String displayName;
        private String displayNameEnglish;
        private String language;
    }
}