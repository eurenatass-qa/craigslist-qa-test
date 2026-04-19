package tests;

import base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.HousingPage;
import pages.HousingPage.SortOption;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the sorting options available on the Craigslist Madrid Housing page.
 *
 * Requirements under test (from the technical test document):
 *   1. By default:       price asc, price desc, newest
 *   2. After searching:  price asc, price desc, newest, upcoming, relevant
 *
 * Implementation notes
 * --------------------
 * Sort options are identified by their stable CSS classes (e.g. cl-search-sort-mode-newest)
 * rather than their localised labels ("+ nuevo", "$ → $$$", etc). This makes the tests
 * robust against language changes.
 *
 * During development we observed that the Madrid site actually exposes 5 sort options by
 * default (newest, oldest, price-asc, price-desc, upcoming) — more than the 3 specified
 * in the requirement. The tests assert the REQUIRED options are present (which they are),
 * and log the full list of found options so any extras are visible in the report.
 */
public class HousingSortTest extends BaseTest {

    private static final String SEARCH_QUERY = "apartment";

    @Test
    @DisplayName("Default Housing page should expose price asc, price desc and newest sort options")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Opens the Madrid Craigslist Housing page and verifies the default sort " +
                 "dropdown contains the required options.")
    void defaultSortOptionsShouldBePresent() {
        HousingPage housing = new HousingPage(page).open();

        List<String> options = housing.getAvailableSortOptions();
        System.out.println("[default] sort options found: " + options);

        assertOptionPresent(housing, SortOption.NEWEST);
        assertOptionPresent(housing, SortOption.PRICE_ASC);
        assertOptionPresent(housing, SortOption.PRICE_DESC);
    }

    @Test
    @DisplayName("After searching, sort options should include relevant and upcoming")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Performs a search on the Housing page and verifies that the sort dropdown " +
                 "contains all required options, including relevant and upcoming.")
    void searchResultsShouldShowExtraSortOptions() {
        HousingPage housing = new HousingPage(page)
                .open()
                .search(SEARCH_QUERY);

        List<String> options = housing.getAvailableSortOptions();
        System.out.println("[after search] sort options found: " + options);

        // Default options still present
        assertOptionPresent(housing, SortOption.NEWEST);
        assertOptionPresent(housing, SortOption.PRICE_ASC);
        assertOptionPresent(housing, SortOption.PRICE_DESC);

        // Additional options after search
        assertOptionPresent(housing, SortOption.UPCOMING);
        assertOptionPresent(housing, SortOption.RELEVANT);
    }

    /** Asserts a sort option exists with a clear failure message. */
    private void assertOptionPresent(HousingPage housing, SortOption option) {
        List<String> found = housing.getAvailableSortOptions();
        boolean present = found.contains(option.name().toLowerCase().replace("_", "-"))
                       || housing.hasSortOption(option);
        assertTrue(
                present,
                () -> String.format(
                        "Expected sort option '%s' to be present, but available options were: %s",
                        option.name(), found
                )
        );
    }
}
