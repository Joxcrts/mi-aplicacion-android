package com.joxcrts.mistareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joxcrts.mistareas.ui.TareasScreen
import com.joxcrts.mistareas.ui.TareasViewModel
import com.joxcrts.mistareas.ui.theme.MisTareasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MisTareasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: TareasViewModel = viewModel(factory = TareasViewModel.Factory)
                    TareasScreen(viewModel = viewModel)
                }
            }
        }
    }
}
