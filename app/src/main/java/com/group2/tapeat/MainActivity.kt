package com.group2.tapeat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.group2.tapeat.ui.theme.TapeatTheme
import com.group2.tapeat.ui.view.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent digunakan untuk mulai menggambar UI menggunakan Jetpack Compose.
        setContent {
            // panggil tema bawaan aplikasi (TapeatTheme) dan memuat MainScreen.
            TapeatTheme {
                MainScreen()
            }
        }
    }
}
