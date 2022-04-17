/*
 * This file was generated by the Gradle "init" task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details take a look at the "Building Java & JVM projects" chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.4.2/userguide/building_java_projects.html
 */

plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

java {
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_1_8;
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_1_8;
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testCompileOnly("org.projectlombok:lombok:1.18.22")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.22")

    // Use JUnit Jupiter for testing.
    testImplementation("org.junit.jupiter:junit-jupiter:${System.getenv().getOrDefault("JUNIT_VERSION", "5.4.0")}")
    testImplementation("org.junit-pioneer:junit-pioneer:1.7.0")
    testImplementation("org.mockito:mockito-core:4.+")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")
    api(("org.junit.jupiter:junit-jupiter-api:5.0.0"))

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:30.1.1-jre")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
