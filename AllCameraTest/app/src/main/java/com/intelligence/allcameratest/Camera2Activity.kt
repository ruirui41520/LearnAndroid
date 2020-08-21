package com.intelligence.allcameratest

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_camera2.*
import kotlinx.coroutines.*
import java.util.*
import java.util.Collections.min
import kotlin.collections.ArrayList

class Camera2Activity : Activity() {
    lateinit var mCameraManager: CameraManager
    var mCameraFacing = CameraCharacteristics.LENS_FACING_BACK
    var mCameraId: String? = null
    var mImageReader: ImageReader? = null
    var mCameraCharacteristics: CameraCharacteristics? = null
    var mCameraSensorOrientation = 0
    var displayOrientation = 0 // 手机方向
    var mPreviewSize = Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)
    var mPicSize = Size(PIC_WIDTH, PIC_HEIGHT)
    lateinit var mCameraHandler: Handler
    val mCameraThread = HandlerThread("CameraThread")
    var mCameraDevice: CameraDevice? = null
    var mCameraCaptureSession: CameraCaptureSession? = null
    var mCanTakePic = true
    var mCanExchangeCamera = false
    val mJob = Job()
    val mCoroutineScope = CoroutineScope(Dispatchers.Main + mJob)

    companion object {
        const val PREVIEW_WIDTH = 720
        const val PREVIEW_HEIGHT = 1080
        const val PIC_WIDTH = 720
        const val PIC_HEIGHT = 1080
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        capture_pic.setOnClickListener { tickPicture() }
        exchange_camera.setOnClickListener { exchangeCamera() }
        displayOrientation = this.windowManager.defaultDisplay.orientation
        mCameraThread.start()
        mCameraHandler = Handler(mCameraThread.looper)
        texture_view.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?, width: Int, height: Int
            ) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
                releaseCamera()
                return true
            }

            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                initCameraInfo()
            }

        }
    }

    fun releaseCamera() {
        mCameraCaptureSession?.close()
        mCameraCaptureSession = null
        mImageReader?.close()
        mImageReader = null
        mCameraDevice?.close()
        mCameraDevice = null
        mCanExchangeCamera = false
    }

    fun initCameraInfo() {
        mCameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = mCameraManager.cameraIdList
        if (cameraIds.isEmpty()) return
        for (id in cameraIds) {
            val cameraCharacteristics = mCameraManager.getCameraCharacteristics(id)
            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == mCameraFacing) {
                mCameraId = id
                mCameraCharacteristics = cameraCharacteristics
            }
        }
        mCameraCharacteristics?.let {
            mCameraSensorOrientation = it.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            val sizeMap = it.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            sizeMap?.let {
                val needExchangeOrientation =
                    exchangeWidthAndHeight(mCameraSensorOrientation, displayOrientation)
                val supportPreViewSize = it.getOutputSizes(SurfaceTexture::class.java)
                val supportPicSize = it.getOutputSizes(ImageFormat.JPEG)
                mPreviewSize = getBestSizeAccordingCamera(
                    if (needExchangeOrientation) mPreviewSize.height else mPreviewSize.width,
                    if (needExchangeOrientation) mPreviewSize.width else mPreviewSize.height,
                    if (needExchangeOrientation) texture_view.height else texture_view.width,
                    if (needExchangeOrientation) texture_view.width else texture_view.height,
                    supportPreViewSize.toList()
                )
                mPicSize = getBestSizeAccordingCamera(
                    if (needExchangeOrientation) mPicSize.height else mPicSize.width,
                    if (needExchangeOrientation) mPicSize.width else mPicSize.height,
                    if (needExchangeOrientation) mPicSize.height else mPicSize.width,
                    if (needExchangeOrientation) mPicSize.width else mPicSize.height,
                    supportPicSize.toList()
                )
            }
            texture_view.surfaceTexture.setDefaultBufferSize(
                mPreviewSize.width,
                mPreviewSize.height
            )
            mImageReader = ImageReader.newInstance(
                mPicSize.width,
                mPicSize.height,
                ImageFormat.JPEG,
                1
            )
            mImageReader?.setOnImageAvailableListener({ reader ->
                reader?.let {
                    var bitmap: Bitmap? = null
                    mCoroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            val image = it.acquireNextImage()
                            val byteBuffer = image.planes[0].buffer
                            val byteArray = ByteArray(byteBuffer.remaining())
                            byteBuffer.get(byteArray)
                            image.close()
                            bitmap = BitmapUtil.saveBitmap(baseContext, byteArray)
                        }
                        image_view.setImageBitmap(bitmap)
                    }
                }
            }, mCameraHandler)
            openCamera()
        }


    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) return
        mCameraManager.openCamera(mCameraId!!, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                mCameraDevice = camera
                createCameraSession(camera)
            }

            override fun onClosed(camera: CameraDevice) {
                super.onClosed(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
            }

            override fun onError(camera: CameraDevice, error: Int) {
            }

        }, mCameraHandler)
    }

    fun tickPicture() {
        if (mImageReader == null || mCameraDevice == null || !mCanTakePic) return
        val mCameraCaptureRequest =
            mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        mCameraCaptureRequest?.let {
            it.addTarget(mImageReader!!.surface)
            it.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            it.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            it.set(CaptureRequest.JPEG_ORIENTATION, mCameraSensorOrientation)
            mCameraCaptureSession?.capture(it.build(), null, mCameraHandler)
        }
    }

    fun createCameraSession(camera: CameraDevice) {
        val cameraCapture = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        val surface = Surface(texture_view.surfaceTexture)
        cameraCapture.addTarget(surface)
        cameraCapture.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
        )
        cameraCapture.set(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        camera.createCaptureSession(
            arrayListOf(surface, mImageReader?.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    mCameraCaptureSession = session
                    session.setRepeatingRequest(
                        cameraCapture.build(),
                        object : CameraCaptureSession.CaptureCallback() {
                            override fun onCaptureCompleted(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                result: TotalCaptureResult
                            ) {
                                super.onCaptureCompleted(session, request, result)
                                mCanTakePic = true
                                mCanExchangeCamera = true

                            }
                        },
                        mCameraHandler
                    )
                }

            },
            mCameraHandler
        )
    }

    fun exchangeWidthAndHeight(sensorOrientation: Int, displayOrientation: Int): Boolean {
        var exchange = false
        when (displayOrientation) {
            Surface.ROTATION_0, Surface.ROTATION_180 ->
                if (sensorOrientation == 90 || sensorOrientation == 270) exchange = true
            Surface.ROTATION_90, Surface.ROTATION_270 ->
                if (sensorOrientation == 0 || sensorOrientation == 180) exchange = true
        }
        return exchange
    }

    fun exchangeCamera() {
        if (mCameraDevice == null || !mCanExchangeCamera || !texture_view.isAvailable) return
        mCameraFacing = if (mCameraFacing == CameraCharacteristics.LENS_FACING_FRONT)
            CameraCharacteristics.LENS_FACING_BACK
        else
            CameraCharacteristics.LENS_FACING_FRONT
        releaseCamera()
        initCameraInfo()
    }

    fun getBestSizeAccordingCamera(
        targetWidth: Int,
        targetHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        sizes: List<Size>
    ): Size {
        val bigSize = ArrayList<Size>()
        val smallSize = ArrayList<Size>()
        sizes.forEach {
            if (it.height <= maxHeight && it.width <= maxWidth && (targetHeight / targetWidth) == (it.height / it.width)) {
                if ((it.width >= targetWidth) && it.height >= targetHeight) {
                    bigSize.add(it)
                } else {
                    smallSize.add(it)
                }
            }
        }
        return when {
            bigSize.size > 0 -> min(bigSize, CompareSizesByArea())
            smallSize.size > 0 -> Collections.max(smallSize, CompareSizesByArea())
            else -> sizes[0]
        }
    }

    private class CompareSizesByArea : Comparator<Size> {
        override fun compare(size1: Size, size2: Size): Int {
            return java.lang.Long.signum(size1.width.toLong() * size1.height - size2.width.toLong() * size2.height)
        }
    }
}

