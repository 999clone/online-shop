package org.example.service.sorting;

import org.example.model.entity.Product;
import org.example.model.enums.ProductSortOption;
import org.example.service.rating.ProductRatingProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HighestRatingSortStrategy implements ProductSortStrategy {
    private final ProductRatingProvider ratingProvider;

    public HighestRatingSortStrategy(ProductRatingProvider ratingProvider) {
        this.ratingProvider = ratingProvider;
    }

    @Override
    public ProductSortOption getOption() {
        return ProductSortOption.HIGHEST_RATING;
    }

    @Override
    public List<Product> sort(List<Product> products) {
        List<Product> sorted = new ArrayList<>(products);
        sorted.sort(Comparator.comparingDouble((Product p) -> ratingProvider.getAverageRating(p.getId())).reversed());
        return sorted;
    }
}
