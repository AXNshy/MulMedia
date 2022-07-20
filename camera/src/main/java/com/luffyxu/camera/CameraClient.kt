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
import android.view.Surface
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.luffyxu.mulmedia.utils.CameraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
    }

    private var canCapture :Boolean = false

    val scope : CoroutineScope = CoroutineScope(Dispatchers.Main)

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


    suspend fun init(context: Context, surface: Surface): Boolean {
        if (!checkPermission(context)) {
            Toast.makeText(context,"没有权限",Toast.LENGTH_SHORT).show()
            Log.d(TAG, "please request camera permission first")
            return false
        }
        this.context = context
        this@CameraClient.surface = surface
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraIds = cameraManager.cameraIdList
        Log.d(TAG, "cameraIdList $cameraIds")

        val size = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )!!
            .getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!

        mReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 2)

            mCamera = openCamera(cameraManager, cameraIds[0])
            captureSession = createSession(mCamera, mutableListOf(surface,mReader.surface))
//
        canCapture = true
        return true
    }

    fun destroy(){

    }

    @SuppressLint("MissingPermission")
    suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice {
        return suspendCoroutine {
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    it.resume(camera)
                }

                override fun onClosed(camera: CameraDevice) {
                    super.onClosed(camera)
                    it.resumeWithException(RuntimeException("onClosed"))
                }

                override fun onDisconnected(camera: CameraDevice) {
                    it.resumeWithException(RuntimeException("onDisconnected"))
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    it.resumeWithException(RuntimeException("onError $error"))
                }
            }, cameraHandler)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun createSession(device: CameraDevice, targets: List<Surface>): CameraCaptureSession {
        return suspendCoroutine {
            device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    it.resume(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    it.resumeWithException(RuntimeException("onConfigureFailed $session"))
                }

            }, cameraHandler)
        }
    }

    /*
    * 开启预览，创建一个CaptureRequest对象，模板参数为CameraDevice.TEMPLATE_PREVIEW，为request对象设置绘制的surface对象。
    * 最后通过CameraCaptureSession::setRepeatingRequest 发送预览的请求到RequestQueue中
    * */
    suspend fun openPreview() {
        val captureRequest = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest.addTarget(surface)
        captureSession.setRepeatingRequest(
            captureRequest.build(),
            object : CameraCaptureSession.CaptureCallback() {
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
                }

                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    super.onCaptureFailed(session, request, failure)
                }

            },
            cameraHandler
        )
    }


    /*
    * 拍照
    *
    * */
    suspend fun takePhoto(): CombinedCaptureResult = suspendCoroutine { con ->
        while (mReader.acquireNextImage() != null) {
        }

        val imageQueue = ArrayBlockingQueue<Image>(2)
        mReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            imageQueue.add(image)
        }, imageReaderHandler)


        val captureRequest = captureSession.device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
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
                imageReaderHandler.postDelayed(timeout,5000)
                scope.launch {
                    while (true){
                        val image = imageQueue.take()
                        if(resultTimestamp != image.timestamp) continue

                        imageReaderHandler.removeCallbacks(timeout)
                        mReader.setOnImageAvailableListener(null,null)
                        while (imageQueue.size>0){
                            imageQueue.take().close()
                        }

                        val mirrired = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
                        val rotation = 0
                        con.resume(CombinedCaptureResult(image,result,CameraUtils.computeExifOrientation(rotation,mirrired),mReader.imageFormat))
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

    suspend fun savePhoto(result : CombinedCaptureResult) : File = suspendCoroutine{ cont->
        when{
            result.format == ImageFormat.JPEG ->{
                val buffer = result.image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
                try{
                    val output = CameraUtils.createFile(context,"jpg")
                    Log.d(TAG,"savePhoto to [${output.path}]")
                    FileOutputStream(output).use { it.write(bytes) }
                    cont.resume(output)
                }catch (e : IOException){
                    cont.resumeWithException(e)
                }
            }
            else ->{

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