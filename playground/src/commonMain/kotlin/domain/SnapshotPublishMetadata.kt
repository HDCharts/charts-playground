package domain

data class SnapshotPublishMetadata(
    val chartsSha: String,
    val playgroundSha: String,
    val publishedAt: String,
)
