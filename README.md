# Craigslist Housing — UI Tests

Browser-automation tests for the **Craigslist Madrid Housing** page, written in **Java + Playwright** with **JUnit 5** and **Allure** reporting.

---

## What is tested

The test suite verifies the **sorting functionality** of the Housing listing page:

| Scenario | Sort options verified as present |
|----------|----------------------------------|
| **Default page load** | `price-asc`, `price-desc`, `newest` |
| **After a search** | `price-asc`, `price-desc`, `newest`, **`upcoming`**, **`relevance`** |

Tests are located in [`src/test/java/tests/HousingSortTest.java`](src/test/java/tests/HousingSortTest.java).

> **Note:** During development, the live Madrid site was observed to expose more sort options than those specified in the original requirement (see [Observations](#observations-during-testing) below). The tests assert that the *required* options are present while logging the full list of observed options for transparency.

---

## Tech stack

| Layer | Choice | Why |
|-------|--------|-----|
| Language | **Java 17** | Stable, recommended in the assignment |
| Browser automation | **Playwright 1.45** | Fast, reliable, modern (recommended in the assignment) |
| Test framework | **JUnit 5** | Industry standard |
| Build tool | **Maven** | Universally supported, easy onboarding |
| Reporting | **Allure** | Rich HTML reports with steps, severity and failure context |

---

## Project structure

```
craigslist-qa-test/
├── pom.xml                                 ← Maven dependencies & plugins
├── README.md                               ← You are here
└── src/test/java/
    ├── base/
    │   └── BaseTest.java                   ← Browser lifecycle (Playwright + JUnit5 hooks)
    ├── pages/
    │   └── HousingPage.java                ← Page Object Model for Housing page
    └── tests/
        └── HousingSortTest.java            ← Actual test scenarios
```

### Design patterns and decisions

- **Page Object Model (POM)** — selectors and page actions live in `HousingPage`, tests stay focused on *what* is being verified, not *how*.
- **Base test class** — `BaseTest` owns the Playwright/Browser lifecycle so tests don't repeat boilerplate.
- **Fluent API** — page methods return `this` (`open().search("apartment")`), making tests read like sentences.
- **Sort options modelled as a Java `enum`** — each option maps to its stable CSS class suffix. Tests reference `SortOption.NEWEST` rather than magic strings, which gives compile-time safety and keeps the Page Object self-documenting.
- **CSS-class-based detection (locale-independent)** — the Madrid site is in Spanish (`"+ nuevo"`, `"$ → $$$"`, etc). Matching on visible text would break if the site added other languages. Instead, the tests read stable CSS classes like `cl-search-sort-mode-newest`, which are language-independent.
- **Encapsulated dropdown handling** — opening and closing the custom combo-box is handled by private helpers in `HousingPage` so tests never leak UI implementation details.

---

## Prerequisites

- **Java 17+** — verify with `java -version`
- **Maven 3.8+** — verify with `mvn -version`
- Internet connection (to download Playwright browsers and reach craigslist.org)

---

## How to run the tests

### 1. Clone the repository
```bash
git clone https://github.com/<your-username>/craigslist-qa-test.git
cd craigslist-qa-test
```

### 2. Run all tests
```bash
mvn test
```
*(Playwright automatically downloads Chromium on the first run — no extra step needed.)*

### 3. Run with a visible browser (helpful for debugging)
```bash
HEADLESS=false mvn test
```

### 4. Generate and open the Allure report
```bash
mvn allure:report
# then open target/site/allure-maven-plugin/index.html
```

If you have the Allure CLI installed, you can also run:
```bash
allure serve target/allure-results
```

---

## Understanding test failures

Every assertion produces a rich error message that shows:

1. **Which sort option was expected** (e.g. `'RELEVANT'`)
2. **All sort options that were actually found** on the page

Example failure message:
```
Expected sort option 'RELEVANT' to be present, but available options were:
[newest, oldest, price-asc, price-desc, upcoming]
```

This makes it immediately clear whether:
- the page didn't load properly
- the search wasn't applied
- Craigslist changed the available sort options

---

## Observations during testing

When the tests were run against the live site, the sort dropdown exposed **more options** than the requirement specified:

| State | Required by assignment | Actually observed on site |
|-------|------------------------|----------------------------|
| Default | 3 options | `newest, oldest, price-asc, price-desc, upcoming` (5) |
| After search | 5 options | `newest, oldest, distance, price-asc, price-desc, relevance, upcoming` (7) |

**Design decision:** the tests were written to assert that the **required** options are present — which they are — rather than asserting an exact count or exact match. This keeps the test suite honest to the requirement while being tolerant of additions the product team may have shipped after the spec was written. The full observed list is logged in the test output for full transparency.

---

## How to add new tests

1. Add new locators or actions to `HousingPage.java` (or create a new Page Object for a new page).
2. Add a new `@Test` method in `HousingSortTest.java` (or create a new test class extending `BaseTest`).
3. Use the fluent `HousingPage` API to keep tests readable.
4. Run `mvn test` to verify.

---

## Implementation notes

- Each test gets a **fresh `BrowserContext`**, ensuring no cookies/state leak between tests.
- The `Browser` instance is shared across the test class for performance, then closed in `@AfterAll`.
- Tests are **headless by default** (CI-friendly), but easily toggled via the `HEADLESS=false` env var.
- The Page Object exposes high-level intent (`hasSortOption(SortOption.NEWEST)`) instead of leaking CSS selectors into tests.
- Dropdowns are closed by pressing `Escape` rather than re-clicking the trigger — more reliable and avoids accidentally selecting an option.

---

**Author:** Renata de Sousa Santos
**Contact:** rehsousa.24@gmail.com
