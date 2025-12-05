package com.innovadata.inventarioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.innovadata.inventarioapp.ui.navigation.AppNavHost
import com.innovadata.inventarioapp.ui.theme.InventarioAppTheme
import com.innovadata.inventarioapp.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventarioAppTheme {
                val navController = rememberNavController()
                val inventoryViewModel: InventoryViewModel = viewModel()

                AppNavHost(
                    navController = navController,
                    viewModel = inventoryViewModel
                )
            }
        }
    }
}
