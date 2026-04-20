package config;

import java.time.Duration;

/**
 * Centralised test configuration.
 *
 * <p>Values are resolved in the following priority order:
 * <ol>
 *   <li>Environment variable (e.g. {@code HEADLESS=false})</li>
 *   <li>System property (e.g. {@code -Dheadless=false})</li>
 *   <li>Hard-coded default (CI-friendly)</li>
 * </ol>
 *
 * <p>This makes the framework easy to drive from both local shells and
 * CI pipelines without code changes.
 */
public final class TestConfig {

    public static final String BASE_URL =
            resolve("BASE_URL", "base.url", "https://madrid.craigslist.org");

    public static final Duration DEFAULT_TIMEOUT =
            Duration.ofSeconds(Long.parseLong(
                    resolve("TIMEOUT_SECONDS", "timeout.seconds", "10")));

    public static final boolean HEADLESS =
            Boolean.parseBoolean(resolve("HEADLESS", "headless", "true"));

    public static final boolean TRACING_ENABLED =
            Boolean.parseBoolean(resolve("TRACING", "tracing", "true"));

    private TestConfig() {
        // utility class
    }

    /**
     * Resolve a configuration value from env var, system property, or default.
     */
    private static String resolve(String envKey, String propKey, String defaultValue) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        return System.getProperty(propKey, defaultValue);
    }
}