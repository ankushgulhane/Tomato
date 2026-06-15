package com.tomato.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AccessDeniedException("No authenticated user");
        }
        return principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static void requireSelfOrAdmin(Long userId) {
        UserPrincipal current = getCurrentUser();
        boolean isAdmin = current.getRole() == com.tomato.model.Role.ADMIN;
        if (!isAdmin && !current.getId().equals(userId)) {
            throw new AccessDeniedException("Not allowed to access another user's resource");
        }
    }
}
