# Cashflow

Aplikasi pencatat pemasukan dan pengeluaran pribadi berbasis Spring Boot, Vaadin, dan PostgreSQL. Pengguna dapat mengelola transaksi serta profilnya; admin dapat meninjau transaksi pengguna lain dalam mode baca-saja.

## Menjalankan aplikasi

1. Siapkan Java 25 dan PostgreSQL, lalu buat database `pbo2026_cashflow`.
2. Salin `.env.example` menjadi `.env`. Atur kredensial database, `ROOT_USERNAME`, `ROOT_PASSWORD`, dan `COOKIE_SECRET`; gunakan nilai rahasia yang kuat dan jangan commit `.env`.
3. Jalankan aplikasi:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

Admin awal dibuat dari `ROOT_USERNAME` dan `ROOT_PASSWORD` jika akun tersebut belum ada.

## Pengujian dan build

Tes menggunakan profil `test` dan database H2:

```powershell
.\mvnw.cmd clean verify
```

Build frontend Vaadin untuk produksi:

```powershell
.\mvnw.cmd clean package -Pproduction
```

Pipeline Jenkins mencakup test dan JaCoCo, Trivy, packaging, publikasi image, serta deploy. Sesuaikan hostname/namespace registry, host deploy, dan Jenkins credentials di `Jenkinsfile` sebelum menjalankannya.
