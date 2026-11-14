package org.delcom.ifs25025springbootcashflow.domain;

import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    @Test
    void transactionExposesStateAndSignedAmount() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 13, 8, 30);
        Transaction income = new Transaction(1, 7, "Gaji", 5_000_000, TransactionType.INCOME, "uploads/a.png", now);
        Transaction expense = new Transaction(2, 7, "Makan", 25_000, TransactionType.EXPENSE, null);

        assertEquals(1, income.getId());
        assertEquals(7, income.getUserId());
        assertEquals("Gaji", income.getDescription());
        assertEquals(5_000_000, income.getAmount());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals("uploads/a.png", income.getProofPath());
        assertEquals(now, income.getCreatedAt());
        assertTrue(income.isIncome());
        assertFalse(income.isExpense());
        assertTrue(income.hasProof());
        assertTrue(income.isOwnedBy(7));
        assertFalse(income.isOwnedBy(8));
        assertEquals(5_000_000, income.signedAmount());

        assertTrue(expense.isExpense());
        assertFalse(expense.isIncome());
        assertFalse(expense.hasProof());
        assertNotNull(expense.getCreatedAt());
        assertEquals(-25_000, expense.signedAmount());
    }

    @Test
    void transactionCanBeChanged() {
        Transaction t = new Transaction(1, 1, "Lama", 10, TransactionType.INCOME, null);
        t.changeDescription("Baru");
        t.changeAmount(99);
        assertEquals("Baru", t.getDescription());
        assertEquals(99, t.getAmount());
    }

    @Test
    void userExposesStateAndCanBeChanged() {
        User admin = new User(1, "admin", "hash", "Administrator", Role.ADMIN);
        User user = new User(2, "budi", "hash2", "Budi", Role.USER, "uploads/b.png");

        assertEquals(1, admin.getId());
        assertEquals("admin", admin.getUsername());
        assertEquals("hash", admin.getPasswordHash());
        assertEquals("Administrator", admin.getFullName());
        assertEquals(Role.ADMIN, admin.getRole());
        assertNull(admin.getPhotoPath());
        assertTrue(admin.isAdmin());
        assertFalse(user.isAdmin());
        assertEquals("uploads/b.png", user.getPhotoPath());

        user.changeFullName("Budi S");
        user.changePasswordHash("h3");
        user.changePhotoPath(null);
        assertEquals("Budi S", user.getFullName());
        assertEquals("h3", user.getPasswordHash());
        assertNull(user.getPhotoPath());
    }

    @Test
    void sortOptionsOrderTransactions() {
        Transaction a = new Transaction(1, 1, "a", 300, TransactionType.EXPENSE, null);
        Transaction b = new Transaction(2, 1, "b", 100, TransactionType.INCOME, null);
        Transaction c = new Transaction(3, 1, "c", 200, TransactionType.EXPENSE, null);
        Transaction d = new Transaction(4, 1, "d", 400, TransactionType.INCOME, null);

        assertEquals(List.of(2, 3, 1, 4), ids(sorted(SortOption.AMOUNT_ASC, a, b, c, d)));
        assertEquals(List.of(4, 1, 3, 2), ids(sorted(SortOption.AMOUNT_DESC, a, b, c, d)));
        assertEquals(List.of(2, 4, 1, 3), ids(sorted(SortOption.INCOME_FIRST, a, b, c, d)));
        assertEquals(List.of(1, 3, 2, 4), ids(sorted(SortOption.EXPENSE_FIRST, a, b, c, d)));
        assertEquals(4, SortOption.values().length);
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
    }

    private static List<Transaction> sorted(SortOption option, Transaction... items) {
        List<Transaction> list = new ArrayList<>(List.of(items));
        list.sort(option.comparator());
        return list;
    }

    private static List<Integer> ids(List<Transaction> list) {
        return list.stream().map(Transaction::getId).toList();
    }

    @Test
    void enumsExposeTheirValues() {
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
        assertEquals(2, TransactionType.values().length);
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(2, Role.values().length);
    }

    @Test
    void passwordHasherProducesSha256Hex() {
        assertEquals("8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
                PasswordHasher.hash("admin"));
        assertTrue(PasswordHasher.matches("admin", PasswordHasher.hash("admin")));
        assertFalse(PasswordHasher.matches("salah", PasswordHasher.hash("admin")));
        assertFalse(PasswordHasher.matches(null, "x"));
        assertFalse(PasswordHasher.matches("x", null));
    }

    @Test
    void passwordHasherReportsUnknownAlgorithm() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> PasswordHasher.digest("TIDAK-ADA", "x"));
        assertTrue(ex.getMessage().contains("TIDAK-ADA"));
    }

    @Test
    void exceptionsCarryMessages() {
        DomainException plain = new DomainException("dasar");
        DomainException withCause = new DomainException("bungkus", new RuntimeException("sebab"));
        EntityNotFoundException notFound = new EntityNotFoundException("Transaksi", 5);
        ValidationException validation = new ValidationException("tidak valid");

        assertEquals("dasar", plain.getMessage());
        assertEquals("sebab", withCause.getCause().getMessage());
        assertEquals("Transaksi dengan ID 5 tidak ditemukan.", notFound.getMessage());
        assertEquals(5, notFound.getId());
        assertEquals("tidak valid", validation.getMessage());
        assertTrue(notFound instanceof DomainException);
        assertTrue(validation instanceof DomainException);
    }
}
