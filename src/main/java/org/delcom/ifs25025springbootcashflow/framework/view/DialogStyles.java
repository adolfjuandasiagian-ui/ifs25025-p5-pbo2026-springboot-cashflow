package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;

/**
 * Pengaturan dialog yang seragam: judul + badge informatif di header.
 * Warna header (#2563EB) dan footer (#F3F4F6, tombol rata kanan) diatur di styles.css.
 */
public final class DialogStyles {

    private DialogStyles() {
    }

    /** Mengatur judul, badge, dan lebar dialog; mengembalikan badge agar teksnya bisa diperbarui. */
    public static Span decorate(Dialog dialog, String title, String badgeText, String width) {
        dialog.setHeaderTitle(title);
        Span badge = new Span(badgeText);
        badge.addClassName("dialog-badge");
        dialog.getHeader().add(badge);
        dialog.setWidth(width);
        dialog.setMaxWidth("95vw");
        return badge;
    }
}
