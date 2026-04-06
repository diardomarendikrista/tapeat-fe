package com.group2.tapeat.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.group2.tapeat.ui.theme.TapeatTheme

@Composable
fun KitchenView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Kitchen Display System Screen mas yohanes")
    }
}

@Preview(showBackground = true)
@Composable
fun KitchenPreview() {
    TapeatTheme {
        KitchenView()
    }
}