package presentation.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_logo_content_description
import chartsproject.playground.generated.resources.playground_metadata_charts
import chartsproject.playground.generated.resources.playground_metadata_playground
import chartsproject.playground.generated.resources.playground_metadata_published
import chartsproject.playground.generated.resources.playground_nav_build_info
import chartsproject.playground.generated.resources.playground_nav_switch_to_dark
import chartsproject.playground.generated.resources.playground_nav_switch_to_light
import chartsproject.playground.generated.resources.playground_title
import config.BuildConfig
import domain.ChartType
import domain.SnapshotMetadataUi
import hdcharts.sample_shared.generated.resources.charts_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import presentation.resources.chartTypeIconResource
import hdcharts.sample_shared.generated.resources.Res as SharedRes

private const val CHARTS_GITHUB_URL = "https://github.com/HDCharts/charts"
private const val PLAYGROUND_GITHUB_URL = "https://github.com/HDCharts/charts-playground"

private val RailWidth = 88.dp
private val DrawerWidth = 280.dp
private val ChartTypeIconSize = 24.dp

/** Fixed left rail listing every chart type, used on medium and wide windows. */
@Composable
internal fun ChartTypeRail(
    chartTypes: List<ChartType>,
    selectedType: ChartType,
    snapshotMetadata: SnapshotMetadataUi?,
    onTypeSelected: (ChartType) -> Unit,
    onOpenUri: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.width(RailWidth).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(SharedRes.drawable.charts_logo),
                contentDescription = stringResource(Res.string.playground_logo_content_description),
                modifier = Modifier.padding(top = 20.dp, bottom = 12.dp).size(36.dp),
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                chartTypes.forEach { chartType ->
                    NavigationRailItem(
                        selected = chartType == selectedType,
                        onClick = { onTypeSelected(chartType) },
                        icon = { ChartTypeIcon(chartType) },
                        label = {
                            Text(
                                text = chartType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        },
                        alwaysShowLabel = true,
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ThemeToggleButton(
                darkTheme = darkTheme,
                onToggle = onToggleTheme,
                modifier = Modifier.padding(top = 8.dp),
            )
            BuildInfoButton(
                snapshotMetadata = snapshotMetadata,
                onOpenUri = onOpenUri,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

/** Divider placed between the rail and the workspace. */
@Composable
internal fun ChartTypeRailDivider() {
    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** Chart type list shown inside the modal drawer on compact windows. */
@Composable
internal fun ChartTypeDrawerContent(
    chartTypes: List<ChartType>,
    selectedType: ChartType,
    onTypeSelected: (ChartType) -> Unit,
) {
    Surface(
        modifier = Modifier.width(DrawerWidth).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PlaygroundBrand(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp))
            chartTypes.forEach { chartType ->
                NavigationDrawerItem(
                    selected = chartType == selectedType,
                    onClick = { onTypeSelected(chartType) },
                    icon = { ChartTypeIcon(chartType) },
                    label = { Text(chartType.displayName) },
                )
            }
        }
    }
}

@Composable
internal fun PlaygroundBrand(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(SharedRes.drawable.charts_logo),
            contentDescription = stringResource(Res.string.playground_logo_content_description),
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = stringResource(Res.string.playground_title),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

/** Switches between the light and dark playground themes. */
@Composable
internal fun ThemeToggleButton(
    darkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        if (darkTheme) {
            Icon(
                imageVector = Icons.Outlined.LightMode,
                contentDescription = stringResource(Res.string.playground_nav_switch_to_light),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.DarkMode,
                contentDescription = stringResource(Res.string.playground_nav_switch_to_dark),
            )
        }
    }
}

/** Info button revealing the charts version, source commits, and publish date. */
@Composable
internal fun BuildInfoButton(
    snapshotMetadata: SnapshotMetadataUi?,
    onOpenUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(Res.string.playground_nav_build_info),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(Res.string.playground_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = BuildConfig.CHARTS_VERSION,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            snapshotMetadata?.let { metadata ->
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(Res.string.playground_metadata_charts, metadata.chartsSha.take(7)),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        onOpenUri("$CHARTS_GITHUB_URL/commit/${metadata.chartsSha}")
                        expanded = false
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text =
                                stringResource(
                                    Res.string.playground_metadata_playground,
                                    metadata.playgroundSha.take(7),
                                ),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = {
                        onOpenUri("$PLAYGROUND_GITHUB_URL/commit/${metadata.playgroundSha}")
                        expanded = false
                    },
                )
                val publishedAt = metadata.publishedAt
                if (!publishedAt.isNullOrBlank()) {
                    Text(
                        text = stringResource(Res.string.playground_metadata_published, publishedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartTypeIcon(chartType: ChartType) {
    Icon(
        painter = painterResource(chartTypeIconResource(chartType)),
        contentDescription = null,
        modifier = Modifier.size(ChartTypeIconSize),
    )
}
