package org.delcom.ifs25025springbootcashflow.adapter.repository;

import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/** Implementasi in-memory {@link ITransactionRepository} untuk unit test. */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    /** Penghitung ID otomatis. */
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type,
                            String proofPath) {
        return save(new Transaction(++idCounter, userId, description, amount, type, proofPath));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.isOwnedBy(userId));
    }
}
