package org.delcom.ifs25025springbootcashflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImmutableVaadinAssetCacheFilterTest {

    private final ImmutableVaadinAssetCacheFilter filter = new ImmutableVaadinAssetCacheFilter();

    @Test
    void appliesImmutablePolicyToFingerprintedAssetsEvenWhenTheServletSetsItsOwnPolicy()
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/VAADIN/build/generated-flow-imports-D9GXC8_d.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setHeader("Cache-Control", "max-age=3600");
            httpResponse.addHeader("Cache-Control", "public");
            httpResponse.setHeader("X-Test", "preserved");
            httpResponse.addHeader("X-Trace", "preserved");
        });

        assertEquals("public, max-age=31536000, immutable",
                response.getHeader("Cache-Control"));
        assertEquals("preserved", response.getHeader("X-Test"));
        assertEquals("preserved", response.getHeader("X-Trace"));
    }

    @Test
    void preservesDefaultPolicyForUnfingerprintedAssets() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/VAADIN/build/devtools.js");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                ((HttpServletResponse) servletResponse)
                        .setHeader("Cache-Control", "max-age=3600"));

        assertEquals("max-age=3600", response.getHeader("Cache-Control"));
    }
}
