package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Object for the Craigslist Madrid Housing page.
 *
 * Sort options are detected by their stable CSS class names
 * (e.g. cl-search-sort-mode-newest), which is language-independent —
 * the visible labels are localised ("+ nuevo", "más antiguas", etc),
 * but the underlying classes are stable across locales.
 */
public class HousingPage {

    private static final String BASE_URL = "https://madrid.craigslist.org/search/hhh";
    private static final int DEFAULT_TIMEOUT_MS = 10_000;

    /** Sort option identifiers — map friendly names to their CSS class suffixes. */
    public enum SortOption {
        NEWEST("newest"),
        OLDEST("oldest"),
        PRICE_ASC("price-asc"),
        PRICE_DESC("price-desc"),
        UPCOMING("upcoming"),
        RELEVANT("relevance"),  // appears only after search
        DISTANCE("distance");   // also appears after search on Madrid site

        private final String classSuffix;

        SortOption(String classSuffix) {
            this.classSuffix = classSuffix;
        }

        public String cssClass() {
            return "cl-search-sort-mode-" + classSuffix;
        }
    }

    private final Page page;

    // ── Locators ──────────────────────────────────────────
    private final Locator searchInput;
    private final Locator sortButton;
    private final Locator sortDropdown;
    private final Locator sortOptionsButtons;

    public HousingPage(Page page) {
        this.page = page;

        // Search input — placeholder "Buscar vivienda" on Madrid site
        this.searchInput = page.locator(
                "input[placeholder*='Buscar'], input[type='search'], input[name='query']"
        ).first();

        // Sort button — custom combo-box inside .cl-search-sort-mode
        this.sortButton = page.locator(".cl-search-sort-mode button.bd-button").first();

        // Sort dropdown container (only visible while open)
        this.sortDropdown = page.locator("div.bd-for-bd-combo-box.bd-list-box");

        // Option buttons inside the open dropdown
        this.sortOptionsButtons = sortDropdown.locator("div.items button[class*='cl-search-sort-mode-']");
    }

    // ── Actions ───────────────────────────────────────────

    /** Open the housing page directly. */
    public HousingPage open() {
        page.navigate(BASE_URL);
        page.waitForLoadState(LoadState.NETWORKIDLE);
        sortButton.waitFor(new Locator.WaitForOptions().setTimeout(DEFAULT_TIMEOUT_MS));
        return this;
    }

    /** Type a query in the search box and submit it. */
    public HousingPage search(String query) {
        searchInput.waitFor(new Locator.WaitForOptions().setTimeout(DEFAULT_TIMEOUT_MS));
        searchInput.fill(query);
        searchInput.press("Enter");
        page.waitForLoadState(LoadState.NETWORKIDLE);
        // Re-wait for the sort button after the new page loads
        sortButton.waitFor(new Locator.WaitForOptions().setTimeout(DEFAULT_TIMEOUT_MS));
        return this;
    }

    // ── Queries ───────────────────────────────────────────

    /**
     * Opens the sort dropdown and returns the list of available option
     * identifiers, derived from CSS classes (e.g. "newest", "price-asc").
     * This approach is locale-independent.
     */
    public List<String> getAvailableSortOptions() {
        openSortDropdown();

        List<String> optionIds = new ArrayList<>();
        int count = sortOptionsButtons.count();
        for (int i = 0; i < count; i++) {
            String classes = sortOptionsButtons.nth(i).getAttribute("class");
            if (classes == null) continue;
            for (String cls : classes.split("\\s+")) {
                if (cls.startsWith("cl-search-sort-mode-")) {
                    String id = cls.substring("cl-search-sort-mode-".length());
                    if (!id.isEmpty()) optionIds.add(id);
                }
            }
        }

        closeSortDropdown();
        return optionIds;
    }

    /** True if the given sort option is available. */
    public boolean hasSortOption(SortOption option) {
        return getAvailableSortOptions().contains(option.classSuffix);
    }

    // ── Internals ─────────────────────────────────────────

    private void openSortDropdown() {
        sortButton.click();
        // Wait for the dropdown to render with its option items
        sortOptionsButtons.first().waitFor(
                new Locator.WaitForOptions().setTimeout(DEFAULT_TIMEOUT_MS)
        );
    }

    private void closeSortDropdown() {
        // Press Escape — more reliable than clicking the button again
        // (which can accidentally select an option on some layouts)
        page.keyboard().press("Escape");
    }
}
