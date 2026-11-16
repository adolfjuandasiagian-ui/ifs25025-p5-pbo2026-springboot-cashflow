package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/** Halaman registrasi (publik). Bila sudah login, diarahkan otomatis ke halaman utama. */
@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirm = new PasswordField("Konfirmasi password");
    private final Button registerButton = new Button("Daftar");
    private final ProgressBar progress = new ProgressBar();

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        addClassName("auth-page");
        getElement().setAttribute("role", "main");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        fullName.setWidthFull();
        username.setWidthFull();
        username.setHelperText("3-30 karakter: huruf, angka, titik, atau underscore");
        password.setWidthFull();
        password.setHelperText("Minimal 6 karakter");
        confirm.setWidthFull();

        fullName.setValueChangeMode(ValueChangeMode.EAGER);
        username.setValueChangeMode(ValueChangeMode.EAGER);
        password.setValueChangeMode(ValueChangeMode.EAGER);
        confirm.setValueChangeMode(ValueChangeMode.EAGER);

        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(e -> submit());

        progress.setIndeterminate(true);
        progress.setVisible(false);

        H1 title = new H1("Buat Akun");
        title.addClassName("auth-title");
        title.getStyle().set("font-size", "1.5rem");
        Paragraph subtitle = new Paragraph("Daftar untuk mulai mencatat pemasukan dan pengeluaran.");
        subtitle.addClassName("auth-subtitle");

        Div footer = new Div(new Span("Sudah punya akun? "), new RouterLink("Masuk", LoginView.class));

        VerticalLayout card = new VerticalLayout(title, subtitle, fullName, username, password,
                confirm, progress, registerButton, footer);
        card.addClassName("auth-card");
        card.setPadding(true);
        card.setSpacing(true);
        add(card);
    }

    private void submit() {
        if (!password.getValue().equals(confirm.getValue())) {
            Toasts.error("[!] Konfirmasi password tidak sama");
            return;
        }
        progress.setVisible(true);
        registerButton.setEnabled(false);
        try {
            authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            Toasts.success("Registrasi berhasil. Silakan masuk.");
            UI.getCurrent().navigate(LoginView.class);
        } catch (ValidationException ex) {
            Toasts.error("[!] " + ex.getMessage());
        } finally {
            progress.setVisible(false);
            registerButton.setEnabled(true);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.rerouteTo(FinanceView.class);
        }
    }
}