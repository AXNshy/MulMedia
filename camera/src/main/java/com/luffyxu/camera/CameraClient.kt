package com.luffyxu.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.luffyxu.mulmedia.utils.CameraUtils
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Runnable
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraClient(var context: Context? = null, var cameraId: Int = 0) {
    companion object {
        const val TAG = "CameraClient"

        data class CombinedCaptureResult(
            val image: Image,
            val metadata: CaptureResult,
            val orientation: Int,
            val format: Int
        ) : Closeable {
            override fun close() = image.close()
        }

        interface Callback {
            fun onCameraSizeChange(size: Size)
        }
    }

    private var canCapture: Boolean = false

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    //preview surface
    private lateinit var surface: Surface

    private var cameraManager: CameraManager
    private var cameraIds: Array<String>
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var mCamera: CameraDevice

    private var characteristics: CameraCharacteristics

    var previewView: CameraPreviewView? = null

    var previewFrameCallback: ((image: Image, data: ByteBuffer, width: Int, height: Int) -> Unit) =
        { _, _, _, _ -> }


    //用来直接读取图像像素数据
    private lateinit var mCaptureReader: ImageReader
    private lateinit var mPreviewImageReader: ImageReader


    private val cameraHandler: Handler = Handler(Looper.getMainLooper())
    private val imageReaderHandler: Handler = Handler(Looper.getMainLooper())

    init {
        cameraManager = context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraIds = cameraManager.cameraIdList
        characteristics = cameraManager.getCameraCharacteristics(cameraIds[cameraId])
    }

    suspend fun startCameraWithEffect(sv: SurfaceView): Boolean {
        Log.d(TAG, "init")
        if (!checkPermission(context!!)) {
            Toast.makeText(context, "没有权限", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "please request camera permission first")
            return false
        }

        mCamera = openCamera(cameraManager, cameraIds[cameraId])
        val previewSize = findSuitablePreviewSize(sv as CameraPreviewView)

        Log.d(TAG, "previewSize ImageReader size ${previewSize.width}x${previewSize.height}")
        val captureSize =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                .getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!

        Log.d(TAG, "captureSize ImageReader size ${captureSize.width}x${captureSize.height}")
        mCaptureReader =
            ImageReader.newInstance(captureSize.width, captureSize.height, ImageFormat.JPEG, 3)
        mPreviewImageReader = ImageReader.newInstance(
            previewSize.width,
            previewSize.height,
            ImageFormat.YUV_420_888,
            3
        )


        captureSession =
            createSession(mCamera, listOf(mCaptureReader.surface, mPreviewImageReader.surface))

        openPreviewWithProcess(mPreviewImageReader.surface) { image: Image, data: ByteBuffer, width: Int, height: Int ->
            previewFrameCallback.let { it(image, data, width, height) }
        }
        canCapture = true
        return true
    }

    suspend fun startCamera(sv: SurfaceView): Boolean {
        Log.d(TAG, "init")
        if (!checkPermission(context!!)) {
            Toast.makeText(context, "没有权限", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "please request camera permission first")
            return false
        }
        val previewSize = findSuitablePreviewSize(sv as CameraPreviewView)

        mCamera = openCamera(cameraManager, cameraIds[cameraId])
        surface = sv.holder.surface
        val captureSize =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                .getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!

        Log.d(TAG, "ImageReader size ${captureSize.width}x${captureSize.height}")
        mCaptureReader =
            ImageReader.newInstance(captureSize.width, captureSize.height, ImageFormat.JPEG, 3)
        mPreviewImageReader = ImageReader.newInstance(
            previewSize.width,
            previewSize.height,
            ImageFormat.YUV_420_888,
            3
        )


        captureSession =
            createSession(
                mCamera,
                listOf(surface, mCaptureReader.surface, mPreviewImageReader.surface)
            )

        openPreviewWithoutProcess(surface)
        canCapture = true
        return true
    }

    private fun findSuitablePreviewSize(surface: CameraPreviewView): Size {
        val previewSize = getPreviewSize(
            surface!!.display,
            characteristics,
            SurfaceHolder::class.java
        )
        Log.d(TAG, "View finder size: ${surface!!.width} x ${surface!!.height}")
        Log.d(TAG, "Selected preview size: $previewSize")
        surface!!.setAspectRatio(
            previewSize.width,
            previewSize.height
        )
        return previewSize
    }


    fun destroy() {

    }

    @SuppressLint("MissingPermission")
    suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice {
        return suspendCancellableCoroutine {
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    it.resume(camera)
                }

                override fun onClosed(camera: CameraDevice) {
                    super.onClosed(camera)
                    if (it.isActive) it.resumeWithException(RuntimeException("onClosed"))
                }

                override fun onDisconnected(camera: CameraDevice) {
                    if (it.isActive) it.resumeWithException(RuntimeException("onDisconnected"))
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (it.isActive) it.resumeWithException(RuntimeException("onError $error"))
                }
            }, cameraHandler)
        }
    }

    suspend fun closeCamera() {
        context = null
        captureSession.stopRepeating()
        captureSession.close()

        mCaptureReader.setOnImageAvailableListener(null, null)
        mPreviewImageReader.setOnImageAvailableListener(null, null)
        mCaptureReader.close()
        mPreviewImageReader.close()
    }

    @SuppressLint("MissingPermission")
    suspend fun createSession(device: CameraDevice, targets: List<Surface>): CameraCaptureSession {
        return suspendCoroutine { cont ->
            device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cont.resume(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    val exc = RuntimeException("Camera ${device.id} session configuration failed")
                    Log.e(TAG, exc.message, exc)
                    cont.resumeWithException(exc)
                }

            }, cameraHandler)
        }
    }

    /*
    * 开启预览，创建一个CaptureRequest对象，模板参数为CameraDevice.TEMPLATE_PREVIEW，为request对象设置绘制的surface对象。
    * 最后通过CameraCaptureSession::setRepeatingRequest 发送预览的请求到RequestQueue中
    * */
    fun openPreview() {
        openPreviewWithProcess(mPreviewImageReader.surface) { image: Image, data: ByteBuffer, width: Int, height: Int ->
            previewFrameCallback?.let { it(image, data, width, height) }
        }
//        openPreviewWithoutProcess(surface)
    }

    fun openPreviewWithoutProcess(surface: Surface) {
        val captureRequest = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest.addTarget(surface)
        captureSession.setRepeatingRequest(
            captureRequest.build(),
            null,
            cameraHandler
        )
    }

    fun openPreviewWithProcess(
        surface: Surface,
        callback: (image: Image, data: ByteBuffer, width: Int, height: Int) -> Unit
    ) {
        Log.d(TAG, "openPreviewWithProcess")
        mPreviewImageReader.setOnImageAvailableListener({
            val image = it.acquireLatestImage()
//            callback(CameraUtils.YUV_420_888_dataFetch(image),image.width,image.height)
            if (image != null) {

                Log.d(TAG, "onImageAvailable ${image.planes.size}")
                callback(image, image.planes[0].buffer, image.width, image.height)
                image.close()
            }
        }, cameraHandler)

        val captureRequest = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest.addTarget(surface)
        captureSession.setRepeatingRequest(
            captureRequest.build(),
            null,
            cameraHandler
        )
    }

    /*
    * 拍照
    *
    * */
    suspend fun takePhoto(): CombinedCaptureResult = suspendCoroutine { con ->

        Log.d(TAG, "takePhoto")
        while (mCaptureReader.acquireNextImage() != null) {
        }

        val imageQueue = ArrayBlockingQueue<Image>(2)
        mCaptureReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            imageQueue.add(image)
        }, imageReaderHandler)


        val captureRequest =
            captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(mCaptureReader.surface)
            }.build()

        captureSession.capture(captureRequest, object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureStarted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                timestamp: Long,
                frameNumber: Long
            ) {
                super.onCaptureStarted(session, request, timestamp, frameNumber)
            }

            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                val exc = TimeoutException("Image dequeuing took too long")
                val timeout = Runnable { con.resumeWithException(exc) }
                imageReaderHandler.postDelayed(timeout, 5000)
                scope.launch(con.context) {
                    while (true) {
                        val image = imageQueue.take()
                        if (resultTimestamp != image.timestamp) continue

                        imageReaderHandler.removeCallbacks(timeout)
                        mCaptureReader.setOnImageAvailableListener(null, null)
                        while (imageQueue.size > 0) {
                            imageQueue.take().close()
                        }

                        val mirrired =
                            characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
                        val rotation = 0
                        con.resume(
                            CombinedCaptureResult(
                                image,
                                result,
                                CameraUtils.computeExifOrientation(rotation, mirrired),
                                mCaptureReader.imageFormat
                            )
                        )
                    }
                }
            }

            override fun onCaptureFailed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                failure: CaptureFailure
            ) {
                super.onCaptureFailed(session, request, failure)
            }
        }, cameraHandler)
    }

    suspend fun savePhoto(result: CombinedCaptureResult): File = suspendCoroutine { cont ->
        when {
            result.format == ImageFormat.JPEG -> {
                val buffer = result.image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
                try {
                    val output = CameraUtils.createFile(context!!.applicationContext, "jpg")
                    output.parentFile.apply {
                        if (!exists()) {
                            mkdirs()
                        }
                    }
                    if (!output.exists()) {
                        output.createNewFile()
                    }
                    Log.d(TAG, "savePhoto to [${output.path}]")
                    FileOutputStream(output).use { it.write(bytes) }
                    cont.resume(output)
                } catch (e: IOException) {
                    e.printStackTrace()
                    cont.resumeWithException(e)
                }
            }
            else -> {

            }
        }
    }

    private fun checkPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}