package org.delcom.ifs25025springbootcashflow.usecase;

import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;

/** Aturan validasi data akun yang dipakai bersama oleh {@link AuthUseCase} dan {@link UserUseCase}. */
final class UserRules {

    static final int MIN_PASSWORD_LENGTH = 6;
    static final int MAX_FULL_NAME_LENGTH = 200;

    private UserRules() {
    }

    /** Mengubah null menjadi string kosong dan memangkas spasi di tepi. */
    static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    /** Memastikan nama lengkap terisi dan tidak terlalu panjang; mengembalikan nilai yang sudah dipangkas. */
    static String requireFullName(String fullName) throws ValidationException {
        String name = clean(fullName);
        if (name.isEmpty()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        if (name.length() > MAX_FULL_NAME_LENGTH) {
            throw new ValidationException("Nama lengkap maksimal " + MAX_FULL_NAME_LENGTH + " karakter");
        }
        return name;
    }

    /** Memastikan password memenuhi panjang minimum. */
    static void requirePassword(String password) throws ValidationException {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password minimal " + MIN_PASSWORD_LENGTH + " karakter");
        }
    }
}
