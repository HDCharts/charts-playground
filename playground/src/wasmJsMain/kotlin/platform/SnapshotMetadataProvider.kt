package platform

import config.BuildConfig
import domain.SnapshotPublishMetadata
import kotlin.time.Instant

internal fun snapshotPublishMetadata(): SnapshotPublishMetadata? =
    BuildConfig.SNAPSHOT_METADATA_CHARTS_SHA
        .takeIf(String::isNotBlank)
        ?.let { chartsSha ->
            SnapshotPublishMetadata(
                chartsSha = chartsSha,
                playgroundSha = BuildConfig.SNAPSHOT_METADATA_PLAYGROUND_SHA,
                publishedAt =
                    BuildConfig.SNAPSHOT_METADATA_PUBLISHED_AT
                        .takeIf(String::isNotBlank)
                        ?.let { runCatching { Instant.parse(it) }.getOrNull() },
            )
        }
