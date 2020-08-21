package com.intelligence.allcameratest

import android.app.Application

class CameraTestApplication:Application() {
    private var fileDownloader: DownLoadHelper? = null


    companion object{
        private var application: CameraTestApplication? = null

        fun getInstance(): CameraTestApplication? {
            return application ?: throw RuntimeException("Application is not attached.")
        }

        fun getFileDownloader(): DownLoadHelper? {
            return getInstance()?.fileDownloader
        }
    }

    override fun onCreate() {
        super.onCreate()
        application= this
        fileDownloader = DownLoadHelper()
    }
}