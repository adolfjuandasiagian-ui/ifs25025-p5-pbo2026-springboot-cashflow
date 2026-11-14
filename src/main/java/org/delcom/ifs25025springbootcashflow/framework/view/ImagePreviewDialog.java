package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.Locale;

/**
 * Modal pratinjau gambar dengan zoom interaktif.
 * Klik gambar untuk toggle zoom 1.0x dan 1.8x; tombol Perkecil / Perbesar / Tutup ada di
 * footer sebelah kanan. Area gambar bisa di-scroll saat diperbesar.
 */
public class ImagePreviewDialog extends Dialog {

    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 3.0;
    private static final double STEP = 0.2;
    private static final double ZOOMED_IN = 1.8;

    private final Image image;
    private final Span zoomBadge;
    private final Button zoomOut = new Button("Perkecil", VaadinIcon.SEARCH_MINUS.create());
    private final Button zoomIn = new Button("Perbesar", VaadinIcon.SEARCH_PLUS.create());
    private double zoom = 1.0;

    public ImagePreviewDialog(String imagePath, String title) {
        zoomBadge = DialogStyles.decorate(this, title, "Zoom 1.0x", "min(900px, 95vw)");

        image = new Image(imagePath, title);
        image.getStyle()
                .set("display", "block")
                .set("max-width", "none")
                .set("height", "auto")
                .set("border-radius", "8px")
                .set("transition", "width 0.15s ease");
        image.getElement().setAttribute("role", "button");
        image.getElement().setAttribute("tabindex", "0");
        image.getElement().setAttribute("aria-label", "Ubah zoom gambar " + title);
        image.getElement().setAttribute("aria-pressed", "false");
        image.getElement().addEventListener("click", e -> toggleZoom());
        image.getElement().addEventListener("keydown", e -> toggleZoom())
                .setFilter("event.key === 'Enter' || event.key === ' '");

        Div viewport = new Div(image);
        viewport.getStyle()
                .set("overflow", "auto")
                .set("max-height", "65vh")
                .set("max-width", "100%");
        add(viewport);

        zoomOut.addClickListener(e -> setZoom(zoom - STEP));
        zoomIn.addClickListener(e -> setZoom(zoom + STEP));
        Button close = new Button("Tutup", VaadinIcon.CLOSE.create(), e -> close());
        getFooter().add(zoomOut, zoomIn, close);

        applyZoom();
    }

    private void toggleZoom() {
        setZoom(zoom > 1.0 ? 1.0 : ZOOMED_IN);
    }

    private void setZoom(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, rounded));
        applyZoom();
    }

    private void applyZoom() {
        image.getStyle()
                .set("width", (zoom * 100) + "%")
                .set("cursor", zoom > 1.0 ? "zoom-out" : "zoom-in");
        image.getElement().setAttribute("aria-pressed", String.valueOf(zoom > 1.0));
        zoomBadge.setText(String.format(Locale.US, "Zoom %.1fx", zoom));
        zoomOut.setEnabled(zoom > MIN_ZOOM);
        zoomIn.setEnabled(zoom < MAX_ZOOM);
    }
}
