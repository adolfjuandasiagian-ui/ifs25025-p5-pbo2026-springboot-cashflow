package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
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

/** Halaman login (publik). Bila sudah login, diarahkan otomatis ke halaman utama. */
@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Button loginButton = new Button("Masuk");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;

        addClassName("auth-page");
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // ID dipasang pada <input> di dalam komponen (bukan elemen pembungkus) agar bisa
        // dipilih oleh pengujian/otomasi: #login-username dan #login-password.
        setInnerInputId(username, "login-username");
        username.setWidthFull();
        username.setAutofocus(true);
        setInnerInputId(password, "login-password");
        password.setWidthFull();

        // EAGER: nilai langsung dikirim ke server setiap ketikan, sehingga saat tombol
        // diklik / Enter ditekan, server sudah memegang username & password terbaru.
        username.setValueChangeMode(ValueChangeMode.EAGER);
        password.setValueChangeMode(ValueChangeMode.EAGER);

        loginButton.setId("login-submit");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> submit());
        username.addKeyUpListener(Key.ENTER, e -> submit());
        password.addKeyUpListener(Key.ENTER, e -> submit());

        progress.setIndeterminate(true);
        progress.setVisible(false);

        H1 title = new H1("Cashflow");
        title.addClassName("auth-title");
        title.getStyle().set("font-size", "1.5rem");
        Paragraph subtitle = new Paragraph("Masuk untuk mengelola catatan keuangan Anda.");
        subtitle.addClassName("auth-subtitle");

        Div footer = new Div(new com.vaadin.flow.component.html.Span("Belum punya akun? "),
                new RouterLink("Daftar", RegisterView.class));

        VerticalLayout card = new VerticalLayout(title, subtitle, username, password, progress,
                loginButton, footer);
        card.addClassName("auth-card");
        card.setPadding(true);
        card.setSpacing(true);
        add(card);
    }

    private void submit() {
        progress.setVisible(true);
        loginButton.setEnabled(false);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (ValidationException ex) {
            Toasts.error("[!] " + ex.getMessage());
        } finally {
            progress.setVisible(false);
            loginButton.setEnabled(true);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository) != null) {
            event.getUI().getPage().setLocation("/");
            event.rerouteTo(FinanceView.class);
        }
    }

    /** Memasang id pada elemen <input> di dalam komponen field Vaadin. */
    private void setInnerInputId(com.vaadin.flow.component.Component component, String id) {
        Runnable apply = () -> component.getElement().executeJs(
                "const applyId = () => {"
                        + "  const input = this.inputElement || this.querySelector('input');"
                        + "  if (input) {"
                        + "    input.id = $0;"
                        + "    const label = this.querySelector('label');"
                        + "    if (label) label.setAttribute('for', $0);"
                        + "    return true;"
                        + "  }"
                        + "  return false;"
                        + "};"
                        + "if (!applyId()) {"
                        + "  if (this.updateComplete) { this.updateComplete.then(applyId); }"
                        + "  const obs = new MutationObserver(() => { if (applyId()) obs.disconnect(); });"
                        + "  obs.observe(this, { childList: true, subtree: true });"
                        + "}", id);
        apply.run();
        component.addAttachListener(event -> apply.run());
    }
}