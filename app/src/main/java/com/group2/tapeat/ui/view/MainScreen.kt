package com.group2.tapeat.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.group2.tapeat.ui.route.AppRoutes
import com.group2.tapeat.ui.route.Screen
import com.group2.tapeat.ui.theme.TapeatTheme

/**
 * Kerangka utama UI aplikasi.
 * Di sini kita menggabungkan BottomBar (menu bawah) dengan NavHost (wadah konten halaman).
 */
@Composable
fun MainScreen() {
    // navController untuk mengatur perpindahan antar halaman
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { innerPadding ->
        // NavHost (Router)
        // Konten di dalamnya akan berganti-ganti tergantung dari rute yang aktif
        AppRoutes(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Komponen UI untuk menu navigasi di bawah layar.
 */
@Composable
fun BottomBar(navController: NavHostController) {
    // List menu yang akan ditampilkan di Bottom Bar
    val screens = listOf(
        Screen.Customer,
        Screen.Cashier,
        Screen.Kitchen,
        Screen.Admin
    )
    // Memantau rute mana yang sedang aktif saat ini.
    // Ini digunakan untuk memberi highlight pada menu yang sedang aktif / dipilih.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        // Looping untuk membuat setiap tombol menu
        screens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(screen.icon, contentDescription = screen.title)
                },
                label = {
                    Text(text = screen.title)
                },
                selected = currentRoute == screen.route, // Menu aktif jika rute cocok
                onClick = {
                    // Logic ketika tombol menu diklik
                    navController.navigate(screen.route) {
                        // popUpTo: Menghindari penumpukan riwayat halaman (back stack).
                        // Setiap klik menu baru, riwayat akan dibersihkan sampai ke start destination.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // launchSingleTop: Mencegah layar yang sama dibuka berkali-kali
                        // jika tombol diklik dengan cepat berturut-turut.
                        launchSingleTop = true
                        // restoreState: Menyimpan state layar sebelumnya.
                        // Misal: Jika di tab Customer sudah scroll ke bawah,
                        // saat kembali dari tab Admin, posisi scroll tetap dipertahankan.
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Preview MainScreen
 */
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TapeatTheme {
        MainScreen()
    }
}
