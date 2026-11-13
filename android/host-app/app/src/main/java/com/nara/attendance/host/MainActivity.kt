package com.nara.attendance.host

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.UUID

/**
 * Host screen: Start session -> QR with session ID + rotating BLE token.
 * The token display is for debugging only — students never type it in;
 * their app reads it from the BLE scan.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var advertiser: BleAdvertiser
    private var sessionSecret: String? = null
    private var sessionId: String? = null
    private var lastWindow: Long = -1

    private val tickHandler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            refreshTokenDisplay()
            tickHandler.postDelayed(this, 1_000)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.all { it }) startSession() else {
            Toast.makeText(
                this,
                "Bluetooth + location permissions are needed for BLE advertising.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        advertiser = BleAdvertiser(bluetoothAdapter)

        findViewById<Button>(R.id.btnStart).setOnClickListener { requestPermissionsAndStart() }
        findViewById<Button>(R.id.btnStop).setOnClickListener { stopSession() }
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    private fun requestPermissionsAndStart() {
        val missing = requiredPermissions().filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) startSession()
        else permissionLauncher.launch(missing.toTypedArray())
    }

    private fun startSession() {
        if (!advertiser.canAdvertise()) {
            Toast.makeText(
                this,
                "This phone can't advertise BLE (check Bluetooth is on).",
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        sessionSecret = UUID.randomUUID().toString()
        sessionId = "ATT-" + UUID.randomUUID().toString().take(8).uppercase()
        lastWindow = -1

        findViewById<TextView>(R.id.txtSessionId).text = "Session: $sessionId"
        findViewById<ImageView>(R.id.imgQr)
            .setImageBitmap(QrCode.bitmap(sessionId!!))
        findViewById<Button>(R.id.btnStart).isEnabled = false
        findViewById<Button>(R.id.btnStop).isEnabled = true

        tickHandler.post(ticker)
        Toast.makeText(this, "Session started — beacon is live.", Toast.LENGTH_SHORT).show()
    }

    /** Re-advertise when the 30 s window rolls over; update countdown each second. */
    private fun refreshTokenDisplay() {
        val secret = sessionSecret ?: return
        val window = TokenRotator.windowNumber()
        if (window != lastWindow) {
            lastWindow = window
            val token = TokenRotator.currentToken(secret)
            advertiser.start(token) { err ->
                Toast.makeText(this, err, Toast.LENGTH_LONG).show()
            }
            findViewById<TextView>(R.id.txtToken).text = "Token: $token"
        }
        val secs = TokenRotator.millisUntilRotation() / 1_000
        findViewById<TextView>(R.id.txtCountdown).text =
            "Next rotation in ${secs}s · ${if (advertiser.isAdvertising()) "advertising" else "not advertising"}"
    }

    private fun stopSession() {
        tickHandler.removeCallbacks(ticker)
        advertiser.stop()
        sessionSecret = null
        sessionId = null
        findViewById<TextView>(R.id.txtSessionId).text = "No active session"
        findViewById<TextView>(R.id.txtToken).text = ""
        findViewById<TextView>(R.id.txtCountdown).text = ""
        findViewById<ImageView>(R.id.imgQr).setImageDrawable(null)
        findViewById<Button>(R.id.btnStart).isEnabled = true
        findViewById<Button>(R.id.btnStop).isEnabled = false
    }

    override fun onDestroy() {
        tickHandler.removeCallbacks(ticker)
        advertiser.stop()
        super.onDestroy()
    }
}
