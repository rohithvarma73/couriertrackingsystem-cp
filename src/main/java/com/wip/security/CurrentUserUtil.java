package com.wip.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Static utility class for resolving the currently authenticated user from the Spring Security context.
 *
 * <p>This utility provides a convenient, centralised API for accessing authentication details
 * of the user who is currently logged in to the application. All methods read from the
 * {@link SecurityContextHolder}, which Spring Security populates on each request thread
 * after successful authentication.</p>
 *
 * <p>Methods in this class are stateless and thread-safe because they read from the
 * inherently thread-local {@link SecurityContextHolder} on every invocation. This class
 * is not intended to be instantiated; all members are {@code static}.</p>
 *
 * <p>Typical usages include controllers and service layers that need to scope data access
 * or business logic to the currently authenticated user without explicitly passing the
 * principal through method parameters.</p>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 * @see SecurityContextHolder
 * @see CustomUserDetails
 */
public class CurrentUserUtil {

    /**
     * Retrieves the username of the currently authenticated user.
     *
     * <p>Reads the {@link Authentication} object from the current thread's
     * {@link SecurityContextHolder}. If no authentication exists or the
     * authentication is not in an authenticated state, {@code null} is returned.
     * Otherwise, the principal name (username) is returned.</p>
     *
     * @return the username of the currently authenticated user as a {@link String},
     *         or {@code null} if there is no active authentication on the current thread
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Retrieves the role of the currently authenticated user.
     *
     * <p>Reads the {@link Authentication} object from the {@link SecurityContextHolder}
     * and checks whether the principal is an instance of {@link CustomUserDetails}.
     * If so, the raw role string (e.g., {@code "ADMIN"} or {@code "USER"}) is returned
     * via {@link CustomUserDetails#getRole()}. If the principal is not a
     * {@link CustomUserDetails} instance or if no authentication exists, {@code null}
     * is returned.</p>
     *
     * @return the role string of the currently authenticated user (e.g., {@code "ADMIN"}
     *         or {@code "USER"}), or {@code null} if no authenticated {@link CustomUserDetails}
     *         principal is present
     */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getRole();
        }
        return null;
    }

    /**
     * Determines whether the currently authenticated user has the {@code ADMIN} role.
     *
     * <p>Delegates to {@link #getCurrentUserRole()} and checks whether the returned
     * role is equal to the string {@code "ADMIN"}. Returns {@code false} if the user
     * is not authenticated, if the principal is not a {@link CustomUserDetails}, or if
     * the user's role is anything other than {@code "ADMIN"}.</p>
     *
     * @return {@code true} if the currently authenticated user's role is {@code "ADMIN"},
     *         {@code false} otherwise
     */
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role);
    }

    /**
     * Retrieves the unique internal user ID of the currently authenticated user.
     *
     * <p>Reads the {@link Authentication} object from the {@link SecurityContextHolder}
     * and checks whether the principal is an instance of {@link CustomUserDetails}.
     * If so, the {@code Long} primary key of the underlying {@link com.wip.entity.AppUser}
     * is returned via {@link CustomUserDetails#getUserId()}. If the principal is not a
     * {@link CustomUserDetails} instance or if no authentication exists, {@code null}
     * is returned.</p>
     *
     * @return the {@code Long} primary key (user ID) of the currently authenticated user,
     *         or {@code null} if no authenticated {@link CustomUserDetails} principal is present
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUserId();
        }
        return null;
    }
}
