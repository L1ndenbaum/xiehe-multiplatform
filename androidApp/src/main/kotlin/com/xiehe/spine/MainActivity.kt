package com.xiehe.spine

import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.xiehe.spine.core.model.AppResult
import kotlinx.coroutines.launch

private const val networkTag = "SpineNetwork"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = createAndroidAppContainer(
            context = applicationContext,
            baseUrl = BuildConfig.BASE_URL,
            enableNetworkDiagnostics = BuildConfig.DEBUG,
        )
        if (BuildConfig.DEBUG) {
            lifecycleScope.launch {
                val connectivityManager = getSystemService(ConnectivityManager::class.java)
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                val linkProperties = connectivityManager.getLinkProperties(network)
                Log.i(
                    networkTag,
                    "activeNetwork=$network capabilities=${capabilities} " +
                        "vpn=${capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_VPN)} " +
                        "proxy=${linkProperties?.httpProxy}",
                )
                when (val health = container.authRepository.healthCheck()) {
                    is AppResult.Success -> {
                        Log.i(
                            networkTag,
                            "startup health ok status=${health.data.status} version=${health.data.version}",
                        )
                    }

                    is AppResult.Failure -> {
                        Log.w(
                            networkTag,
                            "startup health failed message=${health.message} details=${health.debugDetails}",
                        )
                    }
                }
            }
        }

        setContent {
            App(
                container = container,
                showNetworkDiagnostics = BuildConfig.DEBUG,
            )
        }
    }
}
