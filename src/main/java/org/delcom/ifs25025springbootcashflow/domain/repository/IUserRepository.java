package org.delcom.ifs25025springbootcashflow.domain.repository;

import org.delcom.ifs25025springbootcashflow.domain.entity.Role;
import org.delcom.ifs25025springbootcashflow.domain.entity.User;

import java.util.Optional;

/** Port spesifik User yang memperluas generic {@link IRepository}. */
public interface IUserRepository extends IRepository<User, Integer> {
    /** Membuat dan menyimpan pengguna baru; ID dibuat otomatis. */
    User save(String username, String passwordHash, String fullName, Role role);

    /** Mencari pengguna berdasarkan username (persis sama). */
    Optional<User> findByUsername(String username);
}
