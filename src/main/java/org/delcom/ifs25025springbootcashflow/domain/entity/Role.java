package org.delcom.ifs25025springbootcashflow.domain.entity;

/** Peran pengguna aplikasi. */
public enum Role {
    /** Administrator: dapat melihat daftar pengguna dan transaksi pengguna lain (read-only). */
    ADMIN,

    /** Pengguna biasa: hanya dapat mengelola data miliknya sendiri. */
    USER
}
