# JUnit 5 Test Template Combiner

When writing JUnit 5 tests, it's sometimes useful to use multiple Test Templates.
This usually means combining several kinds of Parameterized tests, or adding
something like [JUnit Pioneer's RetryingTest](https://junit-pioneer.org/docs/retrying-test/).

## The current issue

In 'vanilla' JUnit 5 this would result in the test being run with each set of 
test templates one after the other. Which I don't beleve to be a sensible default.

This library provides a new annotation [`@CombineTestTemplates`](lib/src/main/java/com/github/danielhodder/junit_test_template_combiner/CombineTestTemplates.java)
which runs the cartesian product of test templates.

This is mostly useful in larger, or enterprise, application where you often have to
support multiple database, browsers, operating systems, or other similar things; 
all at the same time. Often in these cases you'll have a test (or set of tests)
which verify the correctness of behaviour and then execute those tests across 
a range of different scenarios. This allows you to combine existing, or separately
maintained test template helpers together to run these kinds of tests

### Example

Assume there is a Test Template which gives a database on either Oracle or Microsoft
SQL Server, as a java `Datasource`.

Assume there is another test template provider which gives a Selenium `WebDriver`
instance on one of Firefox, Chrome, or IE11. You could write a test like this

```java
@SupportedBrowsers
@SupportedDatabases
public void test(Datasource sqlDataSource, WebDriver driver) {
    // Do Test
}
```

In this case you would get five executions where the parameters are set as follows:
* Datasource from Oracle, null driver
* Datasource from MSSQL, null driver
* null Datsource, Firefox driver
* null Datasource, Chrome Driver
* null Datasource, IE11 Driver

I don't believe this is in any way sensible but that's how JUnit 5 currently works.
There's a proposal to change this but there's be no real movement on it for many years:
<https://github.com/junit-team/junit5/pull/2409>

With this helper you can write:

```java
@CombineTestTemplates
@SupportedBrowsers
@SupportedDatabases
public void test(Datasource sqlDataSource, WebDriver driver) {
    // Do Test
}
```

This will result in six executions:
* Datasource from Oracle, Firefox Driver
* Datasource from Oracle, Chrome Driver
* Datasource from Oracle, IE11 Driver
* Datasource from MSSQL, Firefox Driver
* Datasource from MSSQL, Chrome Driver
* Datasource from MSSQL, IE11 Driver

Which I believe is a far more useful set of parameters.

## Gotchas

Like all things there are some gotchas to this.

### Extension instantiation

This helper creates new instances of extensions which are not bound to the JUnit lifecycle
so if any of your extensions store instance state, or must only be instantiated once
this library may well break them

### Repeated Test

`@RepeatedTest` doesn't use the core JUnit 5 runtime support for test templates
and is instead implemented in the JUnit Jupiter Engine itself. Trying to use
`@RepeatedTest` with `@CombineTestTemplates` will result in an exception

### Other test template aggregators

Some libraries do clever things with test templates and may not play nicely
together. This is just the nature of combining many things together.