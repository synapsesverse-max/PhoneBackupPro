package com.phonebackup.transfer

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDiscovery @Inject constructor(private val context: Context) {
    fun discoverDevices(): Flow<DiscoveredDevice> = callbackFlow {
        val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        val channel = manager?.initialize(context, context.mainLooper, null)
        if (manager == null || channel == null) {
            close(IllegalStateException("Wi-Fi Direct is unavailable on this device"))
            return@callbackFlow
        }
        val listener = object : WifiP2pManager.PeerListListener {
            override fun onPeersAvailable(peers: android.net.wifi.p2p.WifiP2pDeviceList) {
                peers.deviceList.forEach { device ->
                    trySend(DiscoveredDevice(device.deviceName, device.deviceAddress, "android", "wifi-direct"))
                }
            }
        }
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { manager.requestPeers(channel, listener) }
            override fun onFailure(reason: Int) { close(IllegalStateException("Wi-Fi Direct discovery failed: $reason")) }
        })
        awaitClose { manager.stopPeerDiscovery(channel, null) }
    }
}
