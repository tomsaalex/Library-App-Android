package com.example.library_app_android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.library_app_android.core.TAG
import com.example.library_app_android.ui.theme.MyAppTheme
import androidx.compose.material3.Surface
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Log.d(TAG, "onCreate")
            LibraryApp {
                LibraryAppNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            (application as MyApplication).container.bookRepository.openWsClient()
        }
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            (application as MyApplication).container.bookRepository.closeWsClient()
        }
    }

    @Composable
    fun LibraryApp(content: @Composable () -> Unit) {
        Log.d("LibraryApp", "recompose")
        MyAppTheme {
            Surface {
                content()
            }
        }
    }

    @Preview
    @Composable
    fun PreviewLibraryApp() {
        LibraryApp {
            LibraryAppNavHost()
        }
    }

}
