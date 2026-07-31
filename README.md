# charts-playground

Kotlin Multiplatform playground for chart examples and generated chart code.

Pull requests are coordinated by `.github/workflows/pull-request.yml`. The
required merge checks for auto-merge are:

- `PR Assemble / Assemble`
- `PR Compile / Compile`
- `PR Lint / Lint`
- `PR Test / Test`

The adopted package ownership and dependency rules are documented in
[`ARCHITECTURE.md`](ARCHITECTURE.md). The architecture boundary test runs as
part of `./gradlew -DchartsLocalPath=charts playgroundTest`.
