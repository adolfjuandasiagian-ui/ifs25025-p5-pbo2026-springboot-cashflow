package org.delcom.ifs25025springbootcashflow.adapter.storage;

import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Menyimpan berkas unggahan (foto profil dan bukti transaksi) ke folder {@code UPLOAD_DIR}.
 * Nama berkas dibuat acak (UUID + ekstensi asli) dan yang dikembalikan adalah path relatif
 * {@code uploads/<uuid>.<ext>}, yang dilayani aplikasi lewat {@code /uploads/**}.
 */
public class FileStorage {

    /** Awalan path relatif yang disimpan di database dan dipakai sebagai URL. */
    public static final String PUBLIC_PREFIX = "uploads/";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membuat folder upload: " + root, e);
        }
    }

    /** Folder fisik tempat berkas disimpan (path absolut). */
    public Path getRoot() {
        return root;
    }

    /** Menyimpan berkas gambar dan mengembalikan path relatifnya, mis. {@code uploads/ab12.png}. */
    public String store(String originalFilename, byte[] data) throws ValidationException, IOException {
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Format gambar tidak didukung (gunakan PNG, JPG, GIF, atau WEBP)");
        }
        if (data == null || data.length == 0) {
            throw new ValidationException("Berkas gambar kosong");
        }
        String fileName = UUID.randomUUID() + "." + extension;
        Files.write(root.resolve(fileName), data);
        return PUBLIC_PREFIX + fileName;
    }

    /**
     * Menghapus berkas berdasarkan path relatif hasil {@link #store}.
     * Mengembalikan true jika berkas ada dan terhapus; path di luar folder upload ditolak.
     * Kegagalan I/O dilaporkan sebagai {@link UncheckedIOException}.
     */
    public boolean delete(String relativePath) {
        if (relativePath == null || !relativePath.startsWith(PUBLIC_PREFIX)) {
            return false;
        }
        Path target = root.resolve(relativePath.substring(PUBLIC_PREFIX.length())).normalize();
        if (!target.startsWith(root)) {
            return false;
        }
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menghapus berkas unggahan: " + relativePath, e);
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
