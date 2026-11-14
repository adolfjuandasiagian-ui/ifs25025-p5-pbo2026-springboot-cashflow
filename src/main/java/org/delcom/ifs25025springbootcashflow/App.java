package org.delcom.ifs25025springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.component.page.TargetElement;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Entry point aplikasi Cashflow (Spring Boot + Vaadin + JDBC PostgreSQL). */
@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@PWA(name = "Cashflow", shortName = "Cashflow", iconPath = "icons/icon.png")
public class App implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle("Cashflow | Catatan Keuangan Pribadi");
        settings.addMetaTag("description",
                "Catat pemasukan dan pengeluaran, kelola transaksi, dan pantau saldo keuangan pribadi Anda.");
        settings.addFavIcon("favicon.ico", "32x32", "image/png");
        settings.addInlineWithContents(TargetElement.BODY, Inline.Position.PREPEND,
                "<main id=\"cashflow-shell-fallback\" aria-busy=\"true\" "
                        + "style=\"min-height:100vh;display:grid;place-content:center;"
                        + "text-align:center;padding:1rem\">"
                        + "<h1>Cashflow</h1><p aria-live=\"polite\">Memuat catatan keuangan...</p></main>",
                Inline.Wrapping.NONE);
        settings.addInlineWithContents(Inline.Position.PREPEND,
                "document.documentElement.lang = 'id';"
                        + "const setConnectionStatusRole = () => {"
                        + "  const indicator = document.querySelector('vaadin-connection-indicator');"
                        + "  if (!indicator) return false;"
                        + "  indicator.setAttribute('role', 'region');"
                        + "  indicator.setAttribute('aria-label', 'Status koneksi aplikasi');"
                        + "  indicator.setAttribute('aria-live', 'polite');"
                        + "  indicator.setAttribute('aria-atomic', 'true');"
                        + "  return true;"
                        + "};"
                        + "if (!setConnectionStatusRole()) {"
                        + "  const observer = new MutationObserver(() => {"
                        + "    if (setConnectionStatusRole()) observer.disconnect();"
                        + "  });"
                        + "  observer.observe(document.documentElement, { childList: true, subtree: true });"
                        + "}", Inline.Wrapping.JAVASCRIPT);
        settings.addInlineWithContents(Inline.Position.APPEND,
                "document.addEventListener('DOMContentLoaded', () => {"
                        + "  const outlet = document.getElementById('outlet');"
                        + "  if (!outlet) return;"
                        + "  const hasRouteContent = () => outlet.querySelector('[role=\"main\"]') !== null;"
                        + "  const removeFallback = () => {"
                        + "    const fallback = document.getElementById('cashflow-shell-fallback');"
                        + "    if (fallback) fallback.remove();"
                        + "  };"
                        + "  if (hasRouteContent()) { removeFallback(); return; }"
                        + "  const observer = new MutationObserver(() => {"
                        + "    if (hasRouteContent()) {"
                        + "      removeFallback();"
                        + "      observer.disconnect();"
                        + "    }"
                        + "  });"
                        + "  observer.observe(outlet, { childList: true, subtree: true, attributes: true, attributeFilter: ['role'] });"
                        + "});",
                Inline.Wrapping.JAVASCRIPT);
    }

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
