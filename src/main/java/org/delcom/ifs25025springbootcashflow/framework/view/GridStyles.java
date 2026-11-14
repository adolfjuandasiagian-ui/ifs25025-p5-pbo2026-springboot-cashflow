package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;

/** Gaya grid yang konsisten di semua halaman. */
public final class GridStyles {

    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addClassName("cashflow-grid");
        grid.setWidthFull();
        grid.setHeight("calc(100vh - 400px)");
        grid.setMinHeight("320px");
    }
}
