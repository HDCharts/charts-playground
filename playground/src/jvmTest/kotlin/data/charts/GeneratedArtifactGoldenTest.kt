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
                ChartType.LINE to "c865f6ebccd11752bdfbe9a822109d14fbca2fe8cb079039ea427b1c090ab35a",
                ChartType.BAR to "7df18f5a9a7ae1882684cb5680acbb788e9dc86bf58f0e0abfe31e80042b71cc",
                ChartType.PIE to "ecf8d84cc0f29060a24fb138ad8bf93aa939f631f6dc87c3dce59a31d61b339a",
                ChartType.RADAR to "caea4c1f9861945da2db8b4b57a06640d48e1c28b4fafab88b140b9e8571f6b2",
                ChartType.AREA to "20e0635330721cf56f2a54b4dd7bf172c6e8a7863d045b2aebac092cc5c70ca1",
                ChartType.MULTI_LINE to "fa8e877b52cfc1bad70f13165fb49f427808b2f09642633f6591f68a033c538c",
                ChartType.HISTOGRAM to "beb7aeb61e33552195f0a77e0ec4b5f0b15bc1300d674a4963c2b6bf7f74d4d2",
                ChartType.STACKED_BAR to "ff3833d28820efcc0d3a57bd390dcf74cb91985cfd7f13d7bd82e4ad09d49570",
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
