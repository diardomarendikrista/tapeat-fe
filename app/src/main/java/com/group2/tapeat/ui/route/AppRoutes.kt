package com.group2.tapeat.ui.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.group2.tapeat.ui.view.AdminView
import com.group2.tapeat.ui.view.CashierView
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
        /**
         * Halaman Customer (Pemesanan Mandiri)
         * - Integrasi Backend:
         * 1. GET /api/products -> Mengambil daftar menu yang aktif (isActive=true).
         * 2. POST /api/orders  -> Mengirim payload pesanan (status awal: UNPAID).
         */
        composable(Screen.Customer.route) {
            CustomerView()
        }

        /**
         * Halaman Kasir (Konfirmasi Pembayaran)
         * - Integrasi Backend:
         * 1. GET /api/orders -> Menampilkan riwayat pesanan (filter status UNPAID di frontend).
         * 2. PUT /api/queue/{id}/status?status=PENDING -> Mengubah status agar pesanan masuk ke dapur.
         */
        composable(Screen.Cashier.route) {
            CashierView()
        }

        /**
         * Halaman Kitchen (Antrean Dapur)
         * - Integrasi Backend:
         * 1. GET /api/queue/active -> Mengambil pesanan dengan status selain UNPAID dan CANCELLED.
         * 2. PUT /api/queue/{id}/status?status=READY -> Menandai pesanan selesai dimasak.
         */
        composable(Screen.Kitchen.route) {
            KitchenView()
        }

        /**
         * Halaman Admin (Manajemen Produk)
         * - Peran: Pemilik toko mengelola katalog menu (tambah, edit, stok, foto).
         * - Integrasi Backend:
         * 1. GET /api/products    -> List semua produk (termasuk yang tidak aktif).
         * 2. POST /api/products   -> Menambah menu baru (dengan upload gambar Multipart).
         * 3. PUT /api/products/id -> Update data menu atau stok.
         * 4. DELETE /api/products/id -> Soft delete (mengubah isActive menjadi false).
         */
        composable(Screen.Admin.route) {
            AdminView()
        }
    }
}