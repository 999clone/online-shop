package org.example.service.sorting;

import org.example.model.entity.Product;
import org.example.model.enums.ProductSortOption;

import java.util.List;

/**
 * Strategy interface for product sorting (OCP & LSP).
 */
public interface ProductSortStrategy {
    ProductSortOption getOption();
    List<Product> sort(List<Product> products);
}
