package id.berkahtigaputeri.basicpos.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.berkahtigaputeri.basicpos.BerkahPosApplication
import id.berkahtigaputeri.basicpos.ui.dashboard.DashboardScreen
import id.berkahtigaputeri.basicpos.ui.dashboard.DashboardViewModel
import id.berkahtigaputeri.basicpos.ui.products.ProductsScreen
import id.berkahtigaputeri.basicpos.ui.products.ProductsViewModel

private object PosRoute {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
}

private data class NavigationDestination(
    val route: String,
    val label: String,
    val symbol: String
)

private val destinations = listOf(
    NavigationDestination(PosRoute.DASHBOARD, "Ringkasan", "⌂"),
    NavigationDestination(PosRoute.PRODUCTS, "Produk", "▦")
)

@Composable
fun PosNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as BerkahPosApplication
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(destination.symbol) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PosRoute.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(PosRoute.DASHBOARD) {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(application.container.posRepository)
                )
                DashboardScreen(viewModel = dashboardViewModel)
            }
            composable(PosRoute.PRODUCTS) {
                val productsViewModel: ProductsViewModel = viewModel(
                    factory = ProductsViewModel.Factory(application.container.posRepository)
                )
                ProductsScreen(viewModel = productsViewModel)
            }
        }
    }
}
