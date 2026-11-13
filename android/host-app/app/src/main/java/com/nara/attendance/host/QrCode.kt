package com.nara.attendance.host

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

/** Session ID -> QR bitmap, drawn in the host UI for students to scan. */
object QrCode {
    fun bitmap(text: String, sizePx: Int = 640): Bitmap {
        val matrix = MultiFormatWriter().encode(
            text, BarcodeFormat.QR_CODE, sizePx, sizePx,
        )
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
}
