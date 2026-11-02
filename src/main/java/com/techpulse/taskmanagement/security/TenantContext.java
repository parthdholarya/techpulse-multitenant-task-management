package com.techpulse.taskmanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class TenantContext {

    private TenantContext() {
    }

    public static TenantPrincipal getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof TenantPrincipal)) {

            throw new IllegalStateException("User is not authenticated");
        }

        return (TenantPrincipal) authentication.getPrincipal();
    }

    public static Long getCurrentTenantId() {
        return getCurrentUser().tenantId();
    }

    public static String getCurrentEmail() {
        return getCurrentUser().email();
    }

    public static String getCurrentRole() {
        return getCurrentUser().role();
    }
}