# Contributing guidelines

Welcome to our project! We appreciate your interest in helping us improve it.

## How can I contribute?

There are multiple ways in which you can help us make this project even better.

- Reporting bugs or suggesting new features
- Contributing code improvements or new features
- Writing, updating, or fixing tests
- Improving documentation, including inline comments, user manuals, and developer guides

## Issue reporting

If you found a bug or have an idea for a new feature, please open an issue. Be sure to include a clear and descriptive
title, as well as a detailed description of the bug or feature.

To avoid duplicate issues, please check if a similar issue has already been created.

## Making changes

To make changes to the project, please follow these steps:

1. Fork the project repository.
2. Create a new branch for your changes, based on the project's main branch.
3. Make your changes. Ensure you've followed the coding style and standards.
4. Test your changes thoroughly, ensuring all existing tests pass and new tests cover your changes where appropriate.
5. Commit your changes with a clear and descriptive commit message.
6. Push your changes to your fork.
7. Create a pull request to the project's main branch.

Once we check everything, we will merge the changes into the main branch and include it in the next release.

## Guidelines for pull requests

When submitting a pull request, please ensure that:

- Your pull request is concise and well-scoped
- Your code is properly tested
- Your code adheres to the project's coding standards and style guidelines
- Your commit message is clear and descriptive
- Your pull request includes a description of the changes you have made and why you have made them

Try to avoid creating large pull requests that include multiple unrelated changes. Instead, break them down into
smaller, more focused pull requests. This will make it easier for us to review and merge your changes.

## Building and testing

Run every test your machine can run:

```bash
./gradlew test allTests
```

`test` runs the tests of the JVM modules. The multiplatform modules, `annotations`, `core` and `processor-tests:kmp`,
have no `test` task. `allTests` runs their tests on every target your machine can run.

You don't need Xcode. Without it, on Linux or on a Mac with only the Command Line Tools, everything except the Apple
targets builds and tests:

- Kotlin compiles every target on any host, so the iOS libraries still build and publish;
- linking and running the iOS test binaries needs Xcode. On a Mac without it, the build skips those tasks and warns
  which ones it skipped. To run them, see [Running the iOS simulator tests](#running-the-ios-simulator-tests);
- the Linux native tests, `linuxX64Test`, run only on Linux. On a Mac, Kotlin skips them with a warning. CI runs them.

The JS and Wasm tests run on Node.js, which Gradle downloads. The npm packages for the JS tests are locked in
`kotlin-js-store/yarn.lock`, which is committed. When a Kotlin update changes those packages, the build fails with
"Lock file was changed. Run the `kotlinUpgradeYarnLock` task to actualize lock file". Run that task, and commit the
updated lock with the Kotlin update:

```bash
./gradlew kotlinUpgradeYarnLock
```

## Running the iOS simulator tests

CI runs the iOS simulator tests on macOS on every pull request and every push to `master`. To run them yourself, you
need a Mac with Apple Silicon, and:

- Xcode;
- an iOS simulator runtime. Install it once, in Xcode → Settings → Components.

Then run the tests in every module that has them:

```bash
./gradlew iosSimulatorArm64Test
```

Gradle uses the developer directory that `xcode-select -p` prints. If that's the Command Line Tools, the build skips
the iOS tests with a warning. To point Gradle at Xcode for one run, without changing the system setting, set
`DEVELOPER_DIR`:

```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer ./gradlew iosSimulatorArm64Test
```

To select Xcode for every run instead, run `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`.

If the tests fail with "Xcode does not support simulator tests for ios_simulator_arm64", the iOS simulator runtime
isn't installed.

## Building against published artifacts

Two modules can be built against the JsonApiX artifacts as consumers download them, instead of against the project
modules: the sample app, an Android consumer, and `processor-tests:kmp`, a Kotlin Multiplatform consumer that runs the
behavior tests. Two Gradle properties choose where they get JsonApiX from:

- `jsonApiXSource`, for both: `project` (the default) uses the project modules. `maven` uses the artifacts in your local
  Maven repository, at the version in `releaseConfig` in `config.gradle`. In this mode JsonApiX never resolves from
  Maven Central, so an artifact that wasn't published locally at that version fails the build instead of a released one
  being used.
- `jsonApiXBackend`, for the sample only: `kapt` (the default) runs `processor` on KAPT. `ksp` runs `processor-ksp` on
  KSP.

Publish all library modules to your local Maven repository first, in a separate Gradle run. Then build the sample once
per backend, and run the KMP module's tests:

```bash
./gradlew publishToMavenLocal
./gradlew :sample:assembleDebug -PjsonApiXSource=maven -PjsonApiXBackend=kapt
./gradlew :sample:assembleDebug -PjsonApiXSource=maven -PjsonApiXBackend=ksp
./gradlew :processor-tests:kmp:allTests -PjsonApiXSource=maven
```

`allTests` runs the KMP module's tests on every target your machine can run.

Publishing to the local Maven repository doesn't need a signing key. Only publishing to Maven Central does.

CI runs these steps as the smoke test job on every pull request and every push to `master`.

## Releasing

All artifacts are released together, at the version in `releaseConfig` in `config.gradle`.

Releases can be made from any machine. Kotlin compiles every target JsonApiX publishes, the Apple ones included, on any
host, and publishing doesn't need Xcode.

Before publishing a release, set the new version. Then build the sample with both backends and run the KMP module's
tests, all against the locally published artifacts, as described in
[Building against published artifacts](#building-against-published-artifacts). Publish only if all three succeed.

## Code of conduct

We want to ensure a welcoming environment for everyone. With that in mind, all contributors are expected to follow
our [code of conduct](/CODE_OF_CONDUCT.md).

## License

By submitting a pull request you agree to release that code under the project's [license](/LICENSE).
