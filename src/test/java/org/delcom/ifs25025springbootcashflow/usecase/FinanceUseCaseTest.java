package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25025springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceUseCaseTest {

    private static final int ME = 1;
    private static final int OTHER = 2;

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void balanceIsIncomeMinusExpenseAndScopedPerUser() throws Exception {
        useCase.addIncome(ME, "Gaji", 5_000_000, null);
        useCase.addExpense(ME, "Makan", 25_000, null);
        useCase.addIncome(OTHER, "Bonus", 1_000, null);

        assertEquals(5_000_000, useCase.getTotalIncome(ME));
        assertEquals(25_000, useCase.getTotalExpense(ME));
        assertEquals(4_975_000, useCase.getBalance(ME));
        assertEquals(1_000, useCase.getBalance(OTHER));
        assertEquals(2, useCase.getTransactions(ME).size());
    }

    @Test
    void balanceCanBeNegative() throws Exception {
        useCase.addExpense(ME, "Sewa", 700, null);
        useCase.addIncome(ME, "Uang saku", 200, null);
        assertEquals(-500, useCase.getBalance(ME));
    }

    @Test
    void addTrimsDescriptionAndNormalizesProof() throws Exception {
        Transaction withProof = useCase.addIncome(ME, "  Gaji  ", 10, "uploads/a.png");
        Transaction blankProof = useCase.addExpense(ME, "Kopi", 10, "  ");
        Transaction nullProof = useCase.addExpense(ME, "Teh", 10, null);

        assertEquals("Gaji", withProof.getDescription());
        assertEquals(TransactionType.INCOME, withProof.getType());
        assertEquals("uploads/a.png", withProof.getProofPath());
        assertEquals(TransactionType.EXPENSE, blankProof.getType());
        assertNull(blankProof.getProofPath());
        assertNull(nullProof.getProofPath());
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, null, 10, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, "  ", 10, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, "x".repeat(501), 10, null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "x", 0, null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "x", -5, null));
        assertThrows(ValidationException.class,
                () -> useCase.addExpense(ME, "x", FinanceUseCase.MAX_AMOUNT + 1, null));
        assertTrue(useCase.getTransactions(ME).isEmpty());
    }

    @Test
    void parseAmountAcceptsOnlyWholeNumbers() throws Exception {
        assertEquals(1_000_000, useCase.parseAmount(" 1000000 "));
        assertThrows(ValidationException.class, () -> useCase.parseAmount(null));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("   "));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("abc"));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("10.5"));
        assertThrows(ValidationException.class, () -> useCase.parseAmount("99999999999999999999"));
    }

    @Test
    void updateChangesOwnTransactionOnly() throws Exception {
        Transaction t = useCase.addExpense(ME, "Makan", 10, null);

        Transaction updated = useCase.updateTransaction(ME, t.getId(), " Makan siang ", 30);
        assertEquals("Makan siang", updated.getDescription());
        assertEquals(30, useCase.getTransactions(ME).get(0).getAmount());

        assertThrows(ValidationException.class, () -> useCase.updateTransaction(ME, 0, "x", 1));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(ME, t.getId(), "", 1));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateTransaction(ME, 99, "x", 1));
        assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransaction(OTHER, t.getId(), "x", 1));
    }

    @Test
    void removeDeletesOwnTransactionOnly() throws Exception {
        Transaction t = useCase.addIncome(ME, "Gaji", 10, "uploads/a.png");

        assertThrows(ValidationException.class, () -> useCase.removeTransaction(ME, -1));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(ME, 99));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(OTHER, t.getId()));
        assertEquals(1, useCase.getTransactions(ME).size());

        Transaction removed = useCase.removeTransaction(ME, t.getId());
        assertEquals("uploads/a.png", removed.getProofPath());
        assertTrue(useCase.getTransactions(ME).isEmpty());
    }

    @Test
    void searchAndSortWork() throws Exception {
        useCase.addIncome(ME, "Gaji Bulanan", 100, null);
        useCase.addExpense(ME, "gaji ibu", 300, null);
        useCase.addExpense(ME, "Kopi", 50, null);
        useCase.addExpense(OTHER, "gaji orang lain", 1, null);

        assertEquals(2, useCase.searchTransactions(ME, " GAJI ").size());
        assertEquals(3, useCase.searchTransactions(ME, null).size());
        assertEquals(3, useCase.searchTransactions(ME, "  ").size());
        assertEquals(0, useCase.searchTransactions(ME, "tidak ada").size());

        assertEquals("gaji ibu", useCase.sortTransactions(ME, SortOption.AMOUNT_DESC).get(0).getDescription());
        assertEquals("Gaji Bulanan", useCase.sortTransactions(ME, SortOption.INCOME_FIRST).get(0).getDescription());
        assertEquals(1, useCase.findTransactions(ME, "kopi", SortOption.AMOUNT_ASC).size());
        List<Transaction> unsorted = useCase.findTransactions(ME, "", null);
        assertEquals("Gaji Bulanan", unsorted.get(0).getDescription());
        assertEquals(3, unsorted.size());
    }
}
