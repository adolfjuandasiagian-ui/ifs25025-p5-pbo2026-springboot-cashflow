package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25025springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthUseCaseTest {

    private UserRepository users;
    private AuthUseCase useCase;

    @BeforeEach
    void setUp() {
        users = new UserRepository();
        useCase = new AuthUseCase(users);
    }

    @Test
    void registerCreatesUserWithHashedPassword() throws Exception {
        User user = useCase.register("  budi_01 ", "rahasia1", " Budi Santoso ");

        assertEquals("budi_01", user.getUsername());
        assertEquals("Budi Santoso", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertEquals(PasswordHasher.hash("rahasia1"), user.getPasswordHash());
        assertTrue(users.findByUsername("budi_01").isPresent());
    }

    @Test
    void registerRejectsInvalidInput() {
        assertThrows(ValidationException.class, () -> useCase.register(null, "rahasia1", "Budi"));
        assertThrows(ValidationException.class, () -> useCase.register("ab", "rahasia1", "Budi"));
        assertThrows(ValidationException.class, () -> useCase.register("bud i", "rahasia1", "Budi"));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "rahasia1", "  "));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "rahasia1", "x".repeat(201)));
        assertThrows(ValidationException.class, () -> useCase.register("budi", null, "Budi"));
        assertThrows(ValidationException.class, () -> useCase.register("budi", "12345", "Budi"));
        assertTrue(users.findAll().isEmpty());
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        useCase.register("budi", "rahasia1", "Budi");
        ValidationException ex = assertThrows(ValidationException.class,
                () -> useCase.register("budi", "lainnya1", "Budi Lain"));
        assertEquals("Username sudah digunakan", ex.getMessage());
    }

    @Test
    void loginAcceptsCorrectCredentialsOnly() throws Exception {
        useCase.register("budi", "rahasia1", "Budi");

        assertEquals("budi", useCase.login(" budi ", "rahasia1").getUsername());
        assertThrows(ValidationException.class, () -> useCase.login("budi", "salah"));
        assertThrows(ValidationException.class, () -> useCase.login("tidakada", "rahasia1"));
        assertThrows(ValidationException.class, () -> useCase.login(null, null));
    }

    @Test
    void ensureRootAdminCreatesOnlyOnce() throws Exception {
        User first = useCase.ensureRootAdmin("admin", "admin");
        User second = useCase.ensureRootAdmin("admin", "ganti");

        assertEquals(Role.ADMIN, first.getRole());
        assertEquals(AuthUseCase.ROOT_FULL_NAME, first.getFullName());
        assertEquals(first.getId(), second.getId());
        assertEquals(1, users.findAll().size());
        assertEquals(PasswordHasher.hash("admin"), second.getPasswordHash());
    }

    @Test
    void ensureRootAdminRequiresCredentials() {
        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin(null, "x"));
        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin("  ", "x"));
        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin("admin", null));
        assertThrows(ValidationException.class, () -> useCase.ensureRootAdmin("admin", ""));
    }

    @Test
    void userRulesHelpersBehaveAsDocumented() throws Exception {
        assertEquals("", UserRules.clean(null));
        assertEquals("x", UserRules.clean(" x "));
        assertEquals("Budi", UserRules.requireFullName(" Budi "));
        assertThrows(ValidationException.class, () -> UserRules.requireFullName(null));
        UserRules.requirePassword("123456");
        assertThrows(ValidationException.class, () -> UserRules.requirePassword("123"));
    }
}
