package com.github.danielhodder.junit_test_template_combiner;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.RetryingTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

public class CombinedTestTemplate {
    @CombineTestTemplates
    @ValueSource(strings = {"a", "b"})
    @ParameterizedTest
    @RetryingTest(maxAttempts = 5, minSuccess = 2)
    public void test() {
    }

    @CombineTestTemplates(suppressOtherTestTemplates = false)
    @ValueSource(strings = {"a", "b"})
    @ParameterizedTest
    @RetryingTest(maxAttempts = 5, minSuccess = 2)
    public void test2(String other) {
    }
}
