package org.delcom.ifs25025springbootcashflow.adapter;

import org.delcom.ifs25025springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income = new Transaction(3, 1, "Gaji", 1_000_000, TransactionType.INCOME,
            null, LocalDateTime.of(2026, 11, 13, 9, 5));
    private final Transaction expense = new Transaction(4, 1, "Makan", 25_000, TransactionType.EXPENSE, null);

    @Test
    void formatsMoneyWithThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("-Rp 500.00", presenter.formatMoney(-500));
    }

    @Test
    void formatsLabels() {
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.formatType(TransactionType.EXPENSE));
        assertEquals("Admin", presenter.formatRole(Role.ADMIN));
        assertEquals("User", presenter.formatRole(Role.USER));
        assertEquals("Urutkan...", presenter.formatSortOption(null));
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.formatSortOption(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.formatSortOption(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.formatSortOption(SortOption.EXPENSE_FIRST));
    }

    @Test
    void formatsLinesAndDates() {
        assertEquals("13/11/2026 09:05", presenter.formatCreatedAt(income));
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000));
    }

    @Test
    void buildsMessages() {
        assertEquals("Berhasil menambah pemasukan: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income));
        assertEquals("Berhasil menambah pengeluaran: [4] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showAddSuccess(expense));
        assertEquals("Berhasil mengubah transaksi: [4] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showUpdateSuccess(expense));
        assertEquals("Berhasil menghapus transaksi: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showRemoveSuccess(income));
        assertEquals("[!] Keterangan tidak boleh kosong", presenter.showError("Keterangan tidak boleh kosong"));
    }
}
