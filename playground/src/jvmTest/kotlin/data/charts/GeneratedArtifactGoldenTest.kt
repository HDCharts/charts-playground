package data.charts

import data.ChartCodegenService
import data.chartCatalog
import domain.ChartType
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneratedArtifactGoldenTest {
    @Test
    fun every_chart_type_has_a_stable_generated_source_golden() {
        val service = ChartCodegenService()
        val actualHashes =
            chartCatalog.charts.associate { definition ->
                definition.type to sha256(service.generateArtifact(definition.resetSession().validatedSpec).source)
            }

        assertEquals(
            mapOf(
                ChartType.LINE to "8dce72f75b8cd6bf87aaa4830cf6ecff64c4ab89bd3b5adf841c5ae4d954f7c7",
                ChartType.BAR to "499ff78f3147ec8b4d53ab520c341facc7f8afad3a5ff50ad0c27ba9cbe515de",
                ChartType.PIE to "86018be2bcbc3579b5f4604615229c99a2f43858e65b7f91f182a038871282df",
                ChartType.RADAR to "103587674b5bec86485eb1704dd444f0f7dcce78c6d6f7cfcf65386b0c8cdc87",
                ChartType.AREA to "4639205cbc05d157be8b63b6012b1a09832bb4842b7236c5b354191fa0687bcc",
                ChartType.MULTI_LINE to "75b70eb59a6695eeb49394b25f11791d330c24f61044c2211279ef4549f62f32",
                ChartType.HISTOGRAM to "8eb8d9b1d7dbe65acdd0dd089ac5f6704c8b77110e7dff900c0c7c69ce799964",
                ChartType.STACKED_BAR to "f4b40795d3231d37193cb1455dc48e776657f9380127e07d7cff8dcfbef1c1ca",
            ),
            actualHashes,
        )
    }
}

private fun sha256(value: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(value.encodeToByteArray())
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
