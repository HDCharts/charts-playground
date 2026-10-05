package platform

// Compose blurs its hidden text input when a text field closes, which leaves the page focused, and
// key presses then stop reaching Compose. Focusing Compose's canvas again brings them back.
internal fun focusComposeCanvas(): Unit =
    js(
        """(() => {
            for (const host of document.querySelectorAll('*')) {
                const canvas = host.shadowRoot && host.shadowRoot.querySelector('canvas');
                if (canvas) { canvas.focus({ preventScroll: true }); return; }
            }
        })()""",
    )
