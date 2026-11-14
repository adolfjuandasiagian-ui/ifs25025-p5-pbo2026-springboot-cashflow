package org.delcom.ifs25025springbootcashflow.adapter.repository;

import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/** Implementasi JDBC {@link ITransactionRepository} untuk runtime (PostgreSQL). */
public class JdbcTransactionRepository extends JdbcRepository<Transaction, Integer>
        implements ITransactionRepository {

    private static final String INSERT_SQL =
            "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE transactions SET description = ?, amount = ?, proof_path = ? WHERE id = ?";

    public JdbcTransactionRepository(DataSource dataSource) {
        super(dataSource, "transactions", "id");
    }

    @Override
    protected Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("description"),
                rs.getLong("amount"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type,
                            String proofPath) {
        return insert(userId, description, amount, type, proofPath, LocalDateTime.now());
    }

    @Override
    public Transaction save(Transaction entity) {
        return insert(entity.getUserId(), entity.getDescription(), entity.getAmount(),
                entity.getType(), entity.getProofPath(), entity.getCreatedAt());
    }

    @Override
    public void update(Transaction entity) {
        executeUpdate(UPDATE_SQL, entity.getDescription(), entity.getAmount(),
                entity.getProofPath(), entity.getId());
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return query("SELECT * FROM transactions WHERE user_id = ? ORDER BY id", userId);
    }

    private Transaction insert(int userId, String description, long amount, TransactionType type,
                               String proofPath, LocalDateTime createdAt) {
        int id = insertReturningId(INSERT_SQL, userId, description, amount, type.name(),
                proofPath, Timestamp.valueOf(createdAt));
        return new Transaction(id, userId, description, amount, type, proofPath, createdAt);
    }
}
