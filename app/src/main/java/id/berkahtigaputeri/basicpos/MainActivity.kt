package id.berkahtigaputeri.basicpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import id.berkahtigaputeri.basicpos.navigation.PosNavigation
import id.berkahtigaputeri.basicpos.ui.theme.BerkahPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BerkahPosTheme {
                PosNavigation()
            }
        }
    }
}
