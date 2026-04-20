package pages;

/**
 * Centralised CSS selectors for the Housing page.
 *
 * <p>Keeping selectors in one place means we can react to UI changes
 * in a single file rather than hunting through page classes.
 */
final class HousingLocators {

    static final String SORT_CONTAINER       = ".cl-search-sort-mode";
    static final String SORT_BUTTON          = SORT_CONTAINER + " button.bd-button";
    static final String SORT_DROPDOWN        = "div.bd-for-bd-combo-box.bd-list-box";
    static final String SORT_OPTION_PREFIX   = "cl-search-sort-mode-";
    static final String SORT_OPTIONS         =
            SORT_DROPDOWN + " div.items button[class*='" + SORT_OPTION_PREFIX + "']";

    static final String SEARCH_INPUT =
            "input[placeholder*='Buscar'], input[type='search'], input[name='query']";

    private HousingLocators() {
        // constants holder
    }
}