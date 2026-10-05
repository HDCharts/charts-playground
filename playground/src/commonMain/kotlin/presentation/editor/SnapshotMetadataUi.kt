package presentation.editor

import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_metadata_published_days
import chartsproject.playground.generated.resources.playground_metadata_published_hours
import chartsproject.playground.generated.resources.playground_metadata_published_minutes
import chartsproject.playground.generated.resources.playground_metadata_published_months
import chartsproject.playground.generated.resources.playground_metadata_published_years
import domain.SnapshotPublishMetadata
import org.jetbrains.compose.resources.PluralStringResource
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

data class SnapshotMetadataUi(
    val chartsSha: String,
    val playgroundSha: String,
    val publishedLabel: PublishedLabel?,
)

data class PublishedLabel(
    val resource: PluralStringResource,
    val count: Int,
)

fun SnapshotPublishMetadata.toUI(now: Instant): SnapshotMetadataUi =
    SnapshotMetadataUi(
        chartsSha = chartsSha,
        playgroundSha = playgroundSha,
        publishedLabel = publishedAt?.let { publishedLabel(it, now) },
    )

internal fun publishedLabel(
    publishedAt: Instant,
    now: Instant,
): PublishedLabel {
    val elapsed = (now - publishedAt).coerceAtLeast(1.minutes)
    return when {
        elapsed < 1.hours ->
            PublishedLabel(Res.plurals.playground_metadata_published_minutes, elapsed.inWholeMinutes.toInt())
        elapsed < 1.days ->
            PublishedLabel(Res.plurals.playground_metadata_published_hours, elapsed.inWholeHours.toInt())
        elapsed < 30.days ->
            PublishedLabel(Res.plurals.playground_metadata_published_days, elapsed.inWholeDays.toInt())
        elapsed < 365.days ->
            PublishedLabel(Res.plurals.playground_metadata_published_months, (elapsed.inWholeDays / 30).toInt())
        else ->
            PublishedLabel(Res.plurals.playground_metadata_published_years, (elapsed.inWholeDays / 365).toInt())
    }
}
