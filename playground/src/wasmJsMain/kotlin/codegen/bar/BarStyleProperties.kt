package codegen.bar

import androidx.compose.runtime.Composable
import io.github.dautovicharis.charts.style.BarChartDefaults
import model.BarStyleState
import model.StylePropertiesSnapshot

@Composable
fun barStylePropertiesSnapshot(
    styleState: BarStyleState,
    seriesCount: Int,
): StylePropertiesSnapshot {
    val defaultStyle = BarChartDefaults.style()
    val normalizedBarColors =
        styleState.barColors?.let { colors ->
            normalizeColorCount(colors = colors, targetCount = seriesCount)
        }
    val currentStyle =
        BarChartDefaults.style(
            barColor = styleState.barColor ?: defaultStyle.barColor,
            barColors = normalizedBarColors ?: defaultStyle.barColors,
            barAlpha = styleState.barAlpha ?: defaultStyle.barAlpha,
            gridVisible = styleState.gridVisible ?: defaultStyle.gridVisible,
            axisVisible = styleState.axisVisible ?: defaultStyle.axisVisible,
            selectionLineVisible = styleState.selectionLineVisible ?: defaultStyle.selectionLineVisible,
            selectionLineWidth = styleState.selectionLineWidth ?: defaultStyle.selectionLineWidth,
            zoomControlsVisible = styleState.zoomControlsVisible ?: defaultStyle.zoomControlsVisible,
        )
    return StylePropertiesSnapshot(
        current = currentStyle.getProperties(),
        defaults = defaultStyle.getProperties(),
    )
}

private fun normalizeColorCount(
    colors: List<androidx.compose.ui.graphics.Color>,
    targetCount: Int,
): List<androidx.compose.ui.graphics.Color> {
    if (targetCount <= 0 || colors.isEmpty()) return emptyList()
    return List(targetCount) { index -> colors[index % colors.size] }
}
