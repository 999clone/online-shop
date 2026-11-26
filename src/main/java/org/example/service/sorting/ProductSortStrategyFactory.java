package org.example.service.sorting;

import org.example.model.enums.ProductSortOption;
import org.example.model.exception.ValidationException;
import org.example.service.rating.ProductRatingProvider;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry/Factory resolving {@link ProductSortOption} to {@link ProductSortStrategy} (OCP).
 */
public class ProductSortStrategyFactory {
    private final Map<ProductSortOption, ProductSortStrategy> strategies = new EnumMap<>(ProductSortOption.class);

    public ProductSortStrategyFactory(List<ProductSortStrategy> strategyList) {
        for (ProductSortStrategy strategy : strategyList) {
            strategies.put(strategy.getOption(), strategy);
        }
    }

    public static ProductSortStrategyFactory createDefault(ProductRatingProvider ratingProvider) {
        return new ProductSortStrategyFactory(List.of(
                new CheapestSortStrategy(),
                new MostExpensiveSortStrategy(),
                new NewestSortStrategy(),
                new HighestRatingSortStrategy(ratingProvider),
                new PopularSortStrategy(ratingProvider)
        ));
    }

    public ProductSortStrategy getStrategy(ProductSortOption option) {
        if (option == null || !strategies.containsKey(option)) {
            throw new ValidationException("گزینه مرتب‌سازی نامعتبر است. فقط گزینه‌های مشخص‌شده قابل انتخاب هستند.");
        }
        return strategies.get(option);
    }
}
