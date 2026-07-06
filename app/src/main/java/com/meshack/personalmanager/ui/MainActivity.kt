package com.meshack.personalmanager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.meshack.personalmanager.ui.theme.PersonalManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalManagerTheme {
                PersonalManagerNavHost()
            }
        }
    }
}
