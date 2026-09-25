package data

import codegen.ChartSnippet
import codegen.GeneratedArtifact
import codegen.SnippetData
import codegen.common.StyleCodeProfile
import codegen.common.renderStyleArguments
import codegen.render
import domain.ChartDefinition
import domain.ValidatedChartSpec
import domain.deriveFunctionName

/** A chart the playground offers: its editor definition plus how its usage code is generated. */
interface PlaygroundChart : ChartDefinition {
    /** The chart composable generated code calls, e.g. `BarChart`. */
    val component: String

    /** How this chart's style is written in generated code. */
    val styleProfile: StyleCodeProfile

    /** The chart data as generated code writes it; must match `presentation/chart/LibraryChartData.kt`. */
    fun snippetData(spec: ValidatedChartSpec): SnippetData

    fun generate(spec: ValidatedChartSpec): String =
        ChartSnippet(
            component = component,
            styleObject = styleProfile.styleObject,
            data = snippetData(spec),
            title = spec.title,
            functionName = deriveFunctionName(spec.title, type),
            styleArguments = renderStyleArguments(settings, spec.styleState, spec.data, styleProfile),
        ).render()

    fun generateArtifact(spec: ValidatedChartSpec): GeneratedArtifact = GeneratedArtifact(source = generate(spec))
}
