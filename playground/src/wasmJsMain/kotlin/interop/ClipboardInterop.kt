package interop

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

@OptIn(ExperimentalComposeUiApi::class)
suspend fun copyTextToClipboard(
    clipboard: Clipboard,
    text: String,
): Boolean =
    runCatching {
        clipboard.setClipEntry(ClipEntry.withPlainText(text))
    }.isSuccess
