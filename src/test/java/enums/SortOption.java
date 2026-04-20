package enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Craigslist sort options, identified by the stable CSS class suffix
 * (e.g. {@code cl-search-sort-mode-newest} → {@code "newest"}).
 *
 * <p>Using CSS-class identifiers rather than localised labels makes the
 * tests robust across the multiple locales Craigslist supports.
 */
public enum SortOption {
    NEWEST("newest"),
    OLDEST("oldest"),
    PRICE_ASC("price-asc"),
    PRICE_DESC("price-desc"),
    UPCOMING("upcoming"),
    RELEVANT("relevance"),
    DISTANCE("distance");

    private final String classSuffix;

    SortOption(String classSuffix) {
        this.classSuffix = classSuffix;
    }

    public String classSuffix() {
        return classSuffix;
    }

    /**
     * Reverse lookup: find an enum by its CSS class suffix.
     * Returns {@link Optional#empty()} when the site exposes a suffix
     * that hasn't yet been modelled here (graceful degradation).
     */
    public static Optional<SortOption> fromClassSuffix(String suffix) {
        return Arrays.stream(values())
                .filter(opt -> opt.classSuffix.equals(suffix))
                .findFirst();
    }
}