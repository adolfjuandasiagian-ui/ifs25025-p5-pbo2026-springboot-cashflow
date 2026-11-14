package org.delcom.ifs25025springbootcashflow.adapter;

import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageTest {

    private static final byte[] PNG = {1, 2, 3, 4};

    private Path newDir() throws Exception {
        return Files.createTempDirectory("cashflow-upload-test");
    }

    @Test
    void storesFileWithRandomNameAndKeepsExtension() throws Exception {
        Path dir = newDir();
        FileStorage storage = new FileStorage(dir.toString());

        String path = storage.store("Bukti Saya.PNG", PNG);

        assertTrue(path.startsWith(FileStorage.PUBLIC_PREFIX));
        assertTrue(path.endsWith(".png"));
        assertFalse(path.contains("Bukti"));
        assertArrayEquals(PNG, Files.readAllBytes(storage.getRoot().resolve(path.substring(FileStorage.PUBLIC_PREFIX.length()))));
    }

    @Test
    void rejectsUnsupportedOrEmptyFiles() throws Exception {
        FileStorage storage = new FileStorage(newDir().toString());

        assertThrows(ValidationException.class, () -> storage.store("virus.exe", PNG));
        assertThrows(ValidationException.class, () -> storage.store("tanpa-ekstensi", PNG));
        assertThrows(ValidationException.class, () -> storage.store(null, PNG));
        assertThrows(ValidationException.class, () -> storage.store("a.png", null));
        assertThrows(ValidationException.class, () -> storage.store("a.png", new byte[0]));
    }

    @Test
    void deleteRemovesOnlyFilesInsideUploadFolder() throws Exception {
        Path dir = newDir();
        FileStorage storage = new FileStorage(dir.toString());
        String path = storage.store("a.jpg", PNG);

        assertFalse(storage.delete(null));
        assertFalse(storage.delete("lain/a.jpg"));
        assertFalse(storage.delete("uploads/../../etc/passwd"));
        assertFalse(storage.delete("uploads/tidak-ada.png"));
        assertTrue(storage.delete(path));
        assertFalse(storage.delete(path));
    }

    @Test
    void deleteReturnsFalseWhenFileCannotBeRemoved() throws Exception {
        Path dir = newDir();
        FileStorage storage = new FileStorage(dir.toString());
        Path folder = Files.createDirectory(storage.getRoot().resolve("isi"));
        Files.write(folder.resolve("x.txt"), PNG);

        assertFalse(storage.delete("uploads/isi"));
    }

    @Test
    void failsFastWhenUploadFolderCannotBeCreated() throws Exception {
        Path file = Files.createTempFile("cashflow-not-a-dir", ".tmp");
        assertThrows(UncheckedIOException.class, () -> new FileStorage(file.resolve("sub").toString()));
    }
}
