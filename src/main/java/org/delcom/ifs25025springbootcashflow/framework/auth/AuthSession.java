package org.delcom.ifs25025springbootcashflow.framework.auth;

import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import jakarta.servlet.http.Cookie;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Sesi login: menyimpan ID user di VaadinSession + HTTP session, dan cookie {@code cashflow_uid}
 * (ditandatangani HMAC-SHA256 agar tidak bisa dipalsukan) supaya sesi pulih setelah refresh.
 * Flag {@link #LOGGED_OUT_KEY} mencegah cookie memulihkan sesi setelah logout.
 */
public final class AuthSession {

    public static final String USER_ID_KEY = "cashflow.userId";
    public static final String LOGGED_OUT_KEY = "cashflow.loggedOut";
    public static final String COOKIE_NAME = "cashflow_uid";

    private static final int COOKIE_MAX_AGE_SECONDS = 8 * 60 * 60;
    private static final byte[] SECRET = loadSecret();

    private AuthSession() {
    }

    /** Menandai user sebagai sudah login pada sesi ini. */
    public static void login(User user) {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(LOGGED_OUT_KEY, Boolean.FALSE);
        remember(session, user.getId());
        writeCookie(signedValue(user.getId()), COOKIE_MAX_AGE_SECONDS);
    }

    /** Menghapus sesi login dan cookie, serta mencegah pemulihan otomatis dari cookie. */
    public static void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(USER_ID_KEY, null);
        session.setAttribute(LOGGED_OUT_KEY, Boolean.TRUE);
        if (session.getSession() != null) {
            session.getSession().removeAttribute(USER_ID_KEY);
        }
        writeCookie("", 0);
    }

    /** User yang sedang login, atau null bila belum login / sudah logout / user sudah dihapus. */
    public static User currentUser(IUserRepository userRepository) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        Integer id = asInteger(session.getAttribute(USER_ID_KEY));
        if (id == null && session.getSession() != null) {
            id = asInteger(session.getSession().getAttribute(USER_ID_KEY));
        }
        if (id == null && !Boolean.TRUE.equals(session.getAttribute(LOGGED_OUT_KEY))) {
            id = readCookieUserId();
        }
        if (id == null) {
            return null;
        }
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            remember(session, id);
        }
        return user;
    }

    private static void remember(VaadinSession session, int userId) {
        session.setAttribute(USER_ID_KEY, userId);
        if (session.getSession() != null) {
            session.getSession().setAttribute(USER_ID_KEY, userId);
        }
    }

    private static Integer asInteger(Object value) {
        return value instanceof Integer i ? i : null;
    }

    // ------------------------------------------------------------------ cookie

    private static void writeCookie(String value, int maxAgeSeconds) {
        VaadinResponse response = VaadinResponse.getCurrent();
        VaadinRequest request = VaadinRequest.getCurrent();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        cookie.setPath(cookiePath(request));
        cookie.setAttribute("SameSite", "Lax");
        if (request != null) {
            cookie.setSecure(request.isSecure());
        }
        response.addCookie(cookie);
    }

    private static String cookiePath(VaadinRequest request) {
        String contextPath = request == null ? null : request.getContextPath();
        return contextPath == null || contextPath.isEmpty() ? "/" : contextPath;
    }

    private static Integer readCookieUserId() {
        VaadinRequest request = VaadinRequest.getCurrent();
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return verify(cookie.getValue());
            }
        }
        return null;
    }

    private static String signedValue(int userId) {
        return userId + "." + sign(String.valueOf(userId));
    }

    private static Integer verify(String value) {
        if (value == null) {
            return null;
        }
        int dot = value.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String idPart = value.substring(0, dot);
        String expected = sign(idPart);
        String given = value.substring(dot + 1);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                given.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }
        try {
            return Integer.valueOf(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Gagal menandatangani cookie sesi", e);
        }
    }

    /** Kunci HMAC dari COOKIE_SECRET (opsional di .env); bila kosong dibuat acak per proses. */
    private static byte[] loadSecret() {
        String configured = System.getProperty("COOKIE_SECRET");
        if (configured == null) {
            configured = System.getenv("COOKIE_SECRET");
        }
        if (configured != null && !configured.isBlank()) {
            return configured.getBytes(StandardCharsets.UTF_8);
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return random;
    }
}
