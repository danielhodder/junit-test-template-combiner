package com.github.danielhodder.junit_test_template_combiner;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.jupiter.RetryingTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import javax.sql.DataSource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.http.WebSocket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

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

    @NotABrowser
    @NotADatabase
    @CombineTestTemplates
    public void example(DataSource d, String browser) {
        assertNotNull(d);
        assertNotNull(browser);
    }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(NotADatabaseExtension.class)
@TestTemplate
@interface NotADatabase {
}

class NotADatabaseExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().map(method -> AnnotationSupport.isAnnotated(method, NotADatabase.class)).orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Stream.of(new Context(), new Context());
    }

    private static class Context implements TestTemplateInvocationContext {
        @Override
        public String getDisplayName(int invocationIndex) {
            return "Database "+invocationIndex;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Arrays.asList(new ParameterResolver() {
                @Override
                public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return parameterContext.getParameter().getType() == DataSource.class;
                }

                @Override
                public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return mock(DataSource.class);
                }
            });
        }
    }
}


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtendWith(NotABrowserExtension.class)
@TestTemplate
@interface NotABrowser{
}

class NotABrowserExtension implements TestTemplateInvocationContextProvider {
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod().map(method -> AnnotationSupport.isAnnotated(method, NotABrowser.class)).orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Stream.of(new Context(), new Context());
    }

    private static class Context implements TestTemplateInvocationContext {
        @Override
        public String getDisplayName(int invocationIndex) {
            return "Browser "+invocationIndex;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Arrays.asList(new ParameterResolver() {
                @Override
                public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return parameterContext.getParameter().getType() == String.class;
                }

                @Override
                public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return "Browser";
                }
            });
        }
    }
}