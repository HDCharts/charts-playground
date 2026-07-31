# charts-playground architecture

This document records the small set of boundaries adopted for `charts-playground`.
It is intentionally project-specific: the playground is an example editor and
code generator, not a second application architecture for the charts library.

## Dependency direction

```text
editable draft
    -> validation
    -> validated chart specification
        -> preview rendering
        -> Kotlin code generation
```

The dependency direction is:

```text
domain <- data <- wasm presentation
   ^       ^
   +-------+-- codegen is used by data and has no editor/UI dependency
```

The arrows describe allowed usage. Lower-level packages must not reach upward
into presentation or editor code.

## Ownership

| Concern | Owner | Contract |
| --- | --- | --- |
| Editable draft, row identity, actions, session state | `domain` | Immutable models and typed actions; no Compose or platform UI imports |
| Draft mutation and chart-specific validation | `data` plus `domain.ValidationResult` | The store applies actions; definitions validate raw tables and produce validated specifications |
| Validated chart data and style state | `domain` | `ValidatedChartSpec` is the only input accepted by preview and service-level generation |
| Chart preview | `presentation.chart` | Renderers consume validated specifications and do not perform editor validation |
| Kotlin source generation | `codegen` plus `data.ChartCodegenService` | Generators are deterministic; service selection is separate from editor state |
| Platform/UI integration | `wasmJsMain.presentation` | Routes collect ViewModel state and inject clipboard, URI, and browser callbacks |
| CI contract | `.github/workflows` and root Gradle verification tasks | Required PR check names remain stable; docs-only changes retain checks but skip expensive code work |

## Practical rules

- Do not pass raw editor text to preview renderers or the codegen service.
- Keep validation wording in the presentation layer; domain validation returns typed issues.
- Use `RowId`, not a list position, for row actions and validation paths.
- Keep chart-specific behavior in chart definitions, adapters, and generators rather than adding generic managers.
- Treat generated source as a deterministic artifact. Review per-chart golden changes intentionally.
- Keep platform APIs at the platform boundary. Common domain, data, and codegen code must remain usable without browser UI code.
- Add a new module, persistence layer, or dependency-injection framework only when a concrete requirement justifies it.

## Enforcement

`playground/src/jvmTest/kotlin/architecture/ArchitectureBoundaryTest.kt`
checks the most important package dependency rules from source imports. It runs
as part of `:playground:jvmTest`, which is included in the root `playgroundTest`
task used by PR Test.

The required PR checks are coordinated by
`.github/workflows/pull-request.yml`:

- `PR Assemble / Assemble`
- `PR Compile / Compile`
- `PR Lint / Lint`
- `PR Test / Test`

Docs-only pull requests keep these check contexts but skip the expensive
assemble, compile, lint, and test work where the reusable workflow supports it.
