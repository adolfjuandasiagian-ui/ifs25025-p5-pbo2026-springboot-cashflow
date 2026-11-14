package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;

import java.util.List;

/** Use case pengelolaan pengguna: daftar user, ambil user, ubah profil, dan ubah foto profil. */
public class UserUseCase {

    private final IUserRepository userRepository;

    public UserUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Daftar seluruh pengguna (dipakai halaman admin). */
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    /** Mengambil pengguna berdasarkan ID. */
    public User getUser(int id) throws EntityNotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pengguna", id));
    }

    /**
     * Mengubah nama lengkap dan (opsional) password.
     * Password baru yang kosong/blank berarti password tidak diubah.
     */
    public User updateProfile(int id, String fullName, String newPassword)
            throws EntityNotFoundException, ValidationException {
        User user = getUser(id);
        String cleanFullName = UserRules.requireFullName(fullName);
        boolean changePassword = newPassword != null && !newPassword.isBlank();
        if (changePassword) {
            UserRules.requirePassword(newPassword);
        }
        user.changeFullName(cleanFullName);
        if (changePassword) {
            user.changePasswordHash(PasswordHasher.hash(newPassword));
        }
        userRepository.update(user);
        return user;
    }

    /** Menyimpan path relatif foto profil baru. */
    public User updatePhoto(int id, String photoPath) throws EntityNotFoundException {
        User user = getUser(id);
        user.changePhotoPath(photoPath);
        userRepository.update(user);
        return user;
    }
}
