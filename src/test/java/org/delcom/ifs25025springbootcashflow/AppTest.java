package org.delcom.ifs25025springbootcashflow;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.component.page.TargetElement;
import com.vaadin.flow.server.AppShellSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

/** Smoke test: konteks Spring (profil test, H2) naik dan seluruh bean Clean Architecture terpasang. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AppTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITransactionRepository transactionRepository;

    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private UserUseCase userUseCase;

    @Autowired
    private FinanceUseCase financeUseCase;

    @Autowired
    private FinancePresenter financePresenter;

    @Autowired
    private FileStorage fileStorage;

    @Test
    void contextLoadsAndWiresCleanArchitectureBeans() {
        assertNotNull(userRepository);
        assertNotNull(transactionRepository);
        assertNotNull(authUseCase);
        assertNotNull(userUseCase);
        assertNotNull(financeUseCase);
        assertNotNull(financePresenter);
        assertNotNull(fileStorage);
    }

    @Test
    void rootAdminIsSeededOnStartup() {
        assertEquals(Role.ADMIN, userRepository.findByUsername("admin").orElseThrow().getRole());
    }

    @Test
    void mainDelegatesToSpringApplicationRun() throws Exception {
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            App.main(new String[]{});
            spring.verify(() -> SpringApplication.run(any(Class.class), any(String[].class)));
        }
    }

    @Test
    void configuresAccessiblePageMetadata() {
        AppShellSettings settings = mock(AppShellSettings.class);

        new App().configurePage(settings);

        verify(settings).setPageTitle("Cashflow | Catatan Keuangan Pribadi");
        verify(settings).addMetaTag(eq("description"), contains("Catat pemasukan dan pengeluaran"));
        verify(settings).addFavIcon("favicon.ico", "32x32", "image/png");
        verify(settings).addInlineWithContents(eq(TargetElement.BODY), eq(Inline.Position.PREPEND),
                contains("<main id=\"cashflow-shell-fallback\""), eq(Inline.Wrapping.NONE));
        verify(settings).addInlineWithContents(eq(Inline.Position.PREPEND),
                contains("role', 'region'"), eq(Inline.Wrapping.JAVASCRIPT));
        verify(settings).addInlineWithContents(eq(Inline.Position.PREPEND),
                contains("setAttribute('aria-live', 'polite')"), eq(Inline.Wrapping.JAVASCRIPT));
        verify(settings).addInlineWithContents(eq(Inline.Position.PREPEND),
                contains("setAttribute('aria-atomic', 'true')"), eq(Inline.Wrapping.JAVASCRIPT));
        verify(settings).addInlineWithContents(eq(Inline.Position.APPEND),
                contains("cashflow-shell-fallback"), eq(Inline.Wrapping.JAVASCRIPT));
    }

    @Test
    void servesAccessibleFallbackInInitialPageShell() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/login"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        HttpResponse<String> response;
        do {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body().contains("<main id=\"cashflow-shell-fallback\"")) {
                break;
            }
            Thread.sleep(200);
        } while (System.nanoTime() < deadline);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("<main id=\"cashflow-shell-fallback\""));
        assertTrue(response.body().contains("<h1>Cashflow</h1>"));
    }

    @Test
    void servesValidRobotsInstructionsAsPlainText() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/robots.txt")).GET().build();
        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("")
                .startsWith("text/plain"));
        assertTrue(response.body().contains("User-agent: *"));
        assertTrue(response.body().contains("Allow: /login"));
        assertTrue(response.body().contains("Disallow: /users"));
        assertTrue(response.body().contains(
                "Sitemap: https://ifs25025-p5-pbo2026-springboot-cashflow.onrender.com/sitemap.xml"));
    }

    @Test
    void servesSitemapForPublicRoutesAsXml() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/sitemap.xml")).GET().build();
        HttpResponse<String> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("")
                .contains("xml"));
        assertTrue(response.body().contains("<urlset"));
        assertTrue(response.body().contains(
                "https://ifs25025-p5-pbo2026-springboot-cashflow.onrender.com/login"));
        assertTrue(response.body().contains(
                "https://ifs25025-p5-pbo2026-springboot-cashflow.onrender.com/register"));
        assertTrue(response.body().contains("</urlset>"));
    }

    @Test
    void servesFaviconAtTheStandardPath() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/favicon.ico")).GET().build();
        HttpResponse<Void> response =
                HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());

        assertEquals(302, response.statusCode());
        assertEquals("/icons/icon.png", response.headers().firstValue("Location").orElse(""));

        HttpRequest iconRequest = HttpRequest.newBuilder(
                URI.create("http://localhost:" + port + "/icons/icon.png")).GET().build();
        HttpResponse<byte[]> iconResponse = HttpClient.newHttpClient().send(
                iconRequest, HttpResponse.BodyHandlers.ofByteArray());

        assertEquals(200, iconResponse.statusCode());
        assertTrue(iconResponse.headers().firstValue("Content-Type").orElse("")
                .startsWith("image/png"));
        assertTrue(iconResponse.body().length > 0);
    }
}
