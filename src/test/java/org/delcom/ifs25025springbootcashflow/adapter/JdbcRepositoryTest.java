package org.delcom.ifs25025springbootcashflow.adapter;

import org.delcom.ifs25025springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25025springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25025springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Tes repository JDBC memakai H2 in-memory (mode PostgreSQL), bukan PostgreSQL asli. */
class JdbcRepositoryTest {

    private DataSource dataSource;
    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;

    @BeforeEach
    void setUp() throws Exception {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:jdbc_repo_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");
        dataSource = h2;
        SchemaInitializer.initialize(dataSource);
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM transactions");
            s.execute("DELETE FROM users");
        }
        users = new JdbcUserRepository(dataSource);
        transactions = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void schemaInitializerIsIdempotent() {
        SchemaInitializer.initialize(dataSource);
        SchemaInitializer.initialize(dataSource);
        assertTrue(users.findAll().isEmpty());
    }

    @Test
    void userRepositorySupportsCrudAndLookup() {
        User budi = users.save("budi", "hash", "Budi", Role.USER);
        User admin = users.save("admin", "hash2", "Administrator", Role.ADMIN);

        assertTrue(budi.getId() > 0);
        assertEquals(2, users.findAll().size());
        assertEquals("Budi", users.findById(budi.getId()).orElseThrow().getFullName());
        assertEquals(Role.ADMIN, users.findByUsername("admin").orElseThrow().getRole());
        assertTrue(users.findByUsername("tidak-ada").isEmpty());
        assertTrue(users.findById(9999).isEmpty());
        assertEquals(1, users.findBy(User::isAdmin).size());

        budi.changeFullName("Budi Baru");
        budi.changePasswordHash("hash-baru");
        budi.changePhotoPath("uploads/budi.png");
        users.update(budi);
        User reloaded = users.findById(budi.getId()).orElseThrow();
        assertEquals("Budi Baru", reloaded.getFullName());
        assertEquals("hash-baru", reloaded.getPasswordHash());
        assertEquals("uploads/budi.png", reloaded.getPhotoPath());

        assertTrue(users.deleteById(admin.getId()));
        assertFalse(users.deleteById(admin.getId()));
    }

    @Test
    void userRepositorySavesEntityWithAndWithoutPhoto() {
        User plain = users.save(new User(0, "a", "h", "A", Role.USER));
        User withPhoto = users.save(new User(0, "b", "h", "B", Role.USER, "uploads/b.png"));

        assertNull(users.findById(plain.getId()).orElseThrow().getPhotoPath());
        assertEquals("uploads/b.png", users.findById(withPhoto.getId()).orElseThrow().getPhotoPath());
    }

    @Test
    void transactionRepositorySupportsCrudAndQueries() {
        int me = users.save("me", "h", "Me", Role.USER).getId();
        int other = users.save("other", "h", "Other", Role.USER).getId();

        Transaction income = transactions.save(me, "Gaji", 5_000_000, TransactionType.INCOME, "uploads/g.png");
        Transaction expense = transactions.save(me, "Makan", 25_000, TransactionType.EXPENSE, null);
        transactions.save(other, "Punya orang lain", 1, TransactionType.INCOME, null);

        assertEquals(3, transactions.findAll().size());
        assertEquals(2, transactions.findByUserId(me).size());
        assertEquals("Gaji", transactions.findByUserId(me).get(0).getDescription());
        assertEquals(1, transactions.findBy(Transaction::isExpense).size());

        Transaction loaded = transactions.findById(income.getId()).orElseThrow();
        assertEquals(5_000_000, loaded.getAmount());
        assertEquals(TransactionType.INCOME, loaded.getType());
        assertEquals("uploads/g.png", loaded.getProofPath());
        assertNotNull(loaded.getCreatedAt());
        assertNull(transactions.findById(expense.getId()).orElseThrow().getProofPath());
        assertTrue(transactions.findById(9999).isEmpty());

        expense.changeDescription("Makan malam");
        expense.changeAmount(40_000);
        transactions.update(expense);
        Transaction updated = transactions.findById(expense.getId()).orElseThrow();
        assertEquals("Makan malam", updated.getDescription());
        assertEquals(40_000, updated.getAmount());

        assertTrue(transactions.deleteById(expense.getId()));
        assertFalse(transactions.deleteById(expense.getId()));
    }

    @Test
    void transactionRepositorySavesGivenEntityKeepingItsTimestamp() {
        int me = users.save("me", "h", "Me", Role.USER).getId();
        LocalDateTime when = LocalDateTime.of(2026, 1, 2, 3, 4, 5);

        Transaction saved = transactions.save(
                new Transaction(0, me, "Lama", 10, TransactionType.EXPENSE, null, when));

        assertTrue(saved.getId() > 0);
        assertEquals(when, transactions.findById(saved.getId()).orElseThrow().getCreatedAt());
    }

    @Test
    void databaseFailuresAreWrappedAsIllegalStateException() {
        DataSource broken = (DataSource) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    throw new SQLException("koneksi putus");
                });

        JdbcUserRepository brokenUsers = new JdbcUserRepository(broken);
        IllegalStateException ex = assertThrows(IllegalStateException.class, brokenUsers::findAll);
        assertTrue(ex.getMessage().contains("koneksi putus"));
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(broken));
    }
}
