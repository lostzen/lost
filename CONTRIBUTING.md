# Contributing to LOST

## Getting Started

Fork and clone the repo:

    git clone git@github.com:mapzen/LOST.git

Create a feature branch to make your changes:

    git co -b my-feature-name

Perform a full build including library, sample app, unit tests, and checkstyle:

    ./gradlew

## Building and Testing

Run the default Gradle build profile to ensure all tests and code quality checks succeed before committing your changes. This will build the library, sample app, execute unit tests, run checkstyle, etc.

## Writing Tests

All classes should have unit test classes. All public methods should have unit tests. Those classes and methods should have all their possible states well tested. If you need help with this, let us know!

## Code Style

Essentially the IntelliJ default Java style, with the following specific points

1. Spaces, not tabs.
2. Curly braces for everything: if, else, etc.
3. One line of white space between methods.
4. Maximum 100 characters per line
