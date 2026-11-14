package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

/** Pembungkus Upload gambar (1 berkas, maks 5 MB) yang menahan hasil unggahan di memori. */
public final class ImageUpload {

    public static final int MAX_BYTES = 5 * 1024 * 1024;

    private final Upload upload;
    private String fileName;
    private byte[] data;

    public ImageUpload() {
        upload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            fileName = metadata.fileName();
            data = bytes;
        }));
        upload.setMaxFiles(1);
        upload.setMaxFileSize(MAX_BYTES);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp",
                ".png", ".jpg", ".jpeg", ".gif", ".webp");
        upload.addFileRemovedListener(e -> clear());
        upload.addFileRejectedListener(e -> Toasts.error("[!] " + e.getErrorMessage()));
    }

    public Upload component() {
        return upload;
    }

    public boolean hasFile() {
        return data != null;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public void clear() {
        fileName = null;
        data = null;
    }
}
