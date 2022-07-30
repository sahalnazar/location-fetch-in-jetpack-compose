package com.sahalnazar.permissionhandlinginjetpackcompose

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.sahalnazar.permissionhandlinginjetpackcompose.ui.theme.PermissionHandlingInJetpackComposeTheme
import com.sahalnazar.permissionhandlinginjetpackcompose.utils.LocationUtils.fetchLastLocation
import com.sahalnazar.permissionhandlinginjetpackcompose.utils.LocationUtils.fetchLocation
import androidx.core.content.ContextCompat.checkSelfPermission
import com.sahalnazar.permissionhandlinginjetpackcompose.utils.LocationUtils
import com.sahalnazar.permissionhandlinginjetpackcompose.utils.LocationUtils.hasPermission

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PermissionHandlingInJetpackComposeTheme {

                var locationFromGps: Location? by remember { mutableStateOf(null) }
                var openDialog: Boolean by remember { mutableStateOf(false) }


                val context = LocalContext.current
                val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(context) }
                val locationCallback = remember {
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            Log.d("onLocationResult", "locationResult.latitude: ${locationResult.lastLocation?.latitude}")
                            if (locationFromGps == null && locationFromGps != locationResult.lastLocation) {
                                locationFromGps = locationResult.lastLocation
                            }
                        }
                    }
                }

                val hasPermission = context.hasPermission

                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = {
                        when (it.resultCode) {
                            Activity.RESULT_OK -> {
                                context.fetchLastLocation(
                                    fusedLocationClient = fusedLocationProviderClient,
                                    settingsLauncher = null,
                                    location = {
                                        Log.d("settingsLauncher", "location: ${it.latitude}")
                                        if (locationFromGps == null && locationFromGps != it) {
                                            locationFromGps = it
                                        }
                                    },
                                    locationCallback = locationCallback
                                )
                            }
                            Activity.RESULT_CANCELED -> {
                                Toast.makeText(context, "Activity.RESULT_CANCELED", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )

                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = {
                        context.fetchLastLocation(
                            fusedLocationClient = fusedLocationProviderClient,
                            settingsLauncher = settingsLauncher,
                            location = {
                                Log.d("settingsLauncher", "location: ${it.latitude}")
                                if (locationFromGps == null && locationFromGps != it) {
                                    locationFromGps = it
                                }
                            },
                            locationCallback = locationCallback
                        )
                    }
                )

                LaunchedEffect(
                    key1 = locationFromGps,
                    block = {
                        Log.d("LaunchedEffect", "locationFromGps: $locationFromGps")
                        // TODO: setup GeoCoder

                    }
                )

                LaunchedEffect(key1 = true,
                    block = {
                        context.fetchLocation(
                            forceFetch = true,
                            settingsLauncher = settingsLauncher,
                            fusedLocationClient = fusedLocationProviderClient,
                            requestPermissionLauncher = requestPermissionLauncher,
                            locationCallback = locationCallback,
                            location = {
                                Log.d("LaunchedEffect", "fetchLocation.locationFromGps: $locationFromGps")
                                if (locationFromGps == null && locationFromGps != it) {
                                    locationFromGps = it
                                }
                            },
                            openPermissionRationaleDialog = {
                                openDialog = true
                            }
                        )
                    }
                )

                DisposableEffect(
                    key1 = true
                ) {
                    onDispose {
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(text = "Has permission: $hasPermission")
                        Text(text = "Current location: ${locationFromGps?.latitude}")
                        Button(onClick = {

                        }) {
                            Text(text = "Fetch location")
                        }
                        if (false) {
                            CircularProgressIndicator()
                        }
                    }
                }


                AnimatedVisibility(visible = openDialog) {
                    Dialog(onDismissRequest = { openDialog = false }) {
                        NoPermissionDialog(
                            closeDialog = {
                                openDialog = false
                            },
                            reqPermission = {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                    )
                                )
                                openDialog = false
                            }
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun NoPermissionDialog(closeDialog: () -> Unit, reqPermission: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp),
        elevation = 8.dp
    ) {
        Column(
            Modifier.padding(16.dp)
        ) {
            Text(text = "App required location. Grant permission.")
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.align(Alignment.End)) {
                Button(onClick = {
                    closeDialog()
                }) {
                    Text(text = "Deny")
                }

                Button(onClick = {
                    reqPermission()
                }) {
                    Text(text = "Allow")
                }

            }
        }
    }
}

