package com.github.danielhodder.junit_test_template_combiner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ReflectionUtils;

import static java.util.stream.Collectors.toList;

public class CombineTestTemplatesExtension implements TestTemplateInvocationContextProvider, ExecutionCondition {
  private static final Namespace NAMESPACE = Namespace.create(CombineTestTemplates.class.getName());
  private static final String ENABLE_FLAG = "enable";
  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
    if (!context.getElement().isPresent())
      return Stream.empty();

    if (AnnotationSupport.isAnnotated(context.getElement().get(), RepeatedTest.class)) {
      throw new IllegalStateException("@RepeatedTest is special and is implemented in JUnit 5's core, so can't be used with @CombineTestTemplates");
    }

    val providers = AnnotationSupport.findRepeatableAnnotations(context.getElement().get(), ExtendWith.class)
            .stream()
            .flatMap(extendWith -> Arrays.stream(extendWith.value()))
            .filter(TestTemplateInvocationContextProvider.class::isAssignableFrom) // Get only TestTemplates
            .filter(providerType -> !CombineTestTemplatesExtension.class.equals(providerType)) // Exclude ourself
            .map(ReflectionUtils::newInstance)
            .map(TestTemplateInvocationContextProvider.class::cast)
            .collect(toList());

    return productOfTestTemplateContexts(providers, context).map(AdaptingTestTemplateExecutionContext::new);
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return context.getElement().map(element -> AnnotationSupport.isAnnotated(element, CombineTestTemplates.class)).orElse(false);
  }

  /**
   * Allows the creation of a product of {@link TestTemplateInvocationContext TestTemplateInvocationContext} from a
   * list of providers. Each provider will be asked for execution contexts and those will be combined.
   * <p>
   * 		If there are two Executors (for example the {@code RepeatedTestExtension} and the
   * 		{@code ParameterizedTestExtension} and they provide templates [1, 2, 3] and [A, B] respectively; then this
   * 		method will return a stream of the following items:
   * </p>
   * <ul>
   *     <li>[1, A]</li>
   *     <li>[1, B]</li>
   *     <li>[2, A]</li>
   *     <li>[2, B]</li>
   *     <li>[3, A<]</li>
   *     <li>[3, B]</li>
   * </ul>
   * <p>
   *     The intention here is that this can then be passed to {@link AdaptingTestTemplateExecutionContext} to be executed.
   * </p>
   *
   * @param providers the providers to use to generate the template contexts
   * @param extensionContext the extension context to use when generating the test contexts
   * @return a stream of test invocation context combinations
   */
  private static Stream<List<TestTemplateInvocationContext>> productOfTestTemplateContexts(
          List<TestTemplateInvocationContextProvider> providers, ExtensionContext extensionContext) {
    if (providers.isEmpty()) {
      return Stream.of(Collections.emptyList());
    }

    TestTemplateInvocationContextProvider firstProvider = providers.get(0);
    List<TestTemplateInvocationContextProvider> tail = providers.subList(1, providers.size());

    return firstProvider.provideTestTemplateInvocationContexts(extensionContext).flatMap(
            context -> productOfTestTemplateContexts(tail, extensionContext).map(
                    contexts -> Stream.concat(Stream.of(context), contexts.stream()).collect(toList())));
  }

  /**
   * Adapt a List of {@link TestTemplateInvocationContext TestTemplateInvocationContexts} into a single one for
   * execution. The methods on {@link TestTemplateInvocationContext} delegate to each of the wrapped elements in turn.
   */
  private static class AdaptingTestTemplateExecutionContext implements TestTemplateInvocationContext {
    private final List<TestTemplateInvocationContext> delegates;

    private AdaptingTestTemplateExecutionContext(List<TestTemplateInvocationContext> delegates) {
      this.delegates = delegates;
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
      return
                  delegates.stream().flatMap(context -> context.getAdditionalExtensions().stream())
              .collect(toList());
    }

    @Override
    public String getDisplayName(int invocationIndex) {
      return delegates.stream().map(context -> context.getDisplayName(invocationIndex)).collect(
              Collectors.joining(", ")) + " [Combined " + this.delegates.size() + " contexts]";
    }
  }

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    Boolean suppressOtherTestTemplates = context.getTestMethod().flatMap(method -> AnnotationSupport.findAnnotation(method, CombineTestTemplates.class))
            .map(CombineTestTemplates::suppressOtherTestTemplates)
            .orElse(true);

    if (!suppressOtherTestTemplates) {
      return ConditionEvaluationResult.enabled("Suppression of other test templates disabled");
    }

    if (context.getClass().getSimpleName().startsWith("Method")) {// HACK
      if (context.getDisplayName().contains("Combined")) {
        return ConditionEvaluationResult.enabled("Enabled for this instance of test template");
      } else {
        return ConditionEvaluationResult.disabled("Disabled because of combined test templates");
      }
    } else {
      return ConditionEvaluationResult.enabled("Not a test template");
    }
  }
}
