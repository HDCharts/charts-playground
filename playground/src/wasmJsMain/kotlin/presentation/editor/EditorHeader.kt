package presentation.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chartsproject.charts_demo_shared.generated.resources.charts_logo
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_editor_select_chart_content_description
import chartsproject.playground.generated.resources.playground_logo_content_description
import chartsproject.playground.generated.resources.playground_metadata_charts
import chartsproject.playground.generated.resources.playground_metadata_playground
import chartsproject.playground.generated.resources.playground_metadata_published
import chartsproject.playground.generated.resources.playground_title
import config.BuildConfig
import domain.ChartEditorState
import domain.ChartType
import domain.EditorAction
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import presentation.resources.chartTypeIconResource
import kotlin.time.Instant
import chartsproject.charts_demo_shared.generated.resources.Res as SharedRes

private const val CHARTS_GITHUB_URL = "https://github.com/HDCharts/charts"
private const val PLAYGROUND_GITHUB_URL = "https://github.com/HDCharts/charts-playground"

@Composable
internal fun EditorHeader(
    state: ChartEditorState,
    selectedChartType: ChartType,
    onAction: (EditorAction) -> Unit,
    onOpenUri: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val inlineChartSwitcher = maxWidth >= EditorWideLayoutBreakpoint
        val compactHeader = maxWidth < EditorCompactHeaderBreakpoint
        var chartMenuExpanded by remember { mutableStateOf(false) }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(SharedRes.drawable.charts_logo),
                        contentDescription = stringResource(Res.string.playground_logo_content_description),
                        modifier = Modifier.size(32.dp),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(Res.string.playground_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = BuildConfig.CHARTS_VERSION,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            state.snapshotMetadata?.let { metadata ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text =
                                            stringResource(
                                                Res.string.playground_metadata_charts,
                                                metadata.chartsSha.take(7),
                                            ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier =
                                            Modifier.clickable {
                                                onOpenUri(
                                                    "$CHARTS_GITHUB_URL/commit/${metadata.chartsSha}",
                                                )
                                            },
                                    )
                                    Text(
                                        text =
                                            stringResource(
                                                Res.string.playground_metadata_playground,
                                                metadata.playgroundSha.take(7),
                                            ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier =
                                            Modifier.clickable {
                                                onOpenUri(
                                                    "$PLAYGROUND_GITHUB_URL/commit/${metadata.playgroundSha}",
                                                )
                                            },
                                    )
                                }
                            }
                            PublishedMetadataLabel(state.snapshotMetadata?.publishedAt)
                        }
                    }
                }

                if (inlineChartSwitcher) {
                    ChartTypeSelector(
                        selectedType = state.selectedChartType,
                        primaryTypes = state.primaryChartTypes,
                        overflowTypes = state.overflowChartTypes,
                        onTypeSelected = { chartType -> onAction(EditorAction.SelectChart(chartType)) },
                        modifier = Modifier.weight(1f),
                    )
                } else if (compactHeader) {
                    Text(
                        text = selectedChartType.displayName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        IconButton(onClick = { chartMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription =
                                    stringResource(
                                        Res.string.playground_editor_select_chart_content_description,
                                    ),
                            )
                        }
                        DropdownMenu(
                            expanded = chartMenuExpanded,
                            onDismissRequest = { chartMenuExpanded = false },
                        ) {
                            (state.primaryChartTypes + state.overflowChartTypes).forEach { chartType ->
                                DropdownMenuItem(
                                    text = { Text(chartType.displayName) },
                                    onClick = {
                                        onAction(EditorAction.SelectChart(chartType))
                                        chartMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(chartTypeIconResource(chartType)),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (!inlineChartSwitcher && !compactHeader) {
                ChartTypeSelector(
                    selectedType = state.selectedChartType,
                    primaryTypes = state.primaryChartTypes,
                    overflowTypes = state.overflowChartTypes,
                    onTypeSelected = { chartType -> onAction(EditorAction.SelectChart(chartType)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
@OptIn(FormatStringsInDatetimeFormats::class)
private fun PublishedMetadataLabel(publishedAt: Instant?) {
    val formattedPublishedAt =
        publishedAt
            ?.format(
                DateTimeComponents.Format { byUnicodePattern("MMM d, yyyy, h:mm a 'UTC'") },
                UtcOffset.ZERO,
            )
            ?: "Unavailable"

    Text(
        text = stringResource(Res.string.playground_metadata_published, formattedPublishedAt),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
