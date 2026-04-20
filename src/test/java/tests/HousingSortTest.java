package tests;

import base.BaseTest;
import enums.SortOption;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import pages.HousingPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the sorting options available on the Craigslist Madrid Housing page.
 *
 * <h2>Requirements under test</h2>
 * <ol>
 *   <li>Default page: {@code price-asc}, {@code price-desc}, {@code newest}</li>
 *   <li>After search: adds {@code upcoming} and {@code relevance}</li>
 * </ol>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>Sort options are identified by their stable CSS class suffixes,
 *       making detection locale-independent.</li>
 *   <li>The live Madrid site exposes extra options beyond the requirement
 *       ({@code oldest}, {@code distance}). The tests assert the required
 *       options are <em>present</em> and attach the full observed list to
 *       the Allure report for transparency.</li>
 *   <li>{@code assertAll} is used so every missing option is reported in a
 *       single run, rather than failing on the first one.</li>
 * </ul>
 */
@Feature("Housing — Sort options")
public class HousingSortTest extends BaseTest {

    private static final String SEARCH_QUERY = "apartment";

    @Test
    @Tags({@Tag("smoke"), @Tag("sorting")})
    @Story("Default sort options")
    @DisplayName("Given default housing page, when sort dropdown opened, then required options are present")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Opens the Madrid Craigslist Housing page and verifies the default " +
            "sort dropdown contains the required options (price-asc, price-desc, newest).")
    void defaultSortOptionsShouldBePresent() {
        HousingPage housing = new HousingPage(page).open();

        List<String> rawOptions = housing.readVisibleSortOptionSuffixes();
        Allure.addAttachment("Sort options observed (default)", rawOptions.toString());

        List<SortOption> options = housing.readVisibleSortOptions();

        assertAll("Required default sort options",
                () -> assertSortOptionPresent(options, SortOption.NEWEST),
                () -> assertSortOptionPresent(options, SortOption.PRICE_ASC),
                () -> assertSortOptionPresent(options, SortOption.PRICE_DESC)
        );
    }

    @Test
    @Tags({@Tag("regression"), @Tag("sorting")})
    @Story("Sort options after search")
    @DisplayName("Given a housing search, when sort dropdown opened, then relevance and upcoming are present")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Performs a housing search and verifies the sort dropdown contains " +
            "all required options, including relevance and upcoming.")
    void searchResultsShouldShowExtraSortOptions() {
        HousingPage housing = new HousingPage(page)
                .open()
                .search(SEARCH_QUERY);

        List<String> rawOptions = housing.readVisibleSortOptionSuffixes();
        Allure.addAttachment("Sort options observed (after search)", rawOptions.toString());

        List<SortOption> options = housing.readVisibleSortOptions();

        assertAll("Required sort options after search",
                // Defaults still present
                () -> assertSortOptionPresent(options, SortOption.NEWEST),
                () -> assertSortOptionPresent(options, SortOption.PRICE_ASC),
                () -> assertSortOptionPresent(options, SortOption.PRICE_DESC),
                // Added after search
                () -> assertSortOptionPresent(options, SortOption.UPCOMING),
                () -> assertSortOptionPresent(options, SortOption.RELEVANT)
        );
    }

    /** Asserts a sort option is present with a descriptive failure message. */
    private static void assertSortOptionPresent(List<SortOption> found, SortOption expected) {
        assertTrue(
                found.contains(expected),
                () -> String.format(
                        "Expected sort option '%s' (class suffix '%s') to be present, " +
                                "but available options were: %s",
                        expected.name(), expected.classSuffix(), found)
        );
    }
}