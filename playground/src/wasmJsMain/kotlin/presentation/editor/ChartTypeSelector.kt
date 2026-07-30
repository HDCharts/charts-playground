package presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_chart_selector_more
import domain.ChartType
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import presentation.resources.chartTypeIconResource

@Composable
fun ChartTypeSelector(
    selectedType: ChartType,
    primaryTypes: List<ChartType>,
    overflowTypes: List<ChartType>,
    onTypeSelected: (ChartType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = 8.dp
    val buttonContentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
    var overflowExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        primaryTypes.forEach { chartType ->
            val selected = selectedType == chartType
            Button(
                onClick = { onTypeSelected(chartType) },
                contentPadding = buttonContentPadding,
                colors =
                    if (selected) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                modifier = Modifier.weight(1f),
            ) {
                ChartTypeButtonIcon(chartType = chartType)
                Text(
                    text = chartType.displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (overflowTypes.isNotEmpty()) {
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { overflowExpanded = true },
                    contentPadding = buttonContentPadding,
                    colors = ButtonDefaults.outlinedButtonColors(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.playground_chart_selector_more))
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                ) {
                    overflowTypes.forEach { chartType ->
                        DropdownMenuItem(
                            text = { Text(chartType.displayName) },
                            onClick = {
                                onTypeSelected(chartType)
                                overflowExpanded = false
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
}

@Composable
private fun ChartTypeButtonIcon(chartType: ChartType) {
    val iconSize = 30.dp
    Icon(
        painter = painterResource(chartTypeIconResource(chartType)),
        contentDescription = null,
        modifier = Modifier.size(iconSize),
    )
}
