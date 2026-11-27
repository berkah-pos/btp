package id.berkahtigaputeri.basicpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import id.berkahtigaputeri.basicpos.navigation.PosNavigation
import id.berkahtigaputeri.basicpos.ui.theme.BerkahPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            BerkahPosTheme {
                PosNavigation()
            }
        }
    }
}
