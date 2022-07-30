package com.sahalnazar.permissionhandlinginjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sahalnazar.permissionhandlinginjetpackcompose.ui.theme.PermissionHandlingInJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionHandlingInJetpackComposeTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(text = "Has permission: ")
                        Text(text = "Current location: ")
                        Button(onClick = {

                        }) {
                            Text(text = "Fetch location")
                        }
                        if (false) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}