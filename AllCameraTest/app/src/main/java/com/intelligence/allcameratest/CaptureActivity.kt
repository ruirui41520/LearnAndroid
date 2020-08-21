package com.intelligence.allcameratest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.github.dfqin.grantor.PermissionListener
import com.github.dfqin.grantor.PermissionsUtil
import kotlinx.android.synthetic.main.activity_capture.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.File

class CaptureActivity:Activity() {

    val rootPath =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "CameraDemo"
    var capturePath: File? = null
    var cropPath: File? = null
    var mfaceOrigion = 90f

    companion object {
        val REQUEST_CAPTURE = 0
        val REQUEST_CROP = 1
        val REQUEST_GALLERY = 2
        val AUTHOR = "com.intelligence.allcameratest.fileProvider"
    }

    val job = Job()
    val coroutinesScope = CoroutineScope(job + Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)
        btn.setOnClickListener { requestCapture() }
        gallery.setOnClickListener { getImageFromAlbum() }
        test_co.setOnClickListener { testScope() }
        requestPermission()
    }

    fun testScope() {
        coroutinesScope.launch {
            withContext(Dispatchers.IO) {
                delay(2000)
                text.text = "Dispatchers.IO finish"
            }
        }
        text.text = "start"
    }

    fun showImage(imagePath: File?) {
        coroutinesScope.launch {
            var bitmap: Bitmap? = null
            withContext(Dispatchers.IO) {
                imagePath?.let {
                    bitmap = compressBitmap(it, 1080, 200)
                }
            }
            bitmap.let {
                show_image.setImageBitmap(transferBitmap(imagePath, it!!))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE -> showImage(capturePath)
                REQUEST_GALLERY -> cropBitmap(data?.data)
                REQUEST_CROP -> showImage(cropPath)
            }
        }

    }

    fun requestPermission() {
        val listener = object : PermissionListener {
            override fun permissionDenied(permission: Array<out String>) {
                TODO("Not yet implemented")
            }

            override fun permissionGranted(permission: Array<out String>) {
                TODO("Not yet implemented")
            }
        }
        if (!PermissionsUtil.hasPermission(this, Manifest.permission.CAMERA)) {
            PermissionsUtil.requestPermission(this, listener, Manifest.permission.CAMERA)
        }
        if (!PermissionsUtil.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionsUtil.requestPermission(
                this,
                listener,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    fun requestCapture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createImageFile()?.let {
            capturePath = it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val uri = FileProvider.getUriForFile(this, AUTHOR, it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it))
            }
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            intent.resolveActivity(packageManager).let {
                startActivityForResult(intent, REQUEST_CAPTURE)
            }
        }
    }

    fun getImageFromAlbum() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    fun createImageFile(isCrop: Boolean = false): File? {
        val file = File(rootPath + File.separator + "demo")
        if (!file.exists()) file.mkdirs()
        val timeStamp = System.currentTimeMillis()
        val imageName = if (isCrop) "img_crop$timeStamp.jpg" else "img$timeStamp.jpg"
        return File(file.absolutePath + File.separator + imageName)
    }

    fun compressBitmap(file: File, reqWidth: Int, reqHeight: Int): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val option = BitmapFactory.Options()
        option.inJustDecodeBounds = true
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        BitmapFactory.decodeByteArray(
            byteArrayOutputStream.toByteArray(),
            0,
            byteArrayOutputStream.size(),
            option
        )
        option.inJustDecodeBounds = false
        option.inSampleSize = getBestResample(bitmap, reqWidth, reqHeight)
        return BitmapFactory.decodeByteArray(
            byteArrayOutputStream.toByteArray(),
            0,
            byteArrayOutputStream.size(),
            option
        )
    }

    fun getBestResample(bitmap: Bitmap, width: Int, height: Int): Int {
        var sample = 1
        if (bitmap.width == width && bitmap.height == height) {
            return sample
        }
        while (bitmap.width / (width / 2) > sample && bitmap.height / (height / 2) > sample) {
            sample *= 2
        }
        return sample
    }

    fun transferBitmap(imagePath: File?, bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        imagePath?.let {
            if (it.equals(cropPath)) mfaceOrigion = BitmapUtil.getExifOrientation(it).toFloat() else {
                mfaceOrigion = 90f
            }
        }
        matrix.postRotate(mfaceOrigion)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
    }

    fun cropBitmap(source: Uri?) {
        //todo from album only
        createImageFile(true)?.let {
            cropPath = it
            val intent = Intent("com.android.camera.action.CROP")
            intent.putExtra("crop", true)
            intent.putExtra("outputX", 300)
            intent.putExtra("outputY", 300)
            intent.putExtra("aspectX", 1)
            intent.putExtra("aspectY", 1)
            intent.putExtra("scale", true)
            intent.putExtra("return-data", false)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.setDataAndType(source, "image/*")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it))
                startActivityForResult(intent, REQUEST_CROP)
            } else {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(it))
//                intent.setDataAndType(Uri.fromFile(capturePath),"image/*")
            }

        }

    }

}