package org.delcom.ifs25025springbootcashflow.domain.repository;

import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;

import java.util.List;

/** Port spesifik Transaction yang memperluas generic {@link IRepository}. */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Membuat dan menyimpan transaksi baru milik {@code userId}; ID dibuat otomatis. */
    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    /** Mengambil semua transaksi milik satu pengguna, terurut berdasarkan ID. */
    List<Transaction> findByUserId(int userId);
}
