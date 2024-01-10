package com.example.library_app_android.map

import android.app.Application
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MyLocation() {
    val myLocationViewModel = viewModel<MyLocationViewModel>(
        factory = MyLocationViewModel.Factory(
            LocalContext.current.applicationContext as Application
        )
    )

    val location = myLocationViewModel.uiState
    /*if (location != null) {
        //MyMap(location.latitude, location.longitude)
    } else {
        LinearProgressIndicator()
    }*/
}