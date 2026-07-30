package platform

import config.BuildConfig
import domain.SnapshotPublishMetadata

internal fun snapshotPublishMetadata(): SnapshotPublishMetadata? =
    BuildConfig.SNAPSHOT_METADATA_CHARTS_SHA
        .takeIf(String::isNotBlank)
        ?.let { chartsSha ->
            SnapshotPublishMetadata(
                chartsSha = chartsSha,
                playgroundSha = BuildConfig.SNAPSHOT_METADATA_PLAYGROUND_SHA,
                publishedAt = BuildConfig.SNAPSHOT_METADATA_PUBLISHED_AT,
            )
        }
