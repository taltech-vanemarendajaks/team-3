package com.borsibaar.util;

import com.borsibaar.entity.User;
import com.borsibaar.exception.BadRequestException;
import com.borsibaar.exception.ForbiddenException;
import com.borsibaar.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for accessing security context and authenticated user information.
 * Uses custom exceptions for consistent error handling across the application.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the currently authenticated user from the SecurityContext.
     * Requires the user to have an organization.
     *
     * @return The authenticated User
     * @throws UnauthorizedException if user is not authenticated
     * @throws BadRequestException if user has no organization
     */
    public static User getCurrentUser() {
        return getCurrentUser(true);
    }

    /**
     * Gets the currently authenticated user from the SecurityContext.
     *
     * @param requireOrganization If true, throws exception if user has no organization
     * @return The authenticated User
     * @throws UnauthorizedException if user is not authenticated
     * @throws BadRequestException if requireOrganization is true and user has no organization
     */
    public static User getCurrentUser(boolean requireOrganization) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw UnauthorizedException.notAuthenticated();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw UnauthorizedException.invalidAuthentication();
        }

        User user = (User) principal;

        if (requireOrganization && user.getOrganizationId() == null) {
            throw BadRequestException.noOrganization();
        }

        return user;
    }

    /**
     * Gets the current user's organization ID.
     *
     * @return The organization ID
     * @throws UnauthorizedException if user is not authenticated
     * @throws BadRequestException if user has no organization
     */
    public static Long getCurrentOrganizationId() {
        return getCurrentUser().getOrganizationId();
    }

    /**
     * Gets the current user and verifies they are an admin.
     *
     * @return The authenticated admin User
     * @throws UnauthorizedException if user is not authenticated
     * @throws ForbiddenException if user is not an admin
     */
    public static User getCurrentAdmin() {
        User user = getCurrentUser();
        if (user.getRole() == null || !"ADMIN".equals(user.getRole().getName())) {
            throw ForbiddenException.adminRequired();
        }
        return user;
    }

    /**
     * Verifies that a resource belongs to the specified organization.
     *
     * @param resourceOrganizationId The organization ID of the resource
     * @param expectedOrganizationId The expected organization ID
     * @param resourceType The type of resource for error messages
     * @throws ForbiddenException if the resource doesn't belong to the expected organization
     */
    public static void requireOrganization(Long resourceOrganizationId, Long expectedOrganizationId, String resourceType) {
        if (!expectedOrganizationId.equals(resourceOrganizationId)) {
            throw ForbiddenException.resourceAccessDenied(resourceType);
        }
    }
}
