package presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_right_panel_code
import chartsproject.playground.generated.resources.playground_right_panel_settings
import domain.RightPanelTab
import org.jetbrains.compose.resources.stringResource

private val RightPanelTabIconSize = 18.dp

@Composable
internal fun RightPanel(
    tab: RightPanelTab,
    onTabChange: (RightPanelTab) -> Unit,
    settingsContent: @Composable () -> Unit,
    codeContent: @Composable () -> Unit,
    showTabSelector: Boolean = true,
    expandToFillHeight: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (showTabSelector) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val compactTabs = maxWidth < 360.dp
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { onTabChange(RightPanelTab.SETTINGS) },
                            contentPadding = PaddingValues(horizontal = if (compactTabs) 8.dp else 10.dp),
                            colors = tabButtonColors(tab == RightPanelTab.SETTINGS),
                            modifier = Modifier.weight(1f),
                        ) {
                            if (compactTabs) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = stringResource(Res.string.playground_right_panel_settings),
                                    modifier = Modifier.size(RightPanelTabIconSize),
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(RightPanelTabIconSize),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(Res.string.playground_right_panel_settings), maxLines = 1)
                                }
                            }
                        }

                        Button(
                            onClick = { onTabChange(RightPanelTab.CODE) },
                            contentPadding = PaddingValues(horizontal = if (compactTabs) 8.dp else 10.dp),
                            colors = tabButtonColors(tab == RightPanelTab.CODE),
                            modifier = Modifier.weight(1f),
                        ) {
                            if (compactTabs) {
                                Icon(
                                    imageVector = Icons.Filled.Code,
                                    contentDescription = stringResource(Res.string.playground_right_panel_code),
                                    modifier = Modifier.size(RightPanelTabIconSize),
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Code,
                                        contentDescription = null,
                                        modifier = Modifier.size(RightPanelTabIconSize),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(Res.string.playground_right_panel_code), maxLines = 1)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()
                panelContent(
                    content = if (tab == RightPanelTab.SETTINGS) settingsContent else codeContent,
                    modifier =
                        if (expandToFillHeight) {
                            Modifier.fillMaxWidth().weight(1f)
                        } else {
                            Modifier.fillMaxWidth()
                        },
                )
            } else {
                panelContent(content = settingsContent, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun panelContent(
    content: @Composable () -> Unit,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
    ) {
        content()
    }
}

@Composable
private fun tabButtonColors(selected: Boolean) =
    if (selected) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
