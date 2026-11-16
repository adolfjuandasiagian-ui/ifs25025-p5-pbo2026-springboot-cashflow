package org.delcom.ifs25025springbootcashflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/** Applies long-lived caching only to fingerprinted Vaadin build assets. */
@Component
public final class ImmutableVaadinAssetCacheFilter extends OncePerRequestFilter {

    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String IMMUTABLE_CACHE_POLICY = "public, max-age=31536000, immutable";
    private static final Pattern FINGERPRINTED_ASSET =
            Pattern.compile("^/VAADIN/build/.+-[A-Za-z0-9_-]{8,}\\.(?:js|css|woff2?)$");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !FINGERPRINTED_ASSET.matcher(path).matches();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletResponseWrapper cacheResponse = new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                super.setHeader(name, CACHE_CONTROL.equalsIgnoreCase(name)
                        ? IMMUTABLE_CACHE_POLICY : value);
            }

            @Override
            public void addHeader(String name, String value) {
                if (CACHE_CONTROL.equalsIgnoreCase(name)) {
                    setHeader(name, value);
                } else {
                    super.addHeader(name, value);
                }
            }
        };
        cacheResponse.setHeader(CACHE_CONTROL, IMMUTABLE_CACHE_POLICY);
        filterChain.doFilter(request, cacheResponse);
    }
}
