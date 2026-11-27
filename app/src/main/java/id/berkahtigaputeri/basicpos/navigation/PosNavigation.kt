package id.berkahtigaputeri.basicpos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.berkahtigaputeri.basicpos.BerkahPosApplication
import id.berkahtigaputeri.basicpos.ui.dashboard.DashboardScreen
import id.berkahtigaputeri.basicpos.ui.dashboard.DashboardViewModel

private object PosRoute {
    const val DASHBOARD = "dashboard"
}

@Composable
fun PosNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as BerkahPosApplication
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(application.container.posRepository)
    )

    NavHost(
        navController = navController,
        startDestination = PosRoute.DASHBOARD
    ) {
        composable(PosRoute.DASHBOARD) {
            DashboardScreen(viewModel = dashboardViewModel)
        }
    }
}
