package com.dxsoft.rsgzgl.security.ukey;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;

/**
 * Session markers for password→UKey step-up. Full SecurityContext is not established until
 * verify-step succeeds.
 */
public final class UkeyPreAuth {

    public static final String USERNAME_ATTR = "UKEY_PRE_AUTH_USERNAME";
    public static final String EXPIRES_ATTR = "UKEY_PRE_AUTH_EXPIRES_AT";

    private UkeyPreAuth() {
    }

    public static void store(HttpSession session, String username, long ttlSeconds) {
        session.setAttribute(USERNAME_ATTR, username);
        session.setAttribute(EXPIRES_ATTR, Instant.now().plusSeconds(ttlSeconds).toEpochMilli());
    }

    public static void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(USERNAME_ATTR);
        session.removeAttribute(EXPIRES_ATTR);
    }

    public static String usernameIfValid(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object username = session.getAttribute(USERNAME_ATTR);
        Object expires = session.getAttribute(EXPIRES_ATTR);
        if (!(username instanceof String name) || name.isBlank() || !(expires instanceof Long epochMs)) {
            return null;
        }
        if (Instant.now().toEpochMilli() > epochMs) {
            clear(session);
            return null;
        }
        return name;
    }

    public static boolean hasValid(HttpSession session) {
        return usernameIfValid(session) != null;
    }
}
