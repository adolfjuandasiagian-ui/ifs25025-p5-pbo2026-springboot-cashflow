package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25025springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Optional;

/**
 * Peninjauan transaksi pengguna lain (read-only, khusus ADMIN).
 * ID pengguna dibaca dari query parameter, mis. {@code /user-transactions?userId=3}.
 */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final FinanceUseCase financeUseCase;
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final H1 heading = new H1("Transaksi Pengguna");
    private final SummaryCards summary = new SummaryCards();
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Span emptyState = new Span("Pengguna ini belum memiliki transaksi.");
    private final TransactionGrid grid;

    private User target;

    public UserTransactionsView(FinanceUseCase financeUseCase, UserUseCase userUseCase,
                                IUserRepository userRepository, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        // Read-only: tanpa tombol ubah / hapus.
        grid = new TransactionGrid(presenter, this::openProofDialog, null, null);
        grid.setEmptyStateComponent(emptyState);

        setPadding(true);
        setSpacing(true);

        searchField.setLabel("Cari keterangan");
        searchField.setPlaceholder("Masukkan kata kunci...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> load());
        sortSelect.setLabel("Urutkan transaksi");
        sortSelect.setPlaceholder("Pilih pengurutan...");
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(presenter::formatSortOption);
        sortSelect.addValueChangeListener(e -> load());

        Button back = new Button("Kembali ke Pengguna", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate(UsersView.class));
        HorizontalLayout toolbar = new HorizontalLayout(back, searchField, sortSelect);
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.setFlexGrow(1, searchField);

        add(heading, summary, new PageCard(toolbar, grid));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User current = AuthSession.currentUser(userRepository);
        if (current == null || !current.isAdmin()) {
            event.getUI().getPage().setLocation("/");
            event.rerouteTo(FinanceView.class);
            return;
        }
        Optional<Integer> id = event.getLocation().getQueryParameters()
                .getSingleParameter("userId").flatMap(UserTransactionsView::parseId);
        if (id.isEmpty()) {
            event.getUI().getPage().setLocation("/users");
            event.rerouteTo(UsersView.class);
            return;
        }
        try {
            target = userUseCase.getUser(id.get());
        } catch (EntityNotFoundException ex) {
            event.getUI().getPage().setLocation("/users");
            event.rerouteTo(UsersView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (target != null) {
            heading.setText("Transaksi: " + target.getFullName() + " (" + target.getUsername() + ")");
            load();
        }
    }

    private void load() {
        if (target == null) {
            return;
        }
        int id = target.getId();
        List<Transaction> rows =
                financeUseCase.findTransactions(id, searchField.getValue(), sortSelect.getValue());
        grid.setItems(rows);
        summary.update(financeUseCase.getTransactions(id).size(),
                presenter.formatMoney(financeUseCase.getTotalIncome(id)),
                presenter.formatMoney(financeUseCase.getTotalExpense(id)),
                presenter.formatMoney(financeUseCase.getBalance(id)));
    }

    private void openProofDialog(Transaction transaction) {
        new ImagePreviewDialog(transaction.getProofPath(),
                "Bukti Transaksi #" + transaction.getId()).open();
    }

    private static Optional<Integer> parseId(String text) {
        try {
            return Optional.of(Integer.parseInt(text.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
