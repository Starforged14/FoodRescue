package com.foodrescue.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigurationTest {

    private final SecurityConfiguration securityConfiguration = new SecurityConfiguration();

    @Test
    void corsAllowsOnlyConfiguredOriginsAndApiMethods() {
        CorsConfigurationSource source =
            securityConfiguration.corsConfigurationSource("https://app.example, https://admin.example");

        CorsConfiguration cors = source.getCorsConfiguration(apiRequest());

        assertEquals("https://app.example", cors.checkOrigin("https://app.example"));
        assertEquals("https://admin.example", cors.checkOrigin("https://admin.example"));
        assertNull(cors.checkOrigin("https://untrusted.example"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertFalse(Boolean.TRUE.equals(cors.getAllowCredentials()));
    }

    @Test
    void corsRejectsWildcardOrigins() {
        assertThrows(
            IllegalArgumentException.class,
            () -> securityConfiguration.corsConfigurationSource("*")
        );
    }

    @Test
    void corsDoesNotAllowCrossOriginRequestsByDefault() {
        CorsConfigurationSource source = securityConfiguration.corsConfigurationSource("");

        assertNull(source.getCorsConfiguration(apiRequest()).checkOrigin("https://app.example"));
    }

    private MockHttpServletRequest apiRequest() {
        return new MockHttpServletRequest("GET", "/api/v1/auth/login");
    }
}
