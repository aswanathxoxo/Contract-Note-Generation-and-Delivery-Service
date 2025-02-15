package com.gtl.utils;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

public class HtmlToPDFOptions {

    private Rectangle pageSize;

    public HtmlToPDFOptions() {
        this.pageSize = PageSize.A4;
    }

    public HtmlToPDFOptions(String option) {
        switch (option.toUpperCase()) {
            case "A4":
                pageSize = PageSize.A4;
                break;
            case "A3":
                pageSize = PageSize.A3;
                break;
            case "A3R":
                pageSize = PageSize.A3.rotate();
                break;
            default:
                pageSize = PageSize.A4.rotate();
                break;
        }
    }

    public Rectangle getPageSize() {
        return pageSize;
    }

    public void setPageSize(Rectangle pageSize) {
        this.pageSize = pageSize;
    }
}
