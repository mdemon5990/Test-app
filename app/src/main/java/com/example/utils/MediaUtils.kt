package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

object MediaUtils {
    fun encodeImageUriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) return null

            val maxDim = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = width.toFloat() / height.toFloat()
            val (newWidth, newHeight) = if (width > height && width > maxDim) {
                maxDim to (maxDim / ratio).toInt()
            } else if (height > width && height > maxDim) {
                (maxDim * ratio).toInt() to maxDim
            } else {
                width to height
            }

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun decodeBase64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun readTextFromUri(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }
            if (content.contains("\u0000")) {
                 "Binary file attached. Text representation not available."
            } else {
                content
            }
        } catch (e: Exception) {
            "Failed to read file: ${e.message}"
        }
    }
}
