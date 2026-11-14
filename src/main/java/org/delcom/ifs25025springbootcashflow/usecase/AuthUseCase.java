package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.domain.PasswordHasher;
import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25025springbootcashflow.domain.repository.IUserRepository;

import java.util.regex.Pattern;

/**
 * Use case autentikasi: registrasi, login, dan seed admin awal.
 * Tidak melakukan I/O UI; kegagalan dilaporkan lewat checked exception domain.
 */
public class AuthUseCase {

    /** Nama lengkap admin yang dibuat otomatis dari ROOT_USERNAME / ROOT_PASSWORD. */
    public static final String ROOT_FULL_NAME = "Administrator";

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.]{3,30}$");

    private final IUserRepository userRepository;

    public AuthUseCase(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Mendaftarkan pengguna baru dengan role USER. */
    public User register(String username, String password, String fullName) throws ValidationException {
        String cleanUsername = UserRules.clean(username);
        if (!USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            throw new ValidationException(
                    "Username harus 3-30 karakter (huruf, angka, titik, atau underscore)");
        }
        String cleanFullName = UserRules.requireFullName(fullName);
        UserRules.requirePassword(password);
        if (userRepository.findByUsername(cleanUsername).isPresent()) {
            throw new ValidationException("Username sudah digunakan");
        }
        return userRepository.save(cleanUsername, PasswordHasher.hash(password), cleanFullName, Role.USER);
    }

    /** Memverifikasi username + password dan mengembalikan pengguna yang cocok. */
    public User login(String username, String password) throws ValidationException {
        return userRepository.findByUsername(UserRules.clean(username))
                .filter(user -> PasswordHasher.matches(password, user.getPasswordHash()))
                .orElseThrow(() -> new ValidationException("Username atau password salah"));
    }

    /** Membuat admin dari konfigurasi ROOT_* bila belum ada; bila sudah ada, dikembalikan apa adanya. */
    public User ensureRootAdmin(String username, String password) throws ValidationException {
        String cleanUsername = UserRules.clean(username);
        if (cleanUsername.isEmpty() || password == null || password.isEmpty()) {
            throw new ValidationException("ROOT_USERNAME dan ROOT_PASSWORD tidak boleh kosong");
        }
        return userRepository.findByUsername(cleanUsername)
                .orElseGet(() -> userRepository.save(
                        cleanUsername, PasswordHasher.hash(password), ROOT_FULL_NAME, Role.ADMIN));
    }
}
