package com.group2.tapeat.ui.view

import com.group2.tapeat.ui.theme.TapeatTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CustomerView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Customer Ordering Screen")
    }
}

@Preview(showBackground = true)
@Composable
fun CustomerPreview() {
    TapeatTheme {
        CustomerView()
    }
}
