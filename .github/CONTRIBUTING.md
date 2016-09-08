# Contributing to LOST

Contributor guidelines for the Location Open Source Tracker project.

First off, thanks for taking the time to contribute!

## Aim
This project seeks to provide an open source alternative to basically anything in Google's [com.google.android.gms.location](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) package, only depending on the Android SDK.
Therefore, contributions should implement the [FusedLocationProviderAPI](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi), [GeoencingApi](https://developers.google.com/android/reference/com/google/android/gms/location/GeofencingApi) or [SettingsApi](https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi) or fix bugs/improve the current implementation, rather than implementing completely new features.

But a working subset is always fine. That's kind of what this project is all about ;).

## Prerequisites

1. Android SDK with Tools, Extras, and API 23 installed.
2. Android Studio or IntelliJ IDEA.

## Getting started

1. Fork the project on GitHub.
2. Clone the repository on your local development machine via Android Studio or the command line:

    ```
    git clone git@github.com:username/LOST.git
    ```

3. (Optional): Add the original repository as an additional remote to stay in touch with ongoing developments:

    ```
    git remote add --mirror=fetch upstream https://github.com/mapzen/LOST.git
    ```

4. Create a feature branch to make your changes:

    ```
    git checkout -b my-feature-name
    ```

5. Implement your code and tests in your feature branch.
6. Push to your fork:

    ```
    git push origin my-feature-name
    ```

7. Submit a pull request using GitHub's web interface.

## Building and testing

Execute the following Gradle tasks to run all tests and code style checks and install LOST to your local maven repository:

    ./gradlew clean test checkstyle install

Alternatively you can run the Gradle default tasks pre-configured in the root project `build.gradle`:

    ./gradlew

## Sample app

The project not only contains the library itself, but also a sample app for testing, debugging and guiding developers when using the library. It is only a rough user interface for demonstration purposes.
When you commit a new feature, please consider to also provide a sample implementation of the new library interface.

Build and install the sample application on your USB device or emulator using the `installDebug` task:

    ./gradlew installDebug

## Writing tests

Good test coverage is an important guard against defects and regressions in the LOST framework. All classes should have unit test classes. All public methods should have unit tests. Those classes and methods should have all their possible states well tested.
The optimal way would be to first write the test and then implement the code until the test succeeds.
You can Right Click > "Run" on any test class in Android Studio directly in the editor to run it. Or you can run all tests via the "test" task in the Gradle tab of the GUI or on the command line with `./gradlew clean test`.
Obviously, if the test case fails, the implementation needs to be fixed before committing/issuing a pull request.

## Code style

Essentially the IntelliJ default Java style, but with two-space indents:

1. Spaces, not tabs.
2. Two space indent.
3. Curly braces for everything: if, else, etc.
4. One line of white space between methods.

To help adhering to this, you can include the file [MapzenAndroid.xml](https://github.com/mapzen/java-code-styles/blob/master/configs/MapzenAndroid.xml) into the `config/codestyles/` (create it if not present yet) directory of your Android Studio profile (on Linux this is at `~/.AndroidStudio*/`), [see here for help](https://www.jetbrains.com/help/idea/2016.2/copying-code-style-settings.html).
There is a script doing this for you at https://github.com/mapzen/java-code-styles.
Afterwards, at "File > Settings > Editor > Code Style > Java" you can select the MapzenAndroid scheme at the top.

Now, while you're working on the code, the editor will support you. Also, you can use "Code > Reformat Code" (<kbd>CTRL</kbd>+<kbd>ALT</kbd>+<kbd>L</kbd>) from time to time.

## Checkstyle
As an additional measure, we use [Checkstyle](http://checkstyle.sourceforge.net/).
You can run the checks defined in [config/checkstyle/checkstyle.xml](https://github.com/mapzen/LOST/blob/master/config/checkstyle/checkstyle.xml) either manually on the command line via `./gradlew checkstyle` or pick the task from the Gradle pane before pushing.
It will generate a HTML file at `lost/build/reports/checkstyle/checkstyle.html` inside the project folder, listing all violations.
You could also add a git pre commit hook.

A maybe even more comfortable way is to install the [CheckStyle-IDEA plugin](https://plugins.jetbrains.com/plugin/1065) via "File->Settings->Plugins->Install JetBrains plugin".
After the restart, navigate to "File > Settings > Other Settings > Checkstyle", click on the green <kbd>+</kbd> button on the right of the upper list, choose the checkstyle.xml from the project directory, select "Store relative to project location" and give it a description like "Mapzen LOST".
Above the list, you can check all three additional checkboxes.

Now, while editing code, failed checkstyle rules will show up like the usual Android Linter remarks.
Also, there is a new tab/pane "Checkstyle" in the lower edge of the GUI, allowing to manually run the checks on the current or all files in the project, you can jump to the location by double clicking the entry.
Additionally, you can select "Scan with Checkstyle" in the "Commit" dialogue.

Checkstyle remarks should be fixed before issuing a pull request.

## Managing upstream changes
While you work on your feature branch, there may be changes happening on the upstream master. It is helpful to monitor them, by fetching from time to time.
Please do not merge them into your branch, but instead [rebase your changes](https://git-scm.com/book/en/v2/Git-Branching-Rebasing) before issuing the pull request.
Personally we prefer rebase to merge commits since it makes changes easier to review and history a bit more tidy. 
You can do the rebase from the command line or, in Android Studio, while you are on your branch, select "Rebase onto" from the context menu of the upstream master in the "Log" of the "Version Control" tab.
You will probably (depending on how big the master changes and your's are) have to "Resolve conflicts" in the files separately (select from the context menu from the individual files shown in red under "Log", always finish by "Merge"), and afterwards select "Skip Commit in Rebasing" from the "VCS->Git" menu.
More information on this is available at [IntelliJ](https://www.jetbrains.com/help/idea/2016.2/interactive-rebase.html).

