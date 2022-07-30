package com.sahalnazar.permissionhandlinginjetpackcompose

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class MainScreenUiState(
    val locationFromGps: Location? = null
)

class MainViewModel : ViewModel() {

    var uiState by mutableStateOf(MainScreenUiState())
        private set

    fun setLocationFromGps(location: Location?) {
        if (uiState.locationFromGps == null && uiState.locationFromGps != location) {
            uiState = uiState.copy(locationFromGps = location)
        }
    }

}