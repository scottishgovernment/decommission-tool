package org.mygovscot.decommissioned.config;

/**
 * This is a temporary security context holder,
 * The auth system (beta basic-authentication) is currently POC, once authentication story
 * is done, Spring security context holder (As in basic-authentication) or relevant will be used
 * The current authentication principal, Granted authorities with roles and privileges
 * will be obtained from auth and will be available in security context through the request.
 *
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<String>();

    public static void setUser(String user) {
        CONTEXT.set(user);
    }

    public static String getUser() {
        return CONTEXT.get();
    }

    private SecurityContextHolder() {
        // Utility class - should not be instantiated.
    }

}
