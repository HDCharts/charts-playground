# charts-playground architecture

This document records the small set of boundaries adopted for `charts-playground`.
It is intentionally project-specific: the playground is an example editor and
code generator with its own focused application boundaries.

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
   +-------+-- codegen serves data through a platform-neutral contract
```

The arrows describe the intended usage. Lower-level packages provide contracts
that presentation and editor code consume, keeping dependencies pointed toward
the user-facing layers.

## Ownership

| Concern | Owner | Contract |
| --- | --- | --- |
| Editable draft, row identity, actions, session state | `domain` | Immutable models and typed actions; platform-neutral and UI-independent |
| Draft mutation and chart-specific validation | `data` plus `domain.ValidationResult` | The store applies actions; definitions validate raw tables and produce validated specifications |
| Validated chart data and style state | `domain` | `ValidatedChartSpec` is the input contract for preview and service-level generation |
| Chart preview | `presentation.chart` | Renderers consume validated specifications; editor validation remains in the domain and data workflow |
| Kotlin source generation | `codegen` plus `data.ChartCodegenService` | Generators are deterministic; service selection is separate from editor state |
| Platform/UI integration | `wasmJsMain.presentation` | Routes collect ViewModel state and inject clipboard, URI, and browser callbacks |
| CI contract | `.github/workflows` and root Gradle verification tasks | Required PR check names remain stable; docs-only changes retain checks with lightweight execution |

## Practical rules

- Pass validated specifications to preview renderers and the codegen service.
- Keep validation wording in the presentation layer; domain validation returns typed issues.
- Use `RowId` for row actions and validation paths.
- Keep chart-specific behavior focused in chart definitions, adapters, and generators.
- Treat generated source as a deterministic artifact. Review per-chart golden changes intentionally.
- Keep platform APIs at the platform boundary. Common domain, data, and codegen code remain portable across targets.
- Adopt a new module, persistence layer, or dependency-injection framework when a concrete requirement justifies it.

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

Docs-only pull requests keep these check contexts and use lightweight execution
steps where the reusable workflow supports them.
