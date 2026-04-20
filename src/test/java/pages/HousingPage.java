package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import config.TestConfig;
import enums.SortOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Object for the Craigslist Housing listing page.
 *
 * <p>Locators live in {@link HousingLocators}. This class exposes
 * high-level intent ({@code open}, {@code search}, {@code readVisibleSortOptions})
 * so tests describe <em>what</em> is being verified, not <em>how</em>.
 *
 * <p>Sort options are detected by their stable CSS class suffixes
 * (e.g. {@code cl-search-sort-mode-newest}), making detection
 * locale-independent.
 */
public class HousingPage {

    private final Page page;
    private final Locator searchInput;
    private final Locator sortButton;
    private final Locator sortOptions;

    public HousingPage(Page page) {
        this.page = page;
        this.searchInput  = page.locator(HousingLocators.SEARCH_INPUT).first();
        this.sortButton   = page.locator(HousingLocators.SORT_BUTTON).first();
        this.sortOptions  = page.locator(HousingLocators.SORT_OPTIONS);
    }

    // ── Actions ────────────────────────────────────────────────────────────

    /** Navigate to the housing listing page. */
    public HousingPage open() {
        page.navigate(TestConfig.BASE_URL + "/search/hhh");
        waitForPageReady();
        return this;
    }

    /** Submit a search query and wait for the results page to be ready. */
    public HousingPage search(String query) {
        searchInput.waitFor(waitOptions());
        searchInput.fill(query);
        searchInput.press("Enter");
        waitForPageReady();
        return this;
    }

    // ── Queries ────────────────────────────────────────────────────────────

    /**
     * Opens the sort dropdown, reads the available options, then closes
     * the dropdown. Results are returned as strongly-typed {@link SortOption}s
     * — unknown class suffixes are logged and skipped.
     *
     * <p>Named with a {@code read} prefix to signal that this performs I/O
     * (unlike a pure getter), and returns a modelled list so callers can
     * use enum equality rather than string comparison.
     */
    public List<SortOption> readVisibleSortOptions() {
        List<String> rawSuffixes = readVisibleSortOptionSuffixes();

        List<SortOption> modelled = new ArrayList<>(rawSuffixes.size());
        for (String suffix : rawSuffixes) {
            SortOption.fromClassSuffix(suffix).ifPresent(modelled::add);
        }
        return modelled;
    }

    /**
     * Raw list of sort option suffixes as they appear on the page.
     * Useful for reporting/logging: if Craigslist adds an option we
     * haven't modelled yet, it will show up here but not in
     * {@link #readVisibleSortOptions()}.
     */
    public List<String> readVisibleSortOptionSuffixes() {
        openSortDropdown();
        try {
            List<String> suffixes = new ArrayList<>();
            int count = sortOptions.count();
            for (int i = 0; i < count; i++) {
                String classes = sortOptions.nth(i).getAttribute("class");
                if (classes == null) continue;
                for (String cls : classes.split("\\s+")) {
                    if (cls.startsWith(HousingLocators.SORT_OPTION_PREFIX)) {
                        String suffix = cls.substring(HousingLocators.SORT_OPTION_PREFIX.length());
                        if (!suffix.isEmpty()) {
                            suffixes.add(suffix);
                        }
                    }
                }
            }
            return suffixes;
        } finally {
            closeSortDropdown();
        }
    }

    // ── Internals ──────────────────────────────────────────────────────────

    private void waitForPageReady() {
        // Wait for the specific element we care about rather than NETWORKIDLE,
        // which is unreliable on sites that poll analytics/websockets.
        sortButton.waitFor(waitOptions());
    }

    private void openSortDropdown() {
        sortButton.click();
        sortOptions.first().waitFor(waitOptions());
    }

    private void closeSortDropdown() {
        // Escape is more reliable than re-clicking the trigger, which can
        // accidentally select an option on some layouts.
        page.keyboard().press("Escape");
    }

    private static Locator.WaitForOptions waitOptions() {
        return new Locator.WaitForOptions().setTimeout(TestConfig.DEFAULT_TIMEOUT.toMillis());
    }
}