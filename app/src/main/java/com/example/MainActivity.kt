package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.SmartCartRepository
import com.example.ui.SmartCartApp
import com.example.ui.theme.SmartCartAITheme
import com.example.viewmodel.SmartCartViewModel
import com.example.viewmodel.SmartCartViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = SmartCartRepository(database)
        val factory = SmartCartViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[SmartCartViewModel::class.java]

        setContent {
            SmartCartAITheme {
                SmartCartApp(viewModel = viewModel)
            }
        }
    }
}
