
package com.techpulse.taskmanagement.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser() {

        TenantPrincipal principal = new TenantPrincipal(
                "user@techpulse.com",
                1L,
                "USER"
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        java.util.List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @Test
    void getCurrentUserSuccessfully() {

        setAuthenticatedUser();

        TenantPrincipal principal = TenantContext.getCurrentUser();

        assertNotNull(principal);
        assertEquals("user@techpulse.com", principal.email());
        assertEquals(1L, principal.tenantId());
        assertEquals("USER", principal.role());
    }

    @Test
    void getCurrentTenantIdSuccessfully() {

        setAuthenticatedUser();

        assertEquals(1L, TenantContext.getCurrentTenantId());
    }

    @Test
    void getCurrentEmailSuccessfully() {

        setAuthenticatedUser();

        assertEquals(
                "user@techpulse.com",
                TenantContext.getCurrentEmail()
        );
    }

    @Test
    void getCurrentRoleSuccessfully() {

        setAuthenticatedUser();

        assertEquals("USER", TenantContext.getCurrentRole());
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsMissing() {

        SecurityContextHolder.clearContext();

        assertThrows(
                IllegalStateException.class,
                TenantContext::getCurrentUser
        );
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticated() {

        var authentication =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        "user@techpulse.com",
                        "password"
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalStateException.class,
                TenantContext::getCurrentUser
        );
    }

    @Test
    void shouldThrowExceptionWhenPrincipalIsNotTenantPrincipal() {

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "user@techpulse.com",
                        null,
                        java.util.List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThrows(
                IllegalStateException.class,
                TenantContext::getCurrentUser
        );
    }
}
