# HSLU - Compilerbau (COBAU)

This module focuses on building a compiler for the MiniJ language from the ground up.

## Gradle Tasks

In most cases, the following Gradle tasks should be sufficient for this project.

### Assemble

Assembles all the artifacts, e.g., the binaries, classes and jar files.

```shell
./gradlew assemble
```

### Check

Runs all the system tests and creates a milestone report.

```shell
./gradlew check
```

### Build

Assembles all the artifacts and afterward runs all the system tests.

```shell
./gradlew build
```

### Submission

Creates a zip file containing all the files that need to be submitted.

```shell
./gradlew makeSubmission
```
