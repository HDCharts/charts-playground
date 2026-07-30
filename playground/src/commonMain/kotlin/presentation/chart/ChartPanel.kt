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
                value = session.title,
                onValueChange = onTitleChange,
                singleLine = true,
                label = { Text(stringResource(Res.string.playground_chart_title_label)) },
                modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth().widthIn(max = 760.dp),
                ) {
                    ChartRenderer(type = chartType, session = session)
                }
            }
        }
    }
}
