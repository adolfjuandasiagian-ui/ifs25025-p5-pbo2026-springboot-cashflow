package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.BeforeEnterEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteAccessTest {

    private final IUserRepository userRepository = mock(IUserRepository.class);
    private final User regularUser = new User(1, "user", "hash", "User", Role.USER);

    @Test
    void unauthenticatedProtectedRoutesRerouteToLogin() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        Page page = mockPage(event);
        try (MockedStatic<AuthSession> auth = mockStatic(AuthSession.class)) {
            auth.when(() -> AuthSession.currentUser(userRepository)).thenReturn(null);

            new MainLayout(userRepository, new FinancePresenter()).beforeEnter(event);

            verify(page).setLocation("/login");
            verify(event).rerouteTo(LoginView.class);
        }
    }

    @Test
    void nonAdminUsersAreReroutedFromAdminRoutesToHome() {
        BeforeEnterEvent usersEvent = mock(BeforeEnterEvent.class);
        BeforeEnterEvent transactionsEvent = mock(BeforeEnterEvent.class);
        Page usersPage = mockPage(usersEvent);
        Page transactionsPage = mockPage(transactionsEvent);
        try (MockedStatic<AuthSession> auth = mockStatic(AuthSession.class)) {
            auth.when(() -> AuthSession.currentUser(userRepository)).thenReturn(regularUser);

            new UsersView(mock(UserUseCase.class), userRepository, new FinancePresenter())
                    .beforeEnter(usersEvent);
            new UserTransactionsView(mock(FinanceUseCase.class), mock(UserUseCase.class),
                    userRepository, new FinancePresenter()).beforeEnter(transactionsEvent);

            verify(usersPage).setLocation("/");
            verify(transactionsPage).setLocation("/");
            verify(usersEvent).rerouteTo(FinanceView.class);
            verify(transactionsEvent).rerouteTo(FinanceView.class);
        }
    }

    private static Page mockPage(BeforeEnterEvent event) {
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(event.getUI()).thenReturn(ui);
        when(ui.getPage()).thenReturn(page);
        return page;
    }
}
