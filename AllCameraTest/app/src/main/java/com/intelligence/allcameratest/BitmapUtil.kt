package com.intelligence.allcameratest

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream


object BitmapUtil {
    fun mirrorImage(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postScale(-1f, 1f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    fun rotateImage(degree: Int, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    fun saveBitmap(context:Context, byteArray: ByteArray) :Bitmap?{
        val file = FileHelper.createImageFile()
        try {
            val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size,null)
            val outputStream = FileOutputStream(file)
                // todo facing : Cameracharacteristics.back
            val finalBitmap = rotateImage(90,bitmap)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            outputStream.flush()
            outputStream.close()
//            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
            return finalBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getExifOrientation(file: File): Int {
        var degree = 0
        var exifInterface: ExifInterface? = null
        try {
            exifInterface = ExifInterface(file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        exifInterface?.let {
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            if (orientation != -1) {
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
                }
            }
        }
        return degree
    }
}