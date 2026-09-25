package domain

sealed interface ChartData {
    /** One value per label: line, bar, histogram, pie. */
    data class SingleSeries(
        val values: List<Float>,
        val labels: List<String>? = null,
    ) : ChartData

    /**
     * Named series over shared categories: multi-line, area, stacked bar (a series per segment),
     * and radar (a category per axis). Every series has one value per category.
     */
    data class MultiSeries(
        val categories: List<String>,
        val series: List<Series>,
    ) : ChartData {
        init {
            require(series.all { it.values.size == categories.size }) {
                "Every series needs one value per category (${categories.size})"
            }
        }

        data class Series(
            val name: String,
            val values: List<Float>,
        )
    }
}
