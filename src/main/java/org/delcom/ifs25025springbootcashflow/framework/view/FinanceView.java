package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25025springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25025springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Dashboard transaksi milik pengguna yang login: kartu ringkasan, cari, urutkan,
 * tambah pemasukan / pengeluaran (dengan bukti opsional), ubah, dan hapus dengan konfirmasi.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout {

    private static final String EMPTY_DATA = "Belum ada transaksi.";
    private static final String EMPTY_SEARCH = "Transaksi tidak ditemukan.";

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final IUserRepository userRepository;
    private final FileStorage fileStorage;

    private final SummaryCards summary = new SummaryCards();
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Span statusLabel = new Span();
    private final Span emptyState = new Span(EMPTY_DATA);
    private final TransactionGrid grid;

    private User user;

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter,
                       IUserRepository userRepository, FileStorage fileStorage) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;

        grid = new TransactionGrid(presenter, this::openProofDialog,
                t -> openFormDialog(t.getType(), t), this::openDeleteDialog);
        grid.setEmptyStateComponent(emptyState);

        setPadding(true);
        setSpacing(true);

        configureToolbar();
        statusLabel.addClassName("status-label");
        add(new H1("Transaksi Keuangan"), summary, new PageCard(buildToolbar(), statusLabel, grid));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        user = AuthSession.currentUser(userRepository);
        if (user == null) {
            UI.getCurrent().navigate(LoginView.class);
            return;
        }
        refreshAll();
    }

    // ------------------------------------------------------------------ toolbar

    private void configureToolbar() {
        searchField.setLabel("Cari keterangan");
        searchField.setPlaceholder("Masukkan kata kunci...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilter());

        sortSelect.setLabel("Urutkan transaksi");
        sortSelect.setPlaceholder("Pilih pengurutan...");
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.setItems(SortOption.values());
        sortSelect.setItemLabelGenerator(presenter::formatSortOption);
        sortSelect.addValueChangeListener(e -> applyFilter());
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), e -> applyFilter());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), e -> refreshAll());

        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                e -> openFormDialog(TransactionType.INCOME, null));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                e -> openFormDialog(TransactionType.EXPENSE, null));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortSelect, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setWrap(true);
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    // ------------------------------------------------------------------ dialogs

    /** Dialog tambah (editing == null) atau ubah (editing != null) pemasukan / pengeluaran. */
    private void openFormDialog(TransactionType type, Transaction editing) {
        boolean income = type == TransactionType.INCOME;
        boolean edit = editing != null;

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(FinanceUseCase.MAX_DESCRIPTION_LENGTH);
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setWidthFull();
        if (edit) {
            descriptionField.setValue(editing.getDescription());
            amountField.setValue(String.valueOf(editing.getAmount()));
        }

        ImageUpload proofUpload = new ImageUpload();
        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);
        if (!edit) {
            form.add(new Span("Bukti transaksi (opsional, gambar maks 5 MB)"), proofUpload.component());
        }
        form.add(progress);

        Dialog dialog = new Dialog();
        String title = edit ? "Ubah Transaksi" : income ? "Tambah Pemasukan" : "Tambah Pengeluaran";
        DialogStyles.decorate(dialog, title, presenter.formatType(type), "480px");
        dialog.add(form);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());
        saveButton.addClickListener(e -> {
            progress.setVisible(true);
            saveButton.setEnabled(false);
            try {
                if (saveTransaction(type, editing, descriptionField.getValue(), amountField.getValue(),
                        proofUpload)) {
                    dialog.close();
                    refreshAll();
                }
            } finally {
                progress.setVisible(false);
                saveButton.setEnabled(true);
            }
        });
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    /** Menyimpan transaksi baru / perubahan; mengembalikan true bila berhasil. */
    private boolean saveTransaction(TransactionType type, Transaction editing, String description,
                                    String amountText, ImageUpload proofUpload) {
        String proofPath = null;
        try {
            long amount = financeUseCase.parseAmount(amountText);
            if (editing != null) {
                Transaction updated = financeUseCase.updateTransaction(
                        user.getId(), editing.getId(), description, amount);
                Toasts.success(presenter.showUpdateSuccess(updated));
                return true;
            }
            if (proofUpload.hasFile()) {
                proofPath = fileStorage.store(proofUpload.getFileName(), proofUpload.getData());
            }
            Transaction created = type == TransactionType.INCOME
                    ? financeUseCase.addIncome(user.getId(), description, amount, proofPath)
                    : financeUseCase.addExpense(user.getId(), description, amount, proofPath);
            Toasts.success(presenter.showAddSuccess(created));
            return true;
        } catch (ValidationException | EntityNotFoundException ex) {
            String cleanupWarning = proofPath == null ? null : cleanupProof(proofPath);
            Toasts.error(presenter.showError(ex.getMessage()));
            if (cleanupWarning != null) {
                Toasts.error(presenter.showError(cleanupWarning));
            }
        } catch (IOException ex) {
            Toasts.error(presenter.showError("Gagal menyimpan bukti: " + ex.getMessage()));
        }
        return false;
    }

    private void openDeleteDialog(Transaction transaction) {
        Dialog dialog = new Dialog();
        DialogStyles.decorate(dialog, "Menghapus Transaksi", "Konfirmasi", "460px");
        dialog.add(new Paragraph("Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                + "\" (ID " + transaction.getId() + ", "
                + presenter.formatMoney(transaction.getAmount()) + ")?"));

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), e -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), e -> dialog.close());

        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    private void openProofDialog(Transaction transaction) {
        new ImagePreviewDialog(transaction.getProofPath(),
                "Bukti Transaksi #" + transaction.getId()).open();
    }

    // ------------------------------------------------------------------ actions

    private void deleteTransaction(Transaction transaction) {
        try {
            Transaction removed = financeUseCase.removeTransaction(user.getId(), transaction.getId());
            String cleanupWarning = removed.hasProof() ? cleanupProof(removed.getProofPath()) : null;
            Toasts.success(presenter.showRemoveSuccess(removed));
            if (cleanupWarning != null) {
                Toasts.error(presenter.showError(cleanupWarning));
            }
            refreshAll();
        } catch (ValidationException | EntityNotFoundException ex) {
            Toasts.error(presenter.showError(ex.getMessage()));
        }
    }

    private String cleanupProof(String proofPath) {
        try {
            return fileStorage.delete(proofPath)
                    ? null
                    : "Bukti transaksi tidak ditemukan saat pembersihan.";
        } catch (UncheckedIOException ex) {
            return "Gagal menghapus bukti transaksi: " + ex.getMessage();
        }
    }

    /** Menerapkan kata kunci pencarian dan opsi pengurutan yang sedang dipilih. */
    private void applyFilter() {
        if (user == null) {
            return;
        }
        String keyword = searchField.getValue();
        List<Transaction> results =
                financeUseCase.findTransactions(user.getId(), keyword, sortSelect.getValue());
        boolean searching = keyword != null && !keyword.isBlank();
        emptyState.setText(searching ? EMPTY_SEARCH : EMPTY_DATA);
        statusLabel.setText(searching ? "Hasil pencarian: \"" + keyword.trim() + "\"" : "Daftar transaksi");
        grid.setItems(results);
        updateSummary();
    }

    private void refreshAll() {
        searchField.clear();
        sortSelect.clear();
        applyFilter();
    }

    private void updateSummary() {
        int id = user.getId();
        summary.update(financeUseCase.getTransactions(id).size(),
                presenter.formatMoney(financeUseCase.getTotalIncome(id)),
                presenter.formatMoney(financeUseCase.getTotalExpense(id)),
                presenter.formatMoney(financeUseCase.getBalance(id)));
    }
}
