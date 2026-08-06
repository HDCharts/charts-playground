package domain

import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlin.time.Instant

data class SnapshotMetadataUi(
    val chartsSha: String,
    val playgroundSha: String,
    val publishedAt: String?,
)

@OptIn(FormatStringsInDatetimeFormats::class)
fun SnapshotPublishMetadata.toUI(): SnapshotMetadataUi =
    SnapshotMetadataUi(
        chartsSha = chartsSha,
        playgroundSha = playgroundSha,
        publishedAt = formatPublishedAt(publishedAt),
    )

@OptIn(FormatStringsInDatetimeFormats::class)
private fun formatPublishedAt(instant: Instant?): String? {
    if (instant == null) return null
    return try {
        instant.format(
            DateTimeComponents.Format { byUnicodePattern("yyyy-MM-dd HH:mm 'UTC'") },
            UtcOffset.ZERO,
        )
    } catch (_: Exception) {
        null
    }
}
