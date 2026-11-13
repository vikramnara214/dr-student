package com.nara.attendance.host

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Rotating token, TOTP-style: token = first 4 bytes of
 * HMAC-SHA256(secret, 30-second window), rendered as 8 hex chars.
 * Host and server share the secret, so rotation needs no round-trip.
 * The server accepts the current window and the previous one (clock skew).
 */
object TokenRotator {
    const val WINDOW_MS = 30_000L

    fun windowNumber(nowMs: Long = System.currentTimeMillis()): Long =
        nowMs / WINDOW_MS

    /** Milliseconds until the current window ends — for the countdown. */
    fun millisUntilRotation(nowMs: Long = System.currentTimeMillis()): Long =
        WINDOW_MS - (nowMs % WINDOW_MS)

    fun tokenFor(secret: String, window: Long): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val hash = mac.doFinal(window.toString().toByteArray(Charsets.UTF_8))
        // Mask with 0xFF: without it, bytes >= 0x80 sign-extend in %x and the
        // token grows past 8 chars, which can overflow the 31-byte ad packet.
        return hash.take(4).joinToString("") { "%02x".format(it.toInt() and 0xFF) }
    }

    fun currentToken(secret: String): String =
        tokenFor(secret, windowNumber())
}
