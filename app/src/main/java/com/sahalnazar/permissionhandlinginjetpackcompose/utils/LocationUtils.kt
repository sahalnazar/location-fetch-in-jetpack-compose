package com.sahalnazar.permissionhandlinginjetpackcompose.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

object LocationUtils {

    @SuppressLint("MissingPermission")
    fun Context.fetchLastLocation(
        fusedLocationClient: FusedLocationProviderClient,
        settingsLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>?,
        location: (Location) -> Unit,
        locationCallback: LocationCallback
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                location(it)
            } else {
                this.createLocationRequest(
                    settingsLauncher = settingsLauncher,
                    fusedLocationClient = fusedLocationClient,
                    locationCallback = locationCallback
                )
            }
        }
    }


    @SuppressLint("LongLogTag")
    fun Context.fetchLocation(
        forceFetch: Boolean = false,
        settingsLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        fusedLocationClient: FusedLocationProviderClient,
        requestPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>,
        locationCallback: LocationCallback,
        location: (Location) -> Unit,
        openPermissionRationaleDialog: (ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>) -> Unit
    ) {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {

                if (forceFetch) {
                    this.createLocationRequest(
                        settingsLauncher = settingsLauncher,
                        fusedLocationClient = fusedLocationClient,
                        locationCallback = locationCallback
                    )
                } else {
                    fetchLastLocation(
                        fusedLocationClient,
                        settingsLauncher = settingsLauncher,
                        location = {
                            location(it)
                            Log.d(
                                "LandingScreenContent.fetchLocation",
                                "fetchLastLocation: ${it.latitude}"
                            )
                        },
                        locationCallback
                    )
                }

            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> openPermissionRationaleDialog(requestPermissionLauncher)


            else -> requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission", "LongLogTag")
    private fun Context.createLocationRequest(
        settingsLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>?,
        fusedLocationClient: FusedLocationProviderClient,
        locationCallback: LocationCallback
    ) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1 * 1000
            isWaitForAccurateLocation = true

        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        }

        task.addOnFailureListener { exception ->
            Log.e("LocationUtil.createLocationRequest", exception.toString())
            if (exception is ResolvableApiException) {
                try {
                    settingsLauncher?.launch(
                        IntentSenderRequest.Builder(exception.resolution).build()
                    )
                } catch (e: Exception) {
                    // Ignore the error.
                }
            }
        }
    }

}