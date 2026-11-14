package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25025springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserUseCaseTest {

    private UserUseCase useCase;
    private int userId;

    @BeforeEach
    void setUp() {
        UserRepository repository = new UserRepository();
        useCase = new UserUseCase(repository);
        userId = repository.save("budi", PasswordHasher.hash("lama123"), "Budi", Role.USER).getId();
    }

    @Test
    void listAndGetUsers() throws Exception {
        assertEquals(1, useCase.listUsers().size());
        assertEquals("budi", useCase.getUser(userId).getUsername());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> useCase.getUser(99));
        assertEquals(99, ex.getId());
    }

    @Test
    void updateProfileChangesNameAndKeepsPasswordWhenBlank() throws Exception {
        String oldHash = useCase.getUser(userId).getPasswordHash();

        assertEquals("Budi Baru", useCase.updateProfile(userId, " Budi Baru ", null).getFullName());
        assertEquals(oldHash, useCase.getUser(userId).getPasswordHash());
        useCase.updateProfile(userId, "Budi Baru", "   ");
        assertEquals(oldHash, useCase.getUser(userId).getPasswordHash());
    }

    @Test
    void updateProfileChangesPassword() throws Exception {
        User updated = useCase.updateProfile(userId, "Budi", "baru1234");
        assertEquals(PasswordHasher.hash("baru1234"), updated.getPasswordHash());
    }

    @Test
    void updateProfileValidatesInput() {
        assertThrows(EntityNotFoundException.class, () -> useCase.updateProfile(99, "Budi", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(userId, " ", null));
        assertThrows(ValidationException.class, () -> useCase.updateProfile(userId, "Budi", "123"));
    }

    @Test
    void updatePhotoStoresPath() throws Exception {
        assertEquals("uploads/x.png", useCase.updatePhoto(userId, "uploads/x.png").getPhotoPath());
        assertEquals("uploads/x.png", useCase.getUser(userId).getPhotoPath());
        assertThrows(EntityNotFoundException.class, () -> useCase.updatePhoto(99, "uploads/x.png"));
    }
}
