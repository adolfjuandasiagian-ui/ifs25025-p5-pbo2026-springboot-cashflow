package org.delcom.ifs25025springbootcashflow;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Inline;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.AppShellSettings;
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
        settings.addInlineWithContents(Inline.Position.PREPEND,
                "document.documentElement.lang = 'id';", Inline.Wrapping.JAVASCRIPT);
    }

    public static void main(String[] args) throws Exception {
        DotEnv.load();
        SpringApplication.run(App.class, args);
    }
}
