package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/**
 * Layout halaman terproteksi: sidebar navigasi + menu avatar (Profil / Keluar).
 * Berfungsi juga sebagai route guard: bila belum login, navigasi dialihkan ke /login.
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final Div drawerContent = new Div();
    private final Div userSlot = new Div();

    public MainLayout(IUserRepository userRepository, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.presenter = presenter;

        setPrimarySection(Section.DRAWER);

        H1 title = new H1("Cashflow");
        title.addClassName("app-title");

        Span spacer = new Span();
        spacer.getStyle().set("flex-grow", "1");

        userSlot.addClassName("user-slot");
        addToNavbar(new DrawerToggle(), title, spacer, userSlot);

        Span brand = new Span("Cashflow");
        brand.addClassName("drawer-brand");
        addToDrawer(brand, drawerContent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(userRepository);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        rebuild(user);
    }

    /** Dipanggil ProfileView setelah profil / foto berubah agar avatar dan nama ikut diperbarui. */
    public void refreshUser() {
        User user = AuthSession.currentUser(userRepository);
        if (user != null) {
            rebuild(user);
        }
    }

    private void rebuild(User user) {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }
        drawerContent.removeAll();
        drawerContent.add(nav);

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage(user.getPhotoPath());
        }
        MenuBar menu = new MenuBar();
        var item = menu.addItem(avatar);
        item.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        item.getSubMenu().addItem("Keluar", e -> logout());

        Span name = new Span(user.getFullName());
        name.addClassName("user-name");
        Span role = new Span(presenter.formatRole(user.getRole()));
        role.addClassName("user-role");
        Div text = new Div(name, role);
        text.addClassName("user-text");

        userSlot.removeAll();
        userSlot.add(text, menu);
    }

    private void logout() {
        AuthSession.logout();
        UI.getCurrent().getPage().setLocation("login");
    }
}
