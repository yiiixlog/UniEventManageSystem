package com.campusevent.ui;

import com.campusevent.model.Registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class RegistrationPager {
    private final int pageSize;
    private int pageIndex;

    RegistrationPager(int pageSize) {
        this.pageSize = pageSize;
    }

    void reset() {
        pageIndex = 0;
    }

    void previous() {
        if (pageIndex > 0) {
            pageIndex--;
        }
    }

    void next(int totalRecords) {
        if (pageIndex < getTotalPages(totalRecords) - 1) {
            pageIndex++;
        }
    }

    void clamp(int totalRecords) {
        int totalPages = getTotalPages(totalRecords);
        if (totalPages == 0) {
            pageIndex = 0;
            return;
        }
        if (pageIndex >= totalPages) {
            pageIndex = totalPages - 1;
        }
        if (pageIndex < 0) {
            pageIndex = 0;
        }
    }

    Page getPage(List<Registration> records) {
        int totalRecords = records.size();
        int totalPages = getTotalPages(totalRecords);
        if (totalRecords == 0) {
            return new Page(Collections.emptyList(), "無紀錄", true, true);
        }

        int fromIndex = pageIndex * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalRecords);
        List<Registration> pageRecords = new ArrayList<>(records.subList(fromIndex, toIndex));
        String label = "第 " + (pageIndex + 1) + " / " + totalPages + " 頁";
        boolean previousDisabled = pageIndex == 0;
        boolean nextDisabled = pageIndex >= totalPages - 1;
        return new Page(pageRecords, label, previousDisabled, nextDisabled);
    }

    private int getTotalPages(int totalRecords) {
        if (totalRecords == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalRecords / pageSize);
    }

    static final class Page {
        private final List<Registration> records;
        private final String label;
        private final boolean previousDisabled;
        private final boolean nextDisabled;

        private Page(List<Registration> records, String label, boolean previousDisabled, boolean nextDisabled) {
            this.records = records;
            this.label = label;
            this.previousDisabled = previousDisabled;
            this.nextDisabled = nextDisabled;
        }

        List<Registration> getRecords() {
            return records;
        }

        String getLabel() {
            return label;
        }

        boolean isPreviousDisabled() {
            return previousDisabled;
        }

        boolean isNextDisabled() {
            return nextDisabled;
        }
    }
}
