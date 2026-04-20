# Craigslist Housing — UI Tests

Browser-automation tests for the **Craigslist Madrid Housing** page, written in **Java + Playwright** with **JUnit 5** and **Allure** reporting.

---

## What is tested

The suite verifies the **sorting functionality** of the Housing listing page:

| Scenario | Sort options verified as present |
|----------|----------------------------------|
| **Default page load** | `price-asc`, `price-desc`, `newest` |
| **After a search** | `price-asc`, `price-desc`, `newest`, **`upcoming`**, **`relevance`** |

> The live Madrid site exposes additional options (`oldest`, `distance`). The tests assert the *required* options are present while attaching the full observed list to the Allure report for transparency.

---

## Tech stack

| Layer | Choice | Why |
|-------|--------|-----|
| Language | **Java 17** | Stable LTS, recommended in the assignment |
| Browser automation | **Playwright 1.45** | Built-in auto-wait, modern API, recommended in the assignment |
| Test framework | **JUnit 5** | Industry standard, rich lifecycle annotations |
| Build tool | **Maven** | Universally supported, easy onboarding |
| Reporting | **Allure** | Rich HTML reports with trace + screenshot on failure |
| Logging | **SLF4J + Logback** | Structured logging, eliminates SLF4J binding warnings |

---

## Project structure

```
craigslist-qa-test/
├── pom.xml
├── README.md
└── src/test/
    ├── java/
    │   ├── base/
    │   │   ├── BaseTest.java            ← Playwright lifecycle + tracing
    │   │   └── TestFailureWatcher.java  ← JUnit 5 extension for failure detection
    │   ├── config/
    │   │   └── TestConfig.java          ← Centralised config (env > sys prop > default)
    │   ├── enums/
    │   │   └── SortOption.java          ← Typed sort options with reverse lookup
    │   ├── pages/
    │   │   ├── HousingLocators.java     ← Centralised CSS selectors
    │   │   └── HousingPage.java         ← Page Object
    │   └── tests/
    │       └── HousingSortTest.java     ← Test scenarios
    └── resources/
        └── logback-test.xml
```

### Design decisions

- **Page Object Model** — page classes only expose intent; test classes express assertions. Business logic never leaks into `HousingPage`.
- **Centralised locators** (`HousingLocators`) — UI changes are a one-file diff.
- **Centralised config** (`TestConfig`) — env var → system property → default, so the same binary runs everywhere.
- **Locale-independent detection** — sort options are matched by stable CSS-class suffixes (`cl-search-sort-mode-newest`), not localised labels. The Madrid site is in Spanish; text-based matching would break on language changes.
- **Typed enum with reverse lookup** — `SortOption.fromClassSuffix(...)` returns `Optional`, gracefully handling unmodelled options added by Craigslist later.
- **`assertAll`** — every missing option is reported in one run, not one-by-one.
- **Tracing on failure** — a full Playwright trace (DOM snapshots, network, screenshots) and a final-state screenshot are attached to the Allure report only when a test fails. Makes CI failures debuggable without local repro.
- **No `System.out`** — logs go through SLF4J; diagnostic info is attached to the Allure report.
- **No `NETWORKIDLE` waits** — they are unreliable on sites that poll analytics or websockets. The framework waits for the specific element it needs instead.

---

## Prerequisites

- **Java 17+** — verify with `java -version`
- **Maven 3.8+** — verify with `mvn -version`
- Internet access (Playwright downloads Chromium on first run)

---

## Running the tests

```bash
# All tests, headless (default)
mvn test

# Watch the browser
HEADLESS=false mvn test
# or
mvn test -Dheadless=false

# Run only the smoke suite
mvn test -Dgroups=smoke

# Exclude a tag
mvn test -DexcludedGroups=slow

# Disable tracing for speed
TRACING=false mvn test

# Override timeout (seconds)
TIMEOUT_SECONDS=30 mvn test
```

### Allure report

```bash
mvn allure:report
# then open target/site/allure-maven-plugin/index.html
```

Or with the Allure CLI:
```bash
allure serve target/allure-results
```

---

## Understanding test failures

Assertion messages include:

1. The **expected sort option** (enum name + CSS class suffix)
2. The **full list of options observed** on the page

Example:
```
Expected sort option 'RELEVANT' (class suffix 'relevance') to be present,
but available options were: [NEWEST, PRICE_ASC, PRICE_DESC, UPCOMING]
```

On CI failures, also check:
- **Allure report** — step-by-step execution with attached trace and final screenshot
- **`target/traces/`** — open trace zips with `npx playwright show-trace <file>`

---

## Adding new tests

1. Add locators to `HousingLocators` (or create a new `*Locators` class for a new page).
2. Add high-level actions to `HousingPage` (or create a new Page Object extending nothing — just receiving `Page`).
3. Add a `@Test` in `HousingSortTest` (or a new class extending `BaseTest`).
4. Tag it (`@Tag("smoke")`) so CI pipelines can filter by suite.

---

**Author:** Renata de Sousa Santos
**Contact:** rehsousa.24@gmail.com
