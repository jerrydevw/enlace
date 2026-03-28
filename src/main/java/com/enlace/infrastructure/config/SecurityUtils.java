package com.enlace.infrastructure.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Utility class for accessing security context information.
 * Provides convenient methods to retrieve authenticated user information.
 */
public class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Gets the currently authenticated customer.
     *
     * @return CustomerAuthentication object
     * @throws SecurityException if user is not authenticated as a customer
     */
    public static CustomerAuthentication getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new SecurityException("No authentication found in security context");
        }

        if (!(auth instanceof CustomerAuthentication)) {
            throw new SecurityException("Current authentication is not a CustomerAuthentication");
        }

        return (CustomerAuthentication) auth;
    }

    /**
     * Gets the ID of the currently authenticated customer.
     *
     * @return UUID of the customer
     * @throws SecurityException if user is not authenticated as a customer
     */
    public static UUID getCurrentCustomerId() {
        return getCurrentCustomer().getCustomerId();
    }

    /**
     * Gets the currently authenticated viewer.
     *
     * @return ViewerAuthentication object
     * @throws SecurityException if user is not authenticated as a viewer
     */
    public static ViewerAuthentication getCurrentViewer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            throw new SecurityException("No authentication found in security context");
        }

        if (!(auth instanceof ViewerAuthentication)) {
            throw new SecurityException("Current authentication is not a ViewerAuthentication");
        }

        return (ViewerAuthentication) auth;
    }

    /**
     * Gets the ID of the currently authenticated viewer's session.
     *
     * @return UUID of the viewer session
     * @throws SecurityException if user is not authenticated as a viewer
     */
    public static UUID getCurrentViewerSessionId() {
        return getCurrentViewer().getSessionId();
    }

    /**
     * Checks if the current user is authenticated as a customer.
     *
     * @return true if authenticated as customer, false otherwise
     */
    public static boolean isCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth instanceof CustomerAuthentication;
    }

    /**
     * Checks if the current user is authenticated as a viewer.
     *
     * @return true if authenticated as viewer, false otherwise
     */
    public static boolean isViewer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth instanceof ViewerAuthentication;
    }

    /**
     * Checks if there is any authenticated user in the context.
     *
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
}
