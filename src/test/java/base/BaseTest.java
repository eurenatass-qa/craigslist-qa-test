package base;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import config.TestConfig;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base class for UI tests. Owns the Playwright lifecycle:
 * <ul>
 *   <li>One {@link Playwright} + {@link Browser} instance per test class
 *       (launched once for performance).</li>
 *   <li>A fresh {@link BrowserContext} + {@link Page} per test
 *       (isolated, no state leaks).</li>
 *   <li>Optional Playwright tracing — attached to the Allure report on
 *       failure, making CI failures debuggable without local repro.</li>
 * </ul>
 *
 * <p>Headless behaviour and tracing are driven by {@link TestConfig}.
 */
@ExtendWith(TestFailureWatcher.class)
public abstract class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    private static final Path TRACE_DIR = Paths.get("target", "traces");

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() throws IOException {
        Files.createDirectories(TRACE_DIR);
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(TestConfig.HEADLESS));
        log.info("Browser launched (headless={})", TestConfig.HEADLESS);
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage(TestInfo info) {
        context = browser.newContext();
        if (TestConfig.TRACING_ENABLED) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true)
                    .setTitle(info.getDisplayName()));
        }
        page = context.newPage();
    }

    @AfterEach
    void closeContext(TestInfo info) {
        try {
            if (context != null && TestConfig.TRACING_ENABLED) {
                Path tracePath = TRACE_DIR.resolve(safeFileName(info.getDisplayName()) + ".zip");
                context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));

                // Attach trace to Allure only when the test failed (avoids huge reports)
                if (TestFailureWatcher.currentTestFailed()) {
                    attachTrace(tracePath);
                    attachScreenshot();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to stop tracing / attach artifacts", e);
        } finally {
            if (context != null) context.close();
        }
    }

    private void attachTrace(Path tracePath) {
        try {
            if (Files.exists(tracePath)) {
                Allure.addAttachment(
                        "Playwright trace",
                        "application/zip",
                        Files.newInputStream(tracePath),
                        "zip");
            }
        } catch (IOException e) {
            log.warn("Could not attach trace to Allure", e);
        }
    }

    private void attachScreenshot() {
        try {
            if (page != null && !page.isClosed()) {
                byte[] png = page.screenshot();
                Allure.addAttachment("Failure screenshot", "image/png",
                        new ByteArrayInputStream(png), "png");
            }
        } catch (Exception e) {
            log.warn("Could not attach screenshot to Allure", e);
        }
    }

    private static String safeFileName(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}