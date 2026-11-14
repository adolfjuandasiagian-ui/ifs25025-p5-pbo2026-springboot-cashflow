package org.delcom.ifs25025springbootcashflow.adapter.presenter;

import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Presenter yang memformat data menjadi teks siap tampil (uang, tipe, tanggal, pesan notifikasi).
 * Format teks bukan tanggung jawab entity maupun use case.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<SortOption, String> SORT_LABELS = Map.of(
            SortOption.AMOUNT_ASC, "Jumlah terkecil",
            SortOption.AMOUNT_DESC, "Jumlah terbesar",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu");

    /** Memformat uang: 1000000 menjadi "Rp 1,000,000.00"; negatif menjadi "-Rp 500.00". */
    public String formatMoney(long amount) {
        BigDecimal absolute = BigDecimal.valueOf(amount).abs();
        String text = String.format(Locale.US, "Rp %,.2f", absolute);
        return amount < 0 ? "-" + text : text;
    }

    /** Label tipe transaksi. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Label role pengguna. */
    public String formatRole(Role role) {
        return role == Role.ADMIN ? "Admin" : "User";
    }

    /** Label opsi pengurutan (aman terhadap nilai null). */
    public String formatSortOption(SortOption option) {
        return option == null ? "Urutkan..." : SORT_LABELS.get(option);
    }

    /** Waktu pencatatan: dd/MM/yyyy HH:mm. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Satu baris transaksi: [id] keterangan | Rp x | [Tipe]. */
    public String format(Transaction transaction) {
        return String.format("[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType()));
    }

    /** Saldo: Rp x. */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String showAddSuccess(Transaction transaction) {
        String label = transaction.isIncome() ? "pemasukan" : "pengeluaran";
        return "Berhasil menambah " + label + ": " + format(transaction);
    }

    public String showUpdateSuccess(Transaction transaction) {
        return "Berhasil mengubah transaksi: " + format(transaction);
    }

    public String showRemoveSuccess(Transaction transaction) {
        return "Berhasil menghapus transaksi: " + format(transaction);
    }

    public String showError(String message) {
        return "[!] " + message;
    }
}
