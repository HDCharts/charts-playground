package domain

import kotlin.jvm.JvmInline

/** Platform-neutral ARGB color value used by editor state and actions. */
@JvmInline
value class ColorValue(
    val argb: Long,
)
