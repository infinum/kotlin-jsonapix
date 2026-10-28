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

## Building the sample against published artifacts

The sample app can be built against the JsonApiX artifacts as consumers download them, instead of against the project
modules. Two Gradle properties choose where the sample gets JsonApiX from:

- `jsonApiXSource`: `project` (the default) uses the project modules. `maven` uses the artifacts in your local Maven
  repository, at the version in `releaseConfig` in `config.gradle`. In this mode JsonApiX never resolves from Maven
  Central, so an artifact that wasn't published locally at that version fails the build instead of a released one being
  used.
- `jsonApiXBackend`: `kapt` (the default) runs `processor` on KAPT. `ksp` runs `processor-ksp` on KSP.

Publish all library modules to your local Maven repository first, in a separate Gradle run, then build the sample once
per backend:

```bash
./gradlew publishToMavenLocal
./gradlew :sample:assembleDebug -PjsonApiXSource=maven -PjsonApiXBackend=kapt
./gradlew :sample:assembleDebug -PjsonApiXSource=maven -PjsonApiXBackend=ksp
```

Publishing to the local Maven repository doesn't need a signing key. Only publishing to Maven Central does.

CI runs these steps as the smoke test job on every pull request and every push to `master`.

## Releasing

All artifacts are released together, at the version in `releaseConfig` in `config.gradle`.

Before publishing a release, set the new version and build the sample against the locally published artifacts with both
backends, as described in [Building the sample against published artifacts](#building-the-sample-against-published-artifacts).
Publish only if both builds succeed.

## Code of conduct

We want to ensure a welcoming environment for everyone. With that in mind, all contributors are expected to follow
our [code of conduct](/CODE_OF_CONDUCT.md).

## License

By submitting a pull request you agree to release that code under the project's [license](/LICENSE).
