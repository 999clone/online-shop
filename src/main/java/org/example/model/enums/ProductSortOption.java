package org.example.model.enums;

/**
 * Valid sort options according to User Story 12.
 */
public enum ProductSortOption {
    CHEAPEST("ارزان‌ترین"),
    MOST_EXPENSIVE("گران‌ترین"),
    NEWEST("جدیدترین"),
    POPULAR("محبوب‌ترین"),
    HIGHEST_RATING("بیشترین امتیاز");

    private final String persianLabel;

    ProductSortOption(String persianLabel) {
        this.persianLabel = persianLabel;
    }

    public String getPersianLabel() {
        return persianLabel;
    }
}
