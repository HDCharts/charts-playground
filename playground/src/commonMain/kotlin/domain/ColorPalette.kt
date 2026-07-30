package domain

fun normalizeColorCount(
    colors: List<ColorValue>,
    targetCount: Int,
): List<ColorValue> {
    if (targetCount <= 0 || colors.isEmpty()) return emptyList()
    return List(targetCount) { index -> colors[index % colors.size] }
}
