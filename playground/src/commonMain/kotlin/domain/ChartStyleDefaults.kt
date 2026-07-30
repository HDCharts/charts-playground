package domain

object ChartColorDefaults {
    val primary = ColorValue(0xFF6750A4L)
    val tertiary = ColorValue(0xFF7D5260L)
}

object PieStyleDefaults {
    val donutPercentage = 0f
    val borderWidth = 3f
    val pieAlpha = 0.4f
    val legendVisible = true
}

object LineStyleDefaults {
    val lineColor = ChartColorDefaults.primary
    val pointColor = ChartColorDefaults.tertiary
    val lineAlpha = 0.4f
    val bezier = true
    val pointVisible = false
    val pointSize = 9f
    val dragPointVisible = true
    val dragPointSize = 7f
    val dragActivePointSize = 12f
    val axisVisible = true
    val axisLineWidth = 1f
    val xAxisLabelsVisible = true
    val yAxisLabelsVisible = true
    val zoomControlsVisible = true
}

object MultiLineStyleDefaults {
    val pointColor = LineStyleDefaults.pointColor
    val lineAlpha = LineStyleDefaults.lineAlpha
    val bezier = LineStyleDefaults.bezier
    val pointVisible = LineStyleDefaults.pointVisible
    val dragPointVisible = LineStyleDefaults.dragPointVisible
}

object BarStyleDefaults {
    val barColor = ChartColorDefaults.primary
    val barAlpha = 0.4f
    val gridVisible = true
    val axisVisible = true
    val selectionLineVisible = true
    val selectionLineWidth = 1f
    val zoomControlsVisible = true
}

object StackedBarStyleDefaults {
    val barColor = BarStyleDefaults.barColor
    val barAlpha = BarStyleDefaults.barAlpha
    val selectionLineVisible = BarStyleDefaults.selectionLineVisible
    val selectionLineWidth = BarStyleDefaults.selectionLineWidth
    val zoomControlsVisible = BarStyleDefaults.zoomControlsVisible
}

object AreaStyleDefaults {
    val areaColor = ChartColorDefaults.primary
    val lineColor = ChartColorDefaults.primary
    val fillAlpha = 0.4f
    val lineVisible = true
    val lineWidth = 4f
    val bezier = false
    val zoomControlsVisible = true
}

object RadarStyleDefaults {
    val lineWidth = 3f
    val pointVisible = true
    val pointSize = 9f
    val fillVisible = true
    val fillAlpha = 0.25f
    val gridVisible = true
    val categoryLegendVisible = true
}
