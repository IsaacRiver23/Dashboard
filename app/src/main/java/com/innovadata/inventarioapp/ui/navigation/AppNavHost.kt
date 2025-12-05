package com.innovadata.inventarioapp.ui.navigation
import com.innovadata.inventarioapp.ui.screens.SalesHistoryScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.innovadata.inventarioapp.ui.screens.AddProductScreen
import com.innovadata.inventarioapp.ui.screens.DetailScreen
import com.innovadata.inventarioapp.ui.screens.EditProductScreen
import com.innovadata.inventarioapp.ui.screens.HomeScreen
import com.innovadata.inventarioapp.ui.screens.ProductListScreen
import com.innovadata.inventarioapp.ui.screens.ReportsScreen
import com.innovadata.inventarioapp.ui.screens.StatsScreen
import com.innovadata.inventarioapp.viewmodel.InventoryViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: InventoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(navController)
        }

        composable("add") {
            AddProductScreen(navController, viewModel)
        }

        composable("list") {
            ProductListScreen(navController, viewModel)
        }

        composable("detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
            DetailScreen(navController, id, viewModel)
        }

        composable("edit/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
            EditProductScreen(viewModel, navController, id)
        }

        composable("reports") {
            ReportsScreen(viewModel)
        }

        // 👇 NUEVA RUTA DE ESTADÍSTICAS
        composable("stats") {
            StatsScreen(viewModel)
        }
        composable("sales") {
            SalesHistoryScreen(viewModel)
        }
    }
}
