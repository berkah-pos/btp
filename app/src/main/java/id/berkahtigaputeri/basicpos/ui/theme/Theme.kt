package id.berkahtigaputeri.basicpos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BerkahGreen,
    secondary = BerkahAmber
)

private val DarkColors = darkColorScheme(
    primary = BerkahGreenDark,
    secondary = BerkahAmber
)

@Composable
fun BerkahPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = BerkahTypography,
        content = content
    )
}
