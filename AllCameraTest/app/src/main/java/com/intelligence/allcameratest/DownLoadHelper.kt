package com.intelligence.allcameratest

import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class DownLoadHelper {

    private val listeners = ArrayList<FileDownloaderListener>()

    fun addListener(listener: FileDownloaderListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    companion object {
        private val downloadFiles = HashMap<Long?, String?>()
        fun downLoadFile(context: Context, fileName: File, targetUri: Uri): Long {
            val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            return try {
                var downloadId = getDownloadIdIfEnqueued(downloadManager, targetUri)
                val waitingFileName = downloadFiles[downloadId]
                if (downloadId >= 0 && waitingFileName != null && fileName.path.compareTo(waitingFileName) == 0) return downloadId
                val request = DownloadManager.Request(targetUri)
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                request.setDestinationUri(Uri.fromFile(fileName))
                request.setNotificationVisibility(VISIBILITY_VISIBLE)
                request.setVisibleInDownloadsUi(true)
                downloadId = downloadManager.enqueue(request)
                downloadFiles.put(downloadId, fileName.path)
                downloadId
            } catch (e: Exception) {
                e.printStackTrace()
                -1L
            }

        }

        fun getDownloadIdIfEnqueued(downloadManager: DownloadManager, targetUri: Uri): Long {
            var cursor: Cursor? = null
            var ret = -1L
            try {
                val query = DownloadManager.Query()
                query.setFilterByStatus(DownloadManager.STATUS_RUNNING or DownloadManager.STATUS_PENDING or DownloadManager.STATUS_PAUSED)
                cursor = downloadManager.query(query)
                val downloadUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_URI)
                val downLoadIdIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                if (cursor.moveToFirst()) {
                    do {
                        val downloadUri = cursor.getString(downloadUriIndex)
                        val downloadId = cursor.getLong(downLoadIdIndex)
                        if (downloadUri.equals(targetUri)) {
                            ret = downloadId
                        }
                    } while (cursor.moveToNext())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return ret
        }

        fun removeDownloadItem(context: Context, downloadId: Long) {
            val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            try {
                downloadManager.remove(downloadId)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }

        fun removeAllDownloadCompletedItems(context: Context) {
            val downLoadCompleteIds = ArrayList<Long>()
            var cursor: Cursor? = null
            try {
                val downloadManager =
                    context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query()
                query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL or DownloadManager.STATUS_FAILED)
                cursor = downloadManager.query(query)
                val downloadIdIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                if (cursor.moveToFirst()) {
                    do {
                        val downloadId = cursor.getLong(downloadIdIndex)
                        downLoadCompleteIds.add(downloadId)
                    } while (cursor.moveToNext())
                }
                val array = LongArray(downLoadCompleteIds.size)
                downLoadCompleteIds.asSequence().forEach {
                    downloadManager.remove(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }

        fun getDownloadedFile(downloadManager: DownloadManager, downLoadId: Long): File? {
            var fileUri: String? = null
            try {
                val query = DownloadManager.Query()
                query.setFilterById(downLoadId)
                val cursor = downloadManager.query(query)
                val localFileUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                if (cursor.moveToFirst()) {
                    fileUri = cursor.getString(localFileUriIndex)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return if (fileUri != null) {
                File(Uri.parse(fileUri).path)
            } else {
                null
            }
        }

    }

    fun notifyComplete(downloadId: Long, file: File) {
        downloadFiles.remove(downloadId)
        listeners.forEach {
            it.onFileDownloadComplete(downloadId,file)
        }
    }

    fun notifyFailed(downloadId: Long) {
        downloadFiles.remove(downloadId)
        listeners.forEach {
            it.onFileDownloadFailed(downloadId)
        }
    }

    interface FileDownloaderListener {
        fun onFileDownloadComplete(downloadId: Long, file: File?)
        fun onFileDownloadFailed(downloadId: Long)
    }

}