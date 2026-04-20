package base;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Tracks whether the currently executing test failed, so that
 * {@link BaseTest#closeContext} can conditionally attach a trace
 * and screenshot to the Allure report only for failures.
 *
 * <p>Uses a {@link ThreadLocal} so parallel test execution stays correct.
 */
public class TestFailureWatcher implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final ThreadLocal<Boolean> FAILED = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        FAILED.set(Boolean.FALSE);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        FAILED.set(context.getExecutionException().isPresent());
    }

    static boolean currentTestFailed() {
        return FAILED.get();
    }
}