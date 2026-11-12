package com.pulsevenue.tests;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal zero-dependency assertion and test runner helper for Java 21 CI.
 * No external libraries required — works with plain javac + java.
 */
public final class TestAssert {

    private static final AtomicInteger PASSED = new AtomicInteger(0);
    private static final AtomicInteger FAILED = new AtomicInteger(0);

    private TestAssert() {}

    public static void resetCounters() {
        PASSED.set(0);
        FAILED.set(0);
    }

    public static void runTest(String testId, String description, Runnable testBody) {
        long start = System.currentTimeMillis();
        try {
            testBody.run();
            long elapsed = System.currentTimeMillis() - start;
            PASSED.incrementAndGet();
            System.out.printf("  [PASS] %-6s %-60s (%d ms)%n", testId, description, elapsed);
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            FAILED.incrementAndGet();
            System.err.printf("  [FAIL] %-6s %-60s (%d ms)%n", testId, description, elapsed);
            System.err.printf("         Reason: %s%n", t.getMessage());
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError(message);
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual))
            throw new AssertionError(message + " | Expected: <" + expected + ">, Actual: <" + actual + ">");
    }

    public static void assertClose(double expected, double actual, double epsilon, String message) {
        if (Double.isNaN(actual) || Math.abs(expected - actual) > epsilon)
            throw new AssertionError(message + " | Expected ≈" + expected + ", Actual: " + actual);
    }

    public static void assertContains(String text, String expectedSubstring, String message) {
        if (text == null || !text.toLowerCase().contains(expectedSubstring.toLowerCase()))
            throw new AssertionError(message + " | Expected to contain: <" + expectedSubstring + ">, Actual: <" + text + ">");
    }

    public static int getPassedCount() { return PASSED.get(); }
    public static int getFailedCount() { return FAILED.get(); }
}
