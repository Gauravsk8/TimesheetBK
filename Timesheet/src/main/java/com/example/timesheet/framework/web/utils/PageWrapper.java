package com.example.timesheet.framework.web.utils;


import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import java.util.List;

@SuppressWarnings("rawtypes")
public class PageWrapper {

    private PageImpl pageImpl;

    public PageWrapper(PageImpl pageImpl) {
        this.pageImpl = pageImpl;
    }

    public List getContent() {
        return pageImpl.getContent();
    }

    public long getTotalElements() {
        return pageImpl.getTotalElements();
    }

    public int getTotalPages() {
        return pageImpl.getTotalPages();
    }

    public boolean isLast() {
        return pageImpl.isLast();
    }

    public Sort getSort() {
        return pageImpl.getSort();
    }

    public boolean isFirst() {
        return pageImpl.isFirst();
    }

    public int getNumberOfElements() {
        return pageImpl.getNumberOfElements();
    }

    public int getSize() {
        return pageImpl.getSize();
    }

    public int getNumber() {
        return pageImpl.getNumber() + 1;
    }
}