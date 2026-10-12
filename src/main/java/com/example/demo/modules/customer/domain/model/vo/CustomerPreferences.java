package com.example.demo.modules.customer.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;

/**
 * Value Object representing customer preferences
 * 
 * Immutable configuration for customer experience
 * 
 * @author Customer Domain Team
 */
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public final class CustomerPreferences implements Serializable {
    
    private final String language;
    private final String currency;
    private final boolean emailNotifications;
    private final boolean smsNotifications;
    private final boolean marketingEmails;

    /**
     * Create default preferences
     */
    public static CustomerPreferences defaultPreferences() {
        return CustomerPreferences.builder()
            .language("en")
            .currency("USD")
            .emailNotifications(true)
            .smsNotifications(false)
            .marketingEmails(false)
            .build();
    }

    protected CustomerPreferences() {
        this.language           = null;
        this.currency           = null;
        this.emailNotifications = false;
        this.smsNotifications   = false;
        this.marketingEmails    = false;
    }
    
    /**
     * Validate and create preferences
     */
    public static CustomerPreferences of(
            String language,
            String currency,
            boolean emailNotifications,
            boolean smsNotifications,
            boolean marketingEmails) {
        
        validateLanguage(language);
        validateCurrency(currency);
        
        return new CustomerPreferences(
            language,
            currency,
            emailNotifications,
            smsNotifications,
            marketingEmails
        );
    }
    
    private static void validateLanguage(String language) {
        if (language == null || language.isBlank()) {
            throw new IllegalArgumentException("Language cannot be empty");
        }
        
        try {
            Locale.forLanguageTag(language);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid language code: " + language);
        }
    }
    
    private static void validateCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency cannot be empty");
        }
        
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + currency);
        }
    }
    
    /**
     * Check if any notifications are enabled
     */
    public boolean hasNotificationsEnabled() {
        return emailNotifications || smsNotifications;
    }
    
    /**
     * Create a copy with updated notification preferences
     */
    public CustomerPreferences withNotifications(boolean email, boolean sms, boolean marketing) {
        return new CustomerPreferences(
            this.language,
            this.currency,
            email,
            sms,
            marketing
        );
    }
    
    /**
     * Create a copy with updated language
     */
    public CustomerPreferences withLanguage(String language) {
        validateLanguage(language);
        return new CustomerPreferences(
            language,
            this.currency,
            this.emailNotifications,
            this.smsNotifications,
            this.marketingEmails
        );
    }
    
    /**
     * Create a copy with updated currency
     */
    public CustomerPreferences withCurrency(String currency) {
        validateCurrency(currency);
        return new CustomerPreferences(
            this.language,
            currency,
            this.emailNotifications,
            this.smsNotifications,
            this.marketingEmails
        );
    }

    @Override
    public String toString() {
        return String.format("Preferences[lang=%s, curr=%s, email=%s, sms=%s, marketing=%s]",
            language, currency, emailNotifications, smsNotifications, marketingEmails);
    }
}