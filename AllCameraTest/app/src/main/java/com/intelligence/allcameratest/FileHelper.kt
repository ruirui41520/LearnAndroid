package com.intelligence.allcameratest

import android.content.Context
import android.os.Environment
import java.io.File
import java.lang.Exception

object FileHelper {
    val rootPath =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "CameraDemo"

    fun createImageFile(): File? {
        try {
            val file = File(rootPath + "image")
            if (!file.exists()) file.mkdirs()
            val imageTimeStamp = System.currentTimeMillis()
            val fileName = "img_$imageTimeStamp.jpg"
            return File(file.absolutePath + File.separator + fileName)
        } catch (e: Exception) {
            return null
        }
    }

    fun createCameraFile(): File? {
        var finalFile:File?= null
        try {
            val file = File(rootPath + "camera")
            if (!file.exists()) file.mkdirs()
            val imageTimeStamp = System.currentTimeMillis()
            val fileName = "camera_$imageTimeStamp.jpg"
            finalFile = File(file.absolutePath + File.separator + fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return finalFile
    }

    fun createVideoFile(): File? {
        var finalFile:File?= null
        try {
            val file = File(rootPath + "video")
            if (!file.exists()) file.mkdirs()
            val imageTimeStamp = System.currentTimeMillis()
            val fileName = "video_$imageTimeStamp.jpg"
            finalFile = File(file.absolutePath + File.separator + fileName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return finalFile
    }
}