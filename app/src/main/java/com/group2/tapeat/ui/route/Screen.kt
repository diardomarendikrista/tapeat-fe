package com.group2.tapeat.ui.route

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class yg digunakan untuk mendefinisikan rute (halaman) yang ada di aplikasi.
 * @param route URL/Path internal untuk pindah halaman.
 * @param title Teks label yang akan ditampilkan pada menu Bottom Navigation.
 * @param icon Ikon yang akan ditampilkan pada menu Bottom Navigation.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Rute untuk halaman Customer (Pemesanan Makanan)
    object Customer : Screen(
        "customer_route",
        "Order",
        Icons.Default.ShoppingCart
    )

    // Rute untuk halaman Kitchen (Sistem Antrean Dapur)
    object Kitchen : Screen(
        "kitchen_route",
        "Kitchen",
        Icons.Default.List
    )

    // Rute untuk halaman Admin (Manajemen Produk/Menu)
    object Admin : Screen(
        "admin_route",
        "Admin",
        Icons.Default.Settings
    )
}
