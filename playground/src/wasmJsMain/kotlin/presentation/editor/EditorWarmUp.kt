package presentation.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.ChartType

private const val WARM_UP_MILLIS = 1000L

// Draws every chart's editor once so the GPU compiles its shaders before the first chart switch.
@Composable
fun EditorWarmUp(
    chartTypes: List<ChartType>,
    onFinished: () -> Unit,
    editor: @Composable (ChartType) -> Unit,
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        // Skip the slow first frame so all of WARM_UP_MILLIS goes to the animations.
        withFrameMillis { }
        val start = withFrameMillis { it }
        while (withFrameMillis { it } - start < WARM_UP_MILLIS) Unit
        currentOnFinished()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        chartTypes.forEach { chartType ->
            key(chartType) {
                // Not full size: a full-page opaque background makes the GPU skip the editors below.
                Box(modifier = Modifier.fillMaxSize().padding(end = 1.dp)) {
                    editor(chartType)
                }
            }
        }
    }
}
