package presentation.editor

import chartsproject.playground.generated.resources.Res
import chartsproject.playground.generated.resources.playground_metadata_published_days
import chartsproject.playground.generated.resources.playground_metadata_published_hours
import chartsproject.playground.generated.resources.playground_metadata_published_minutes
import chartsproject.playground.generated.resources.playground_metadata_published_months
import chartsproject.playground.generated.resources.playground_metadata_published_years
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SnapshotMetadataUiTest {
    private val now = Instant.parse("2026-10-05T12:00:00Z")

    private fun labelAfter(elapsed: Duration) = publishedLabel(publishedAt = now - elapsed, now = now)

    @Test
    fun under_a_minute_and_future_times_read_as_one_minute() {
        val oneMinute = PublishedLabel(Res.plurals.playground_metadata_published_minutes, 1)
        assertEquals(oneMinute, labelAfter(10.seconds))
        assertEquals(oneMinute, publishedLabel(publishedAt = now + 5.minutes, now = now))
    }

    @Test
    fun picks_the_largest_whole_unit() {
        val plurals = Res.plurals
        assertEquals(PublishedLabel(plurals.playground_metadata_published_minutes, 59), labelAfter(59.minutes))
        assertEquals(PublishedLabel(plurals.playground_metadata_published_hours, 2), labelAfter(2.hours + 30.minutes))
        assertEquals(PublishedLabel(plurals.playground_metadata_published_days, 1), labelAfter(1.days))
        assertEquals(PublishedLabel(plurals.playground_metadata_published_days, 29), labelAfter(29.days))
        assertEquals(PublishedLabel(plurals.playground_metadata_published_months, 1), labelAfter(30.days))
        assertEquals(PublishedLabel(plurals.playground_metadata_published_years, 1), labelAfter(365.days))
    }
}
