package com.intelligence.allcameratest

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DownloadBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        intent.let {
            val action = it.action
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                val file = DownLoadHelper.getDownloadedFile(downloadManager, downloadId)
                if (file != null && file.exists()) {
                    DownLoadHelper.removeDownloadItem(context, downloadId)
                    CameraTestApplication.getFileDownloader()?.notifyComplete(downloadId, file)
                } else {
                    DownLoadHelper.removeDownloadItem(context, downloadId)
                    CameraTestApplication.getFileDownloader()?.notifyFailed(downloadId)
                }
            }
        }
    }
}