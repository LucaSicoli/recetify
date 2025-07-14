package com.example.recetify.ui.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

enum class ConnectionType {
    NONE,
    WIFI,
    CELLULAR,
    OTHER
}

data class ConnectionState(
    val isConnected: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.NONE
)

/**
 * Hook Compose que expone isOnline = true/false según el estado de la red.
 */
@Composable
fun rememberIsOnline(): State<Boolean> {
    val connectionState = rememberConnectionState()
    return remember { derivedStateOf { connectionState.value.isConnected } }
}

/**
 * Hook Compose que expone el estado completo de la conexión (tipo y estado)
 */
@Composable
fun rememberConnectionState(): State<ConnectionState> {
    val context = LocalContext.current
    val cm = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val connectionState = remember { mutableStateOf(ConnectionState()) }

    DisposableEffect(cm) {
        fun updateConnectionState() {
            val activeNetwork = cm.activeNetwork
            if (activeNetwork == null) {
                connectionState.value = ConnectionState(false, ConnectionType.NONE)
                return
            }

            val capabilities = cm.getNetworkCapabilities(activeNetwork)
            if (capabilities == null) {
                connectionState.value = ConnectionState(false, ConnectionType.NONE)
                return
            }

            val isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            val type = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                else -> ConnectionType.OTHER
            }

            connectionState.value = ConnectionState(isConnected, type)
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateConnectionState()
            }

            override fun onLost(network: Network) {
                updateConnectionState()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                updateConnectionState()
            }
        }

        // Estado inicial
        updateConnectionState()

        cm.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
        onDispose {
            cm.unregisterNetworkCallback(callback)
        }
    }
    return connectionState
}