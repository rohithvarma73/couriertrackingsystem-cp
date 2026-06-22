package com.wip.security;

import com.wip.entity.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom implementation of Spring Security's {@link UserDetails} for the Courier Tracking System.
 *
 * <p>This class serves as the bridge between the application's {@link AppUser} domain entity
 * and the Spring Security authentication framework. It wraps an {@link AppUser} instance and
 * exposes the fields required by Spring Security (username, password, and granted authorities)
 * alongside application-specific accessors for the user's internal ID, associated customer ID,
 * and role string.</p>
 *
 * <p>The granted authority is derived from the {@link AppUser#getRole()} value by prepending
 * the {@code ROLE_} prefix required by Spring Security's role-based access control (e.g., a
 * role of {@code "ADMIN"} becomes the authority {@code "ROLE_ADMIN"}).</p>
 *
 * <p>All account-status flags ({@code accountNonExpired}, {@code accountNonLocked},
 * {@code credentialsNonExpired}, and {@code enabled}) unconditionally return {@code true},
 * meaning all accounts are considered active and valid at all times.</p>
 *
 * @author Dharshan K S (dharshan.ks@wipro.com)
 * @version 1.0
 * @since 1.0
 * @see AppUser
 * @see UserDetails
 */
public class CustomUserDetails implements UserDetails {

    /**
     * The underlying application user entity whose data is exposed to Spring Security.
     */
    private final AppUser user;

    /**
     * Constructs a new {@code CustomUserDetails} wrapping the given {@link AppUser}.
     *
     * @param user the application user entity to wrap; must not be {@code null}
     */
    public CustomUserDetails(AppUser user) {
        this.user = user;
    }

    /**
     * Returns the unique internal identifier of the authenticated user.
     *
     * <p>This is the primary key ({@code userId}) of the {@link AppUser} entity
     * and can be used to perform user-specific data queries within the application.</p>
     *
     * @return the {@code Long} primary key of the wrapped {@link AppUser}
     */
    public Long getUserId() {
        return user.getUserId();
    }

    /**
     * Returns the unique identifier of the customer profile associated with this user, if any.
     *
     * <p>A user may or may not have a linked customer profile. If the {@link AppUser} has
     * an associated {@code Customer} entity, its {@code customerId} is returned;
     * otherwise {@code null} is returned.</p>
     *
     * @return the {@code Long} customer ID if a customer profile is linked to this user,
     *         or {@code null} if no customer profile exists
     */
    public Long getCustomerId() {
        return user.getCustomer() != null ? user.getCustomer().getCustomerId() : null;
    }

    /**
     * Returns the raw role string assigned to this user as stored in the data source.
     *
     * <p>This value is the plain role name without the {@code ROLE_} prefix (e.g., {@code "ADMIN"}
     * or {@code "USER"}). For the Spring Security authority representation with the prefix,
     * refer to {@link #getAuthorities()}.</p>
     *
     * @return the role string of the wrapped {@link AppUser} (e.g., {@code "ADMIN"} or {@code "USER"})
     */
    public String getRole() {
        return user.getRole();
    }

    /**
     * Returns the underlying {@link AppUser} domain entity wrapped by this instance.
     *
     * <p>Use this accessor when full access to the application user's domain-level
     * properties is required beyond what is exposed by the {@link UserDetails} interface.</p>
     *
     * @return the {@link AppUser} entity wrapped by this {@code CustomUserDetails}
     */
    public AppUser getAppUser() {
        return user;
    }

    /**
     * Returns the collection of granted authorities assigned to the authenticated user.
     *
     * <p>The authority is derived from the user's role by prefixing it with {@code ROLE_},
     * as required by Spring Security's role-based access control conventions. For example,
     * a user with role {@code "ADMIN"} is granted the authority {@code "ROLE_ADMIN"}.</p>
     *
     * @return a singleton {@link List} containing a {@link SimpleGrantedAuthority} constructed
     *         from the user's role prefixed with {@code "ROLE_"}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    /**
     * Returns the BCrypt-encoded password of the authenticated user as stored in the database.
     *
     * <p>This value is used by Spring Security's {@code DaoAuthenticationProvider} to verify
     * the raw password supplied at login against the stored hash.</p>
     *
     * @return the BCrypt-encoded password hash of the wrapped {@link AppUser}
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * <p>This value is used as the principal identifier throughout Spring Security's
     * authentication and authorization pipeline.</p>
     *
     * @return the username of the wrapped {@link AppUser}
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * <p>This implementation always returns {@code true}, indicating that accounts
     * never expire in this application.</p>
     *
     * @return {@code true} unconditionally
     */
    @Override public boolean isAccountNonExpired() { return true; }

    /**
     * Indicates whether the user's account is locked.
     *
     * <p>This implementation always returns {@code true}, indicating that accounts
     * are never locked in this application.</p>
     *
     * @return {@code true} unconditionally
     */
    @Override public boolean isAccountNonLocked() { return true; }

    /**
     * Indicates whether the user's credentials (password) have expired.
     *
     * <p>This implementation always returns {@code true}, indicating that credentials
     * never expire in this application.</p>
     *
     * @return {@code true} unconditionally
     */
    @Override public boolean isCredentialsNonExpired() { return true; }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * <p>This implementation always returns {@code true}, indicating that all registered
     * users are enabled and can authenticate without any additional activation step.</p>
     *
     * @return {@code true} unconditionally
     */
    @Override public boolean isEnabled() { return true; }
}
