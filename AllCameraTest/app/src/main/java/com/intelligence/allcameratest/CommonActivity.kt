package com.intelligence.allcameratest

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_common.*
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CommonActivity : Activity(), DownLoadHelper.FileDownloaderListener {
    val job = Job()
    val mainScope = CoroutineScope(Dispatchers.Main + job)
    val imageUrls = arrayOf(
        "https://www.niwoxuexi.com/statics/images/nougat_bg.png"
    )
    var data: DownloadData? = null
    val executor = Executors.newSingleThreadExecutor() as ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common)
        // get uri from server
        data = DownloadData(imageUrls[0])
        CameraTestApplication.getFileDownloader()?.addListener(this)
        downLoadImage()

    }

    fun downLoadImage() {
        DownLoadHelper.removeAllDownloadCompletedItems(baseContext)
        executor.execute {
            (0 until imageUrls.size).asSequence().forEach {
                val index = it
                FileHelper.createImageFile()?.let {
                    DownLoadHelper.downLoadFile(
                        baseContext,
                        it,
                        Uri.parse(data!!.resourceId)
                    )
                }
            }

        }
    }

    override fun onFileDownloadComplete(downloadId: Long, file: File?) {
        file?.let {
            mainScope.launch {
                var bitmap: Bitmap? = null
                withContext(Dispatchers.IO) {
                    try {
                        val fileOut = FileHelper.createImageFile() ?: return@withContext
                        if (fileOut.exists())fileOut.delete()
                        if (!file.renameTo(fileOut)){
                            // onFileDownloadComplete renameTo failed!!!!
                        }
                        data?.resourceId = fileOut.path
                        bitmap = BitmapFactory.decodeFile(data?.resourceId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                image_view.setImageBitmap(bitmap)
            }
        }

    }

    override fun onFileDownloadFailed(downloadId: Long) {
        // todo add failed solutions
    }

}
