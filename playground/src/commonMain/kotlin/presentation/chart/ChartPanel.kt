package presentation.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_chart_title_label
import domain.ChartSession
import domain.ChartType
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChartPanel(
    session: ChartSession,
    chartType: ChartType,
    onTitleChange: (String) -> Unit,
    expandToFillHeight: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        val columnModifier =
            Modifier
                .fillMaxWidth()
                .then(if (expandToFillHeight) Modifier.fillMaxHeight() else Modifier)
                .padding(16.dp)

        Column(modifier = columnModifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = session.draft.title,
                onValueChange = onTitleChange,
                singleLine = true,
                label = { Text(stringResource(Res.string.playground_chart_title_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = session.validatedSpec.previewSummary(),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(
                modifier =
                    if (expandToFillHeight) {
                        Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                    } else {
                        Modifier.fillMaxWidth().heightIn(min = 220.dp)
                    },
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .widthIn(max = 760.dp)
                            .semantics {
                                contentDescription = session.validatedSpec.previewSummary()
                            },
                ) {
                    ChartRenderer(type = chartType, spec = session.validatedSpec)
                }
            }
        }
    }
}

private fun domain.ValidatedChartSpec.previewSummary(): String =
    when (val chartData = data) {
        is domain.ChartData.SingleSeries -> "${chartType.displayName} preview with ${chartData.values.size} values."
        is domain.ChartData.MultiSeries ->
            "${chartType.displayName} preview with ${chartData.series.size} series and " +
                "${chartData.xLabels?.size ?: 0} categories."
        is domain.ChartData.StackedSeries ->
            "${chartType.displayName} preview with ${chartData.bars.size} bars and " +
                "${chartData.segmentNames.size} segments."
        is domain.ChartData.RadarSeries ->
            "${chartType.displayName} preview with ${chartData.entries.size} entries and " +
                "${chartData.axes.size} axes."
    }
