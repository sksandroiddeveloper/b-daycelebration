package com.birthday.celebrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.birthday.celebrate.data.BirthdayViewModel
import com.birthday.celebrate.ui.BirthdayNavGraph
import com.birthday.celebrate.ui.theme.BirthdayTheme

class MainActivity : ComponentActivity() {

    // ViewModel survives config changes; MusicController lives inside it
    private val viewModel: BirthdayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BirthdayTheme {
                BirthdayNavGraph(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to MusicService so we can control it
        viewModel.musicController.bind()
    }

    override fun onStop() {
        super.onStop()
        // Unbind but don't stop — music keeps playing via foreground service
        viewModel.musicController.unbind()
    }
}
