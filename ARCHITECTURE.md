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
| Kotlin source generation | `codegen` plus `data.ChartCodegenService` | One deterministic renderer (`ChartSnippet`) for every chart; service selection is separate from editor state |
| Platform/UI integration | `wasmJsMain.presentation` | Routes collect ViewModel state and inject clipboard, URI, and browser callbacks |
| CI contract | `.github/workflows` and root Gradle verification tasks | Required PR check names remain stable; docs-only changes retain checks with lightweight execution |

## Practical rules

- Pass validated specifications to preview renderers and the codegen service.
- Keep validation wording in the presentation layer; domain validation returns typed issues.
- Use `RowId` for row actions and validation paths.
- Keep chart-specific behavior in the chart's `PlaygroundChart` definition; shared code stays chart-agnostic.
- Treat generated source as a deterministic artifact. Review per-chart golden changes intentionally.
- Keep platform APIs at the platform boundary. Common domain, data, and codegen code remain portable across targets.
- Adopt a new module, persistence layer, or dependency-injection framework when a concrete requirement justifies it.

## Adding a chart

Each chart is one object in `data/charts/<Chart>ChartDefinition.kt`. Most charts extend
`SingleSeriesChart` (one value per label) or `MultiSeriesChart` (named series over shared
categories), which build, validate, extend, and randomize the data table. The object itself only
supplies its type, title, label prefix, value range, sample data, settings, composable name
(`component`), and `StyleCodeProfile`. Generated code is shared (`PlaygroundChart.generate`); the
data it writes comes from `snippetData`, which mirrors `presentation/chart/LibraryChartData.kt`.

1. Add a `ChartType` entry and the definition, and list it in `playgroundCharts`
   (`data/ChartCatalog.kt`) in navigation order. `ChartCatalogTest` fails for a type without one.
2. Declare its settings in `<Chart>StyleSettings.kt` (see below).
3. Handle the new type where the compiler asks: the style builder in
   `presentation/chart/ChartStyles.kt`, the preview in `ChartRenderer`, and the coverage entry in
   `StyleApiCoverageTest`.
4. Record the new chart's hash in `GeneratedArtifactGoldenTest`.

## Chart style settings

Each style setting is declared once, in the chart's `data/charts/<Chart>StyleSettings.kt`, by
the path of the property in the chart's library style (for example `points.size` or
`axis.xLabels.count`), its value kind, its control, and how to read its library default:

```kotlin
+slider("points.size", "Size", StyleKind.DP, 2f..20f, 1f) { it.points.size }
```

Everything else derives from that declaration:

- **State**: `ChartStyleState` holds only the values the user set, keyed by path. Unset means the
  library default.
- **Defaults**: read from the library default style for the current theme through the declaration,
  so the editor never hardcodes them.
- **Generated code**: `renderStyleArguments` turns each set value into an argument from its path
  (`a.b` becomes `a = Owner.a(b = …)`). A per-chart `StyleCodeProfile` covers factory exceptions,
  such as histograms building shared blocks with `BarChartDefaults`.
- **Settings panel**: sections, section toggles, and `visibleWhen` rules hide settings that have no
  effect. Playground-only switches (`StyleTarget.LOCAL`, such as "Fixed Range") gate the settings
  in their section for both preview and generated code.

To add a setting: declare it, then read it in the chart's builder in
`presentation/chart/ChartStyles.kt`. These tests keep the pieces aligned:

- `StyleApiCoverageTest`: every public library style property is declared, not applicable, or
  listed as not exposed yet. A new library property fails this test.
- `StyleRoundTripTest`: every declared setting reaches the preview style at its path.
- `EverySettingCompilesTest`: generated code compiles with every setting changed, for every chart.
- `BlockDefaultsTest`: where a chart's default style block differs from what its factory builds
  (e.g. a histogram range starts at 0), the member is listed in `StyleCodeProfile.blockDefaults`,
  so generated code matches the preview.
- `SettingDefaultsTest`: every setting resolves to a default of its kind.

The playground fails fast when the library changes: a new chart, style property, or value type
fails the build instead of falling back to a default. `StyleApiCoverageTest` also names library
charts the playground does not offer yet, and a library value type `toStyleValue` does not know
throws rather than showing no default.

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
