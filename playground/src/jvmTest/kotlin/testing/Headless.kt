package testing

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.coroutines.EmptyCoroutineContext

/** Runs [block] once in a headless composition and returns its result. */
fun <T> readComposable(block: @Composable () -> T): T {
    var result: T? = null
    var produced = false
    val recomposer = Recomposer(EmptyCoroutineContext)
    val composition = Composition(NoOpApplier, recomposer)
    try {
        composition.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                result = block()
                produced = true
            }
        }
    } finally {
        composition.dispose()
        recomposer.cancel()
    }
    check(produced) { "Composable did not run" }
    @Suppress("UNCHECKED_CAST")
    return result as T
}

private object NoOpApplier : AbstractApplier<Unit>(Unit) {
    override fun insertTopDown(
        index: Int,
        instance: Unit,
    ) = Unit

    override fun insertBottomUp(
        index: Int,
        instance: Unit,
    ) = Unit

    override fun remove(
        index: Int,
        count: Int,
    ) = Unit

    override fun move(
        from: Int,
        to: Int,
        count: Int,
    ) = Unit

    override fun onClear() = Unit
}
