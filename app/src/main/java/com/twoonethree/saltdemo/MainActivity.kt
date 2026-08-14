package com.twoonethree.saltdemo

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.twoonethree.saltdemo.navigation.NavigationSetup
import com.twoonethree.saltdemo.ui.theme.SaltDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(scrim = Color.BLACK)
        )
        setContent {
            SaltDemoTheme {
                NavigationSetup()
            }
        }
    }
}