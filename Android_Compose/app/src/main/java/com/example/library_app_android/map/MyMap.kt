package com.example.library_app_android.map

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

val TAG = "MyMap"

@Composable
fun MyMap(lat: Double, lng: Double, onLocationChanged: (Double, Double) -> Unit) {
    Log.d("MyMap", "lat = $lat, long = $lng")

    val bookPosition: LatLng = LatLng(lat, lng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bookPosition, 5f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = {
            Log.d(TAG, "onMapClick $it")
        },
        onMapLongClick = {
            Log.d(TAG, "onMapLongClick $it")

            onLocationChanged(it.latitude, it.longitude)
        }
    ) {
        Marker(
            state = MarkerState(position = bookPosition),
            title = "User location title",
            snippet = "User location"
        )
    }
}