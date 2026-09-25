package platform

private const val THEME_KEY = "hdcharts.playground.theme"
private const val DARK = "dark"
private const val LIGHT = "light"

/** Returns the theme the user picked earlier, or null to follow the system setting. */
internal fun loadDarkThemePreference(): Boolean? =
    when (readStorage(THEME_KEY)) {
        DARK -> true
        LIGHT -> false
        else -> null
    }

internal fun saveDarkThemePreference(darkTheme: Boolean) {
    writeStorage(THEME_KEY, if (darkTheme) DARK else LIGHT)
}

// localStorage can throw (e.g. blocked site data), so failures fall back to the system setting.
private fun readStorage(key: String): String? =
    js("(() => { try { return localStorage.getItem(key); } catch (e) { return null; } })()")

private fun writeStorage(
    key: String,
    value: String,
): Unit = js("(() => { try { localStorage.setItem(key, value); } catch (e) {} })()")
