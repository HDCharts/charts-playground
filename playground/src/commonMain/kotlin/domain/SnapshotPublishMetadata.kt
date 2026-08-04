package domain

import kotlin.time.Instant

data class SnapshotPublishMetadata(
    val chartsSha: String,
    val playgroundSha: String,
    val publishedAt: Instant?,
)
