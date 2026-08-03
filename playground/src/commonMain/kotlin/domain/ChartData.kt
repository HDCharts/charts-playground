package domain

sealed interface ChartData {
    data class SingleSeries(
        val values: List<Float>,
        val labels: List<String>? = null,
    ) : ChartData

    data class MultiSeries(
        val series: List<Series>,
        val xLabels: List<String>? = null,
    ) : ChartData {
        data class Series(
            val name: String,
            val values: List<Float>,
        )
    }

    data class StackedSeries(
        val segmentNames: List<String>,
        val bars: List<StackedBar>,
        val labels: List<String>? = null,
    ) : ChartData {
        data class StackedBar(
            val label: String,
            val values: List<Float>,
        )
    }

    data class RadarSeries(
        val entries: List<RadarEntry>,
        val axes: List<String>,
    ) : ChartData {
        data class RadarEntry(
            val name: String,
            val values: List<Float>,
        )
    }
}
