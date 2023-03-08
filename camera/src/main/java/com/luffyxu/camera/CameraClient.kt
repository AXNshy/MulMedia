package com.luffyxu.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.luffyxu.mulmedia.utils.CameraUtils
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Runnable
import java.lang.RuntimeException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CameraClient {
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

    val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    //preview surface
    lateinit var surface: Surface

    lateinit var cameraManager: CameraManager
    lateinit var cameraIds: Array<String>
    lateinit var captureSession: CameraCaptureSession
    lateinit var mCamera: CameraDevice

    lateinit var context: Context
    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(cameraIds[0])
    }

    //用来直接读取图像像素数据
    lateinit var mReader: ImageReader

    val cameraHandler: Handler = Handler(Looper.getMainLooper())
    val imageReaderHandler: Handler = Handler(Looper.getMainLooper())

    var clientCallback: Callback? = null



    suspend fun init(context: Context, sv: AutoFitSurfaceView,cameraId: Int = 0): Boolean {
        if (!checkPermission(context)) {
            Toast.makeText(context, "没有权限", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "please request camera permission first")
            return false
        }
        this.context = context
        this@CameraClient.surface = sv.holder.surface
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraIds = cameraManager.cameraIdList
        Log.d(TAG, "cameraIdList ${cameraIds.joinToString { it }}")

        val size = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!
            .getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!

        val viewSize = getPreviewSize(context!!.display!!,characteristics,SurfaceHolder::class.java)

        mReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 2)
        sv.setAspectRatio(viewSize.width,viewSize.height)

        mCamera = openCamera(cameraManager, cameraIds[cameraId])
        captureSession = createSession(mCamera, mutableListOf(surface, mReader.surface))
//
//        captureSession.device.si
        canCapture = true
        return true
    }

    fun getSuitableSurfaceSize(availableSizes: Array<Size>,display: Display):Size{
        val size = CameraUtils.getSuitableSize(availableSizes, display)
        return size
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

    @SuppressLint("MissingPermission")
    suspend fun createSession(device: CameraDevice, targets: List<Surface>): CameraCaptureSession {
        return suspendCoroutine { cont ->
            device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cont.resume(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(RuntimeException("onConfigureFailed $session"))
                }

            }, cameraHandler)
        }
    }

    /*
    * 开启预览，创建一个CaptureRequest对象，模板参数为CameraDevice.TEMPLATE_PREVIEW，为request对象设置绘制的surface对象。
    * 最后通过CameraCaptureSession::setRepeatingRequest 发送预览的请求到RequestQueue中
    * */
    suspend fun openPreview(surface: Surface) {
        val captureRequest = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest.addTarget(surface)
        captureSession.setRepeatingRequest(
            captureRequest.build(),
            null,
            cameraHandler
        )
    }


    suspend fun closePreview() {
        captureSession.stopRepeating()
    }

    /*
    * 拍照
    *
    * */
    suspend fun takePhoto(): CombinedCaptureResult = suspendCoroutine { con ->

        Log.d(TAG, "takePhoto")
        while (mReader.acquireNextImage() != null) {
        }

        val imageQueue = ArrayBlockingQueue<Image>(2)
        mReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            imageQueue.add(image)
        }, imageReaderHandler)


        val captureRequest =
            captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(mReader.surface)
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
                        mReader.setOnImageAvailableListener(null, null)
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
                                mReader.imageFormat
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
                    val output = CameraUtils.createFile(context.applicationContext, "jpg")
                    Log.d(TAG, "savePhoto to [${output.path}]")
                    FileOutputStream(output).use { it.write(bytes) }
                    cont.resume(output)
                } catch (e: IOException) {
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