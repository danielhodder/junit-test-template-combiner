package com.github.danielhodder.junit_test_template_combiner;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.lang.annotation.*;

/**
 * Combine multiple test templates. This is a workaround the odd default behaviour of JUnit 5 which is documented on
 * {@link https://github.com/junit-team/junit5/pull/2409}
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@ExtendWith(CombineTestTemplatesExtension.class)
public @interface CombineTestTemplates {
    /**
     * Disable the execution of other discovered Test Templates found on this method.
     */
    boolean suppressOtherTestTemplates() default true;
}
