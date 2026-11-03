
package com.techpulse.taskmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthenticationFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        SecurityContextHolder.clearContext();
    }

    // 1. Valid JWT should authenticate the user
    @Test
    void validTokenShouldAuthenticateUser() throws Exception {

        Claims claims = mock(Claims.class);

        when(jwtService.extractClaims("valid-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("user@techpulse.com");

        when(claims.get("tenantId", Long.class))
                .thenReturn(1L);

        when(claims.get("role", String.class))
                .thenReturn("USER");

        request.addHeader(
                "Authorization",
                "Bearer valid-token"
        );

        jwtFilter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        TenantPrincipal principal =
                (TenantPrincipal) authentication.getPrincipal();

        assertEquals("user@techpulse.com", principal.email());
        assertEquals(1L, principal.tenantId());
        assertEquals("USER", principal.role());

        assertTrue(authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    // 2. Missing Authorization header
    @Test
    void missingTokenShouldContinueFilterChain() throws Exception {

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(200, response.getStatus());
    }

    // 3. Authorization header without Bearer prefix
    @Test
    void nonBearerTokenShouldContinueFilterChain() throws Exception {

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        jwtFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService);
    }

    // 4. Invalid JWT should return 401
    @Test
    void invalidTokenShouldReturnUnauthorized() throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        when(jwtService.extractClaims("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());

        assertTrue(response.getContentAsString()
                .contains("Invalid token claims"));

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    // 5. Missing tenant ID should return 401
    @Test
    void missingTenantIdShouldReturnUnauthorized() throws Exception {

        Claims claims = mock(Claims.class);

        when(jwtService.extractClaims("invalid-claims-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("user@techpulse.com");

        when(claims.get("tenantId", Long.class))
                .thenReturn(null);

        when(claims.get("role", String.class))
                .thenReturn("USER");

        request.addHeader(
                "Authorization",
                "Bearer invalid-claims-token"
        );

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    // 6. Invalid role should return 401
    @Test
    void invalidRoleShouldReturnUnauthorized() throws Exception {

        Claims claims = mock(Claims.class);

        when(jwtService.extractClaims("invalid-role-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("user@techpulse.com");

        when(claims.get("tenantId", Long.class))
                .thenReturn(1L);

        when(claims.get("role", String.class))
                .thenReturn("SUPER_ADMIN");

        request.addHeader(
                "Authorization",
                "Bearer invalid-role-token"
        );

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    // 7. Zero tenant ID should return 401
    @Test
    void zeroTenantIdShouldReturnUnauthorized() throws Exception {

        Claims claims = mock(Claims.class);

        when(jwtService.extractClaims("zero-tenant-token"))
                .thenReturn(claims);

        when(claims.getSubject())
                .thenReturn("user@techpulse.com");

        when(claims.get("tenantId", Long.class))
                .thenReturn(0L);

        when(claims.get("role", String.class))
                .thenReturn("USER");

        request.addHeader(
                "Authorization",
                "Bearer zero-tenant-token"
        );

        jwtFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }
}
