package com.nara.attendance.host

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

/**
 * One advertiser, many scanners: the host phone is the only device
 * advertising, so 50–70 student phones can listen passively with no
 * BLE congestion. Total payload stays under the 31-byte ad limit:
 * flags (3) + 128-bit service UUID (18) + 8 bytes of token (10) = 31.
 */
class BleAdvertiser(private val adapter: BluetoothAdapter?) {

    companion object {
        // Fixed service UUID for this app — students filter scans on it.
        val SERVICE_UUID: UUID =
            UUID.fromString("b5a3f2c1-4d6e-4a8b-9c0d-1e2f3a4b5c6d")
        private const val TAG = "BleAdvertiser"
    }

    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertising = false

    fun canAdvertise(): Boolean =
        adapter != null && adapter.isEnabled &&
            adapter.isMultipleAdvertisementSupported

    /** (Re)start advertising with this token. Call again when it rotates. */
    fun start(token: String, onError: (String) -> Unit) {
        advertiser = adapter?.bluetoothLeAdvertiser
        if (advertiser == null) {
            onError("This phone can't advertise BLE (no advertiser).")
            return
        }
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(
                ParcelUuid(SERVICE_UUID),
                token.toByteArray(Charsets.UTF_8),
            )
            .build()
        // Re-advertising replaces the old payload; stop first for a clean swap.
        if (advertising) advertiser?.stopAdvertising(callback)
        advertiser?.startAdvertising(settings, data, callback)
    }

    fun stop() {
        if (advertising) advertiser?.stopAdvertising(callback)
        advertising = false
    }

    fun isAdvertising(): Boolean = advertising

    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            advertising = true
            Log.d(TAG, "advertising started")
        }

        override fun onStartFailure(errorCode: Int) {
            advertising = false
            Log.e(TAG, "advertising failed: $errorCode")
        }
    }
}
