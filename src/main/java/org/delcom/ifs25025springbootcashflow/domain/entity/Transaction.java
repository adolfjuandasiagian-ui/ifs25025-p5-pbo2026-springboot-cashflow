package org.delcom.ifs25025springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/**
 * Entity inti yang merepresentasikan satu transaksi keuangan milik satu pengguna.
 * Berada di layer domain, tidak bergantung pada layer lain (JDBC, Vaadin, Spring).
 */
public class Transaction {
    /** ID unik transaksi, tidak berubah setelah dibuat. */
    private final int id;

    /** ID pemilik transaksi. */
    private final int userId;

    /** Keterangan transaksi. */
    private String description;

    /** Jumlah uang (rupiah); selalu positif, arah ditentukan oleh {@link #type}. */
    private long amount;

    /** Jenis transaksi: pemasukan atau pengeluaran. */
    private final TransactionType type;

    /** Path relatif bukti transaksi (opsional), null jika tidak ada bukti. */
    private final String proofPath;

    /** Waktu transaksi dicatat. */
    private final LocalDateTime createdAt;

    public Transaction(int id, int userId, String description, long amount,
                       TransactionType type, String proofPath) {
        this(id, userId, description, amount, type, proofPath, LocalDateTime.now());
    }

    public Transaction(int id, int userId, String description, long amount,
                       TransactionType type, String proofPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isIncome() {
        return type == TransactionType.INCOME;
    }

    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }

    public boolean hasProof() {
        return proofPath != null;
    }

    public boolean isOwnedBy(int ownerId) {
        return userId == ownerId;
    }

    /** Dampak transaksi terhadap saldo: positif untuk pemasukan, negatif untuk pengeluaran. */
    public long signedAmount() {
        return isIncome() ? amount : -amount;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeAmount(long amount) {
        this.amount = amount;
    }
}
