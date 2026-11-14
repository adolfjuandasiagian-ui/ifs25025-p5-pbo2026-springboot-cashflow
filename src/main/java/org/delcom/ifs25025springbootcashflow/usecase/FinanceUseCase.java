package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;
import java.util.Locale;

/**
 * Use case pencatatan keuangan. Seluruh operasi dibatasi per pengguna ({@code userId}):
 * transaksi milik orang lain diperlakukan seolah tidak ada.
 * Tidak melakukan I/O UI, hanya memproses data dan melempar checked exception domain.
 */
public class FinanceUseCase {

    /** Batas jumlah per transaksi: 1 triliun rupiah. */
    public static final long MAX_AMOUNT = 1_000_000_000_000L;

    /** Batas panjang keterangan. */
    public static final int MAX_DESCRIPTION_LENGTH = 500;

    private static final String ENTITY_NAME = "Transaksi";

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengubah teks input menjadi jumlah; gagal bila kosong atau bukan angka bulat. */
    public long parseAmount(String text) throws ValidationException {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) {
            throw new ValidationException("Jumlah harus diisi");
        }
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat yang valid");
        }
    }

    /** Semua transaksi milik pengguna. */
    public List<Transaction> getTransactions(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Menambah pemasukan setelah validasi; {@code proofPath} boleh null/kosong. */
    public Transaction addIncome(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        return add(userId, description, amount, TransactionType.INCOME, proofPath);
    }

    /** Menambah pengeluaran setelah validasi; {@code proofPath} boleh null/kosong. */
    public Transaction addExpense(int userId, String description, long amount, String proofPath)
            throws ValidationException {
        return add(userId, description, amount, TransactionType.EXPENSE, proofPath);
    }

    /** Mengubah keterangan dan jumlah transaksi milik pengguna. */
    public Transaction updateTransaction(int userId, int id, String description, long amount)
            throws EntityNotFoundException, ValidationException {
        requireValidId(id);
        validate(description, amount);
        Transaction transaction = findOwned(userId, id);
        transaction.changeDescription(description.trim());
        transaction.changeAmount(amount);
        transactionRepository.update(transaction);
        return transaction;
    }

    /** Menghapus transaksi milik pengguna dan mengembalikan transaksi yang terhapus. */
    public Transaction removeTransaction(int userId, int id)
            throws EntityNotFoundException, ValidationException {
        requireValidId(id);
        Transaction transaction = findOwned(userId, id);
        transactionRepository.deleteById(id);
        return transaction;
    }

    /** Mencari transaksi berdasarkan keterangan (case-insensitive); keyword kosong = semua. */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        List<Transaction> own = transactionRepository.findByUserId(userId);
        if (keyword == null || keyword.isBlank()) {
            return own;
        }
        String lowerKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return own.stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    /** Mengurutkan semua transaksi pengguna sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(int userId, SortOption option) {
        return sort(transactionRepository.findByUserId(userId), option);
    }

    /** Mencari lalu mengurutkan; keyword kosong = semua data, option null = urutan asli. */
    public List<Transaction> findTransactions(int userId, String keyword, SortOption option) {
        return sort(searchTransactions(userId, keyword), option);
    }

    /** Total seluruh pemasukan pengguna. */
    public long getTotalIncome(int userId) {
        return total(userId, TransactionType.INCOME);
    }

    /** Total seluruh pengeluaran pengguna. */
    public long getTotalExpense(int userId) {
        return total(userId, TransactionType.EXPENSE);
    }

    /** Saldo = total pemasukan - total pengeluaran (bisa negatif). */
    public long getBalance(int userId) {
        return getTotalIncome(userId) - getTotalExpense(userId);
    }

    private Transaction add(int userId, String description, long amount, TransactionType type,
                            String proofPath) throws ValidationException {
        validate(description, amount);
        String cleanProof = proofPath == null || proofPath.isBlank() ? null : proofPath;
        return transactionRepository.save(userId, description.trim(), amount, type, cleanProof);
    }

    private Transaction findOwned(int userId, int id) throws EntityNotFoundException {
        return transactionRepository.findById(id)
                .filter(t -> t.isOwnedBy(userId))
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NAME, id));
    }

    private List<Transaction> sort(List<Transaction> transactions, SortOption option) {
        if (option == null) {
            return transactions;
        }
        return transactions.stream().sorted(option.comparator()).toList();
    }

    private long total(int userId, TransactionType type) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(t -> t.getType() == type)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    private void requireValidId(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID transaksi tidak valid");
        }
    }

    private void validate(String description, long amount) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        if (description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Keterangan maksimal " + MAX_DESCRIPTION_LENGTH + " karakter");
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        if (amount > MAX_AMOUNT) {
            throw new ValidationException("Jumlah terlalu besar (maksimal 1 triliun rupiah)");
        }
    }
}
