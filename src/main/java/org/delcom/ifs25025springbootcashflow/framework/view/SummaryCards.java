package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/** Empat kartu ringkasan: total transaksi, total pemasukan, total pengeluaran, dan saldo. */
public class SummaryCards extends HorizontalLayout {

    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balance = new Span();

    public SummaryCards() {
        setWidthFull();
        setSpacing(true);
        setWrap(true);
        add(card("Total Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                card("Total Pemasukan", totalIncome, "#DCFCE7", "#16A34A", "#166534"),
                card("Total Pengeluaran", totalExpense, "#FEE2E2", "#DC2626", "#991B1B"),
                card("Saldo", balance, "#FEF3C7", "#D97706", "#92400E"));
    }

    public void update(int count, String income, String expense, String balanceText) {
        totalCount.setText(String.valueOf(count));
        totalIncome.setText(income);
        totalExpense.setText(expense);
        balance.setText(balanceText);
    }

    private Div card(String label, Span value, String background, String accent, String valueColor) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.4rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }
}
