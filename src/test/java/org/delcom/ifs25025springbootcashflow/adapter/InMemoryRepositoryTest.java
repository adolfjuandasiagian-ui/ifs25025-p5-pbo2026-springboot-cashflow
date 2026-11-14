package org.delcom.ifs25025springbootcashflow.adapter;

import org.delcom.ifs25025springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25025springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25025springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRepositoryTest {

    @Test
    void transactionRepositorySupportsCrudAndQueries() {
        TransactionRepository repo = new TransactionRepository();
        Transaction a = repo.save(1, "A", 10, TransactionType.INCOME, null);
        Transaction b = repo.save(2, "B", 20, TransactionType.EXPENSE, "uploads/b.png");

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(2, repo.findAll().size());
        assertEquals("B", repo.findById(2).orElseThrow().getDescription());
        assertTrue(repo.findById(9).isEmpty());
        assertEquals(1, repo.findByUserId(1).size());
        assertEquals(1, repo.findBy(t -> t.getAmount() > 15).size());

        a.changeAmount(11);
        repo.update(a);
        assertEquals(11, repo.findById(1).orElseThrow().getAmount());

        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        TransactionRepository repo = new TransactionRepository();
        repo.save(1, "A", 10, TransactionType.INCOME, null);
        repo.findAll().clear();
        assertEquals(1, repo.findAll().size());
    }

    @Test
    void userRepositoryFindsByUsername() {
        UserRepository repo = new UserRepository();
        User saved = repo.save("budi", "hash", "Budi", Role.USER);

        assertEquals(1, saved.getId());
        assertEquals(saved, repo.findByUsername("budi").orElseThrow());
        assertTrue(repo.findByUsername("lain").isEmpty());
    }
}
