package org.example.service.sorting;

import org.example.model.entity.Product;
import org.example.model.enums.ProductSortOption;
import org.example.service.rating.ProductRatingProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PopularSortStrategy implements ProductSortStrategy {
    private final ProductRatingProvider ratingProvider;

    public PopularSortStrategy(ProductRatingProvider ratingProvider) {
        this.ratingProvider = ratingProvider;
    }

    @Override
    public ProductSortOption getOption() {
        return ProductSortOption.POPULAR;
    }

    @Override
    public List<Product> sort(List<Product> products) {
        List<Product> sorted = new ArrayList<>(products);
        sorted.sort(Comparator.comparingInt((Product p) -> ratingProvider.getReviewCount(p.getId())).reversed());
        return sorted;
    }
}
