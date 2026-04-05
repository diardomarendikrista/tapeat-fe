package com.group2.tapeat.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.group2.tapeat.ui.view.AdminView
import com.group2.tapeat.ui.view.CustomerView
import com.group2.tapeat.ui.view.KitchenView

/**
 * Pusat untuk mengatur semua logika perpindahan halaman (Routing/Navigation Graph).
 */
@Composable
fun AppRoutes(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Customer.route, // Halaman default saat aplikasi dibuka
        modifier = modifier
    ) {
        // Halaman Customer
        // Mapping ke endpoint: GET /api/products & POST /api/orders
        composable(Screen.Customer.route) {
            CustomerView()
        }

        // Halaman Kitchen
        // Mapping ke endpoint: GET /api/queue/active & PUT /api/queue/{id}/status
        composable(Screen.Kitchen.route) {
            KitchenView()
        }

        // Halaman Admin
        // Mapping ke endpoint: CRUD /api/products
        composable(Screen.Admin.route) {
            AdminView()
        }
    }
}