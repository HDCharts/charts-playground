package data.charts

import data.playgroundCharts
import domain.StyleTarget
import domain.styleSettings
import presentation.chart.StyleReader
import presentation.chart.chartStyle
import testing.callStyleFactory
import testing.readComposable
import testing.readPath
import testing.styleFields
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Generated code writes a style block as `<owner>.<block>(…)` with only the members the user set,
 * so every other member takes the factory's default. The preview starts from the chart's default
 * style instead. Where the two differ, the member must be listed in the chart's
 * `StyleCodeProfile.blockDefaults`, or the generated chart will not match the preview.
 */
class BlockDefaultsTest {
    @Test
    fun generated_blocks_start_from_the_preview_defaults() {
        val problems =
            playgroundCharts.flatMap { definition ->
                val profile = definition.styleProfile
                val data = definition.resetSession().validatedSpec.data
                val defaultStyle = readComposable { chartStyle(definition.type, StyleReader.Defaults, data) }
                val blockPaths =
                    definition.settings.styleSettings
                        .filter { it.target == StyleTarget.STYLE }
                        .flatMap { setting ->
                            val segments = setting.path.split('.')
                            (1 until segments.size).map { size -> segments.take(size).joinToString(".") }
                        }.toSortedSet()

                blockPaths.flatMap { blockPath ->
                    val owner = profile.owner(blockPath.substringBefore('.'))
                    val block = blockPath.substringAfterLast('.')
                    val preview = styleFields(readPath(defaultStyle, blockPath)!!)
                    val factory = styleFields(callStyleFactory(owner, block)!!)
                    val listed = profile.blockDefaults[blockPath].orEmpty().keys
                    val differing = preview.keys.filter { preview[it] != factory[it] }.toSet()
                    val where = "${definition.type} $owner.$block()"
                    (differing - listed).map { member ->
                        "$where: '$member' is ${preview[member]} in the preview but ${factory[member]} " +
                            "in generated code; add it to blockDefaults[\"$blockPath\"]"
                    } +
                        (listed - differing).map { member ->
                            "$where: blockDefaults[\"$blockPath\"] lists '$member', which already matches"
                        }
                }
            }

        assertTrue(problems.isEmpty(), problems.joinToString(separator = "\n"))
    }
}
