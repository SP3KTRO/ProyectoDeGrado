package com.tupausa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tupausa.ui.theme.TuPausaTheme
import com.tupausa.viewModel.TuPausaViewModel
import com.tupausa.viewModel.appNavigation.AppNavigation
import com.tupausa.viewmodel.TuPausaViewModelFactory

class MainActivity : ComponentActivity() {

    private val tuPausaViewModel: TuPausaViewModel by viewModels {
        TuPausaViewModelFactory((application as TuPausaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TuPausaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = tuPausaViewModel)
                }
            }
        }
    }
}