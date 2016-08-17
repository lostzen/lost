# Contributing to LOST

Contributor guidelines for the Location Open Source Tracker project.

## Prerequisites

1. Android SDK with Tools, Extras, and API 23 installed.
2. Android Studio or IntelliJ IDEA.

## Getting started

1. Fork and clone the repository.

    ```
    git clone git@github.com:username/LOST.git
    ```

2. Create a feature branch to make your changes.

    ```
    git checkout -b my-feature-name
    ```

3. Implement your code and tests in your feature branch then push to your fork.

    ```
    git push origin my-feature-name
    ```

4. Submit a pull request using GitHub's web interface.

## Building and testing

Execute the following Gradle tasks to run all tests and code style checks and install LOST to your local maven repository.

    ./gradlew clean test checkstyle install

Alternatively you can run the Gradle default tasks pre-configured in the root project `build.gradle`.

    ./gradlew

## Sample app

Build and install the sample application on your USB device or emulator using the `installDebug` task.

    ./gradlew installDebug

## Writing tests

Good test coverage is important guard against defects and regressions in the LOST framework. All classes should have unit test classes. All public methods should have unit tests. Those classes and methods should have all their possible states well tested.

## Code style

Essentially the IntelliJ default Java style, but with two-space indents.

1. Spaces, not tabs.
2. Two space indent.
3. Curly braces for everything: if, else, etc.
4. One line of white space between methods.

Code style config can be downloaded and installed from https://github.com/mapzen/java-code-styles.
