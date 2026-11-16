package org.delcom.ifs25025springbootcashflow.framework.view;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Grid transaksi: ID, keterangan, jumlah, tipe, bukti (thumbnail yang bisa diklik), waktu,
 * dan (opsional) tombol ubah / hapus. Bila {@code onEdit} dan {@code onDelete} null,
 * kolom tindakan tidak dibuat sehingga grid bersifat read-only (dipakai halaman admin).
 */
public class TransactionGrid extends Grid<Transaction> {

    private final FinancePresenter presenter;

    public TransactionGrid(FinancePresenter presenter, Consumer<Transaction> onProof,
                           Consumer<Transaction> onEdit, Consumer<Transaction> onDelete) {
        this.presenter = presenter;
        getElement().setAttribute("aria-label", "Daftar transaksi");
        GridStyles.apply(this);

        addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        addComponentColumn(this::createAmount)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));

        addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        addComponentColumn(t -> createProof(t, onProof))
                .setHeader("Bukti")
                .setKey("proof")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        if (onEdit != null || onDelete != null) {
            addComponentColumn(t -> createActions(t, onEdit, onDelete))
                    .setHeader("Tindakan")
                    .setKey("actions")
                    .setAutoWidth(true)
                    .setFlexGrow(0)
                    .setSortable(false);
        }
    }

    private Span createAmount(Transaction transaction) {
        Span amount = new Span(presenter.formatMoney(transaction.getAmount()));
        amount.getStyle()
                .set("font-weight", "600")
                .set("color", transaction.isIncome() ? "#166534" : "#991B1B");
        return amount;
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction.getType()));
        boolean income = transaction.isIncome();
        badge.getStyle()
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("color", income ? "#166534" : "#991B1B")
                .set("background", income ? "#DCFCE7" : "#FEE2E2");
        return badge;
    }

    private com.vaadin.flow.component.Component createProof(Transaction transaction,
                                                            Consumer<Transaction> onProof) {
        if (!transaction.hasProof()) {
            return new Span("-");
        }
        Image thumbnail = new Image(transaction.getProofPath(), "Bukti transaksi " + transaction.getId());
        thumbnail.getStyle()
                .set("width", "44px")
                .set("height", "44px")
                .set("object-fit", "cover")
                .set("border-radius", "6px")
                .set("border", "1px solid #D1D5DB");

        Button proofButton = new Button(thumbnail, e -> onProof.accept(transaction));
        proofButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        proofButton.getElement().setAttribute("aria-label", "Buka bukti transaksi " + transaction.getId());
        proofButton.getStyle().set("padding", "0").set("min-width", "0");
        return proofButton;
    }

    private HorizontalLayout createActions(Transaction transaction, Consumer<Transaction> onEdit,
                                           Consumer<Transaction> onDelete) {
        HorizontalLayout actions = new HorizontalLayout();
        if (onEdit != null) {
            Button edit = new Button("Ubah", VaadinIcon.EDIT.create(), e -> onEdit.accept(transaction));
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            actions.add(edit);
        }
        if (onDelete != null) {
            Button delete = new Button("Hapus", VaadinIcon.TRASH.create(), e -> onDelete.accept(transaction));
            delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
            actions.add(delete);
        }
        return actions;
    }
}
