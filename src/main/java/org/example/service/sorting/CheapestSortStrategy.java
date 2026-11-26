package org.example.service.sorting;

import org.example.model.entity.Product;
import org.example.model.enums.ProductSortOption;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CheapestSortStrategy implements ProductSortStrategy {
    @Override
    public ProductSortOption getOption() {
        return ProductSortOption.CHEAPEST;
    }

    @Override
    public List<Product> sort(List<Product> products) {
        List<Product> sorted = new ArrayList<>(products);
        sorted.sort(Comparator.comparingDouble(Product::getPrice));
        return sorted;
    }
}
