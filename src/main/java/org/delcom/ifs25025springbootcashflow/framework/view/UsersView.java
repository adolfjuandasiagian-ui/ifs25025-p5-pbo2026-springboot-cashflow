package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import java.util.Comparator;
import java.util.Map;

/** Daftar seluruh pengguna (khusus ADMIN). Role USER dialihkan kembali ke /. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final Grid<User> grid = new Grid<>();

    public UsersView(UserUseCase userUseCase, IUserRepository userRepository, FinancePresenter presenter) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        getElement().setAttribute("role", "main");
        setPadding(true);
        setSpacing(true);
        configureGrid();
        add(new H1("Daftar Pengguna"), new PageCard(grid));
    }

    private void configureGrid() {
        GridStyles.apply(grid);

        grid.addColumn(User::getId)
                .setHeader("ID").setKey("id").setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(User::getId);

        grid.addComponentColumn(u -> {
            Avatar avatar = new Avatar(u.getFullName());
            if (u.getPhotoPath() != null) {
                avatar.setImage(u.getPhotoPath());
            }
            return avatar;
        }).setHeader("Foto").setKey("photo").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(User::getUsername)
                .setHeader("Username").setKey("username").setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));

        grid.addColumn(User::getFullName)
                .setHeader("Nama lengkap").setKey("fullName").setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(u -> {
            Span badge = new Span(presenter.formatRole(u.getRole()));
            badge.getStyle()
                    .set("padding", "0.15rem 0.6rem")
                    .set("border-radius", "999px")
                    .set("font-size", "0.8rem")
                    .set("font-weight", "600")
                    .set("color", u.isAdmin() ? "#1E40AF" : "#374151")
                    .set("background", u.isAdmin() ? "#DBEAFE" : "#E5E7EB");
            return badge;
        }).setHeader("Role").setKey("role").setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(Comparator.comparing(User::getRole));

        grid.addComponentColumn(u -> {
            Button view = new Button("Lihat Transaksi", VaadinIcon.EYE.create(), e ->
                    UI.getCurrent().navigate("user-transactions",
                            QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            return view;
        }).setHeader("Tindakan").setKey("actions").setAutoWidth(true).setFlexGrow(0);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(userRepository);
        if (current == null || !current.isAdmin()) {
            event.getUI().getPage().setLocation("/");
            event.rerouteTo(FinanceView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        grid.setItems(userUseCase.listUsers());
    }
}
