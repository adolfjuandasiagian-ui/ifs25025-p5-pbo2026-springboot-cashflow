package org.delcom.ifs25025springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Kartu konten halaman (latar putih, sudut membulat, bayangan halus). */
public class PageCard extends VerticalLayout {

    public PageCard(Component... children) {
        addClassName("page-card");
        setPadding(true);
        setSpacing(true);
        add(children);
    }
}
