package com.example.studydemo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera
import android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
import android.hardware.camera2.*
import android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.studydemo.R
import kotlinx.android.synthetic.main.activity_camera_preview.*
import java.util.*
import java.util.concurrent.Semaphore


/**
 *
 * Description: 打开相机预览
 * Author: glp
 * CreateDate: 2020-06-11
 */
class CameraPreviewActivity : AppCompatActivity(), View.OnClickListener {

    private val MY_REQUEST_CODE = 1
    private val TAG = "CameraPreviewActivity"

    private var mFlashSupported: Boolean = false

    /**
     *  CAMERA_FACING_BACK = 0;  后置摄像头
     *  CAMERA_FACING_FRONT = 1; 前置摄像头
     */
    private var mCameraId: String = "0"
    private lateinit var mCamera: Camera
    private var cameraWidth: Int = 0
    private var cameraHeight: Int = 0


    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val cameraOpenCloseLock = Semaphore(1)

    /**
     * A reference to the opened [android.hardware.camera2.CameraDevice].
     */
    private var mCameraDevice: CameraDevice? = null

    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    /**
     * A [Handler] for running tasks in the background.
     */
    private var backgroundHandler: Handler? = null

    /**
     * A reference to the current [android.hardware.camera2.CameraCaptureSession] for
     * preview.
     */
    private var captureSession: CameraCaptureSession? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_preview)
        open_camera.setOnClickListener(this)
        close_camera.setOnClickListener(this)
        change_camera.setOnClickListener(this)

        // 设置surface圆角
        texture_view.outlineProvider =
            TextureVideoViewOutlineProvider(
                30F
            )
        texture_view.clipToOutline = true;
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.open_camera -> openCamera()
            R.id.close_camera -> closeCamera()
            R.id.change_camera -> {
                mCameraId = if (mCameraId == "0") {
                    "1"
                } else {
                    "0"
                }
                closeCamera()
                openCamera()
            }
        }
    }

    /**
     * 打开相机预览
     */
    private fun openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 及以上版本

            val result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

            // PERMISSION_GRANTED = 0 有权限
            // PERMISSION_DENIED = -1 没权限

            if (result == PackageManager.PERMISSION_GRANTED) {
                // 拥有权限
                Log.e(TAG, "---------->> 拥有权限")
                initCamera()

            } else {

                // 检查用户是否禁止申请该权限
                val denied = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA
                )
                if (denied) {
                    // 用户禁止
                    Log.e(TAG, "---------->> 用户拒绝过权限")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_REQUEST_CODE
                    )

                } else {
                    // 用户勾选禁止后不再提示、其他
                    Log.e(TAG, "---------->> 用户没有拒绝,尝试申请权限")
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        MY_REQUEST_CODE
                    )
                }
            }

        } else {
            initCamera()
        }
    }

    /**
     * Close the [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            closePreviewSession()
            mCameraDevice?.close()
            mCameraDevice = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * 打开相机
     */
    @SuppressLint("MissingPermission")
    private fun initCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5.0及以上
            val cameraManager: CameraManager =
                getSystemService(Context.CAMERA_SERVICE) as CameraManager?
                    ?: return

            try {
                //获取可用摄像头列表
                for (cameraId in cameraManager.cameraIdList) {
                    val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

//                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
//                        // 前置摄像头
//                        continue
//                    }

                    if (mCameraId == cameraId) {
                        val map =
                            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                ?: continue

                        val available =
                            characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)

                        mFlashSupported = available != null
                        Toast.makeText(this, "相机可用", Toast.LENGTH_SHORT).show()
                    }

                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            } catch (e: NullPointerException) {
                // 不支持Camera2API
                Toast.makeText(this, "不支持Camera2API", Toast.LENGTH_SHORT).show()
            }

            cameraManager.openCamera(mCameraId, stateCallback, null)
        } else {
            // Android 5.0以下版本
            try {
                // CAMERA_FACING_BACK = 0;  后置摄像头
                // CAMERA_FACING_FRONT = 1; 前置摄像头

                mCamera = Camera.open(if (mCameraId == "0") Camera.CameraInfo.CAMERA_FACING_BACK else CAMERA_FACING_FRONT)
                val cameraInfo = Camera.CameraInfo()
                Camera.getCameraInfo(
                    if (mCameraId == "0") Camera.CameraInfo.CAMERA_FACING_BACK else CAMERA_FACING_FRONT,
                    cameraInfo
                )
                val params = mCamera.parameters
                val bestPreviewSize = calBestPreviewSize(
                    mCamera.parameters, 1080, 1440
                )
                cameraWidth = bestPreviewSize.width
                cameraHeight = bestPreviewSize.height
                params.setPreviewSize(cameraWidth, cameraHeight)
                mCamera.setDisplayOrientation(getCameraAngle(this))
                mCamera.setParameters(params)

                val lp = texture_view.layoutParams
                lp.height = texture_view.width * 4 / 3
                texture_view.layoutParams = lp

                mCamera.setPreviewTexture(texture_view.surfaceTexture)
                mCamera.startPreview()
            } catch (e: Exception) {
                Toast.makeText(this, "初始化相机出现异常", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */
    private fun calBestPreviewSize(
        camPara: Camera.Parameters,
        width: Int,
        height: Int
    ): Camera.Size {
        val allSupportedSize = camPara.supportedPreviewSizes
        val widthLargerSize = ArrayList<Camera.Size>()
        for (tmpSize in allSupportedSize) {
            if (tmpSize.width > tmpSize.height) {
                widthLargerSize.add(tmpSize)
            }
        }

        widthLargerSize.sortWith(Comparator { lhs, rhs ->
            val off_one = Math.abs(lhs.width * lhs.height - width * height)
            val off_two = Math.abs(rhs.width * rhs.height - width * height)
            off_one - off_two
        })

        return widthLargerSize[0]
    }

    /**
     * 获取照相机旋转角度
     */
    private fun getCameraAngle(activity: Activity): Int {
        var rotateAngle = 90
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(CAMERA_FACING_FRONT, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotateAngle = (info.orientation + degrees) % 360
            rotateAngle = (360 - rotateAngle) % 360 // compensate the mirror
        } else { // back-facing
            rotateAngle = (info.orientation - degrees + 360) % 360
        }
        return rotateAngle
    }


    /**
     * 重写onRequestPermissionsResult方法根据用户的不同选择做出响应
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限获取成功
                Log.e(TAG, "---------->> 权限获取成功")


            } else {
                // 没有权限
                Log.e(TAG, "---------->> 没有权限")
                showWaringDialog()
            }
        }
    }

    private fun showWaringDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("警告！")
            .setMessage("请前往设置->应用->StudyDemo->权限中打开相关权限，否则功能无法正常运行！")
            .setPositiveButton("确定", DialogInterface.OnClickListener { dialog, which ->
                // 一般情况下如果用户不授权的话，功能是无法运行的，做退出处理
                // finish()

                // 引导用户打开设置页打开权限
                openSetting()
            }).show()
    }

    /**
     * 打开该应用的设置详情页 引导打开权限
     */
    private fun openSetting() {
        val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
        val uri = Uri.fromParts("package", this.packageName, null as String?)
        intent.data = uri
        startActivityForResult(intent, MY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            //
            Log.e(TAG, "---------->> onActivityResult() 用户从设置页面回来了，可以做重新检查权限动作")
        }
    }

    private val stateCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            mCameraDevice = cameraDevice
            startPreview()

            val lp = texture_view.layoutParams
            lp.height = texture_view.width * 4 / 3
            texture_view.layoutParams = lp

            configureTransform(texture_view.width, texture_view.height)
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            mCameraDevice = null
            finish()
        }

    }

    /**
     * Start the camera preview.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startPreview() {
        if (mCameraDevice == null || !texture_view.isAvailable) return

        try {
            closePreviewSession()
            val texture = texture_view.surfaceTexture
            texture?.setDefaultBufferSize(texture_view.width, texture_view.height)
            previewRequestBuilder = mCameraDevice!!.createCaptureRequest(TEMPLATE_PREVIEW)

            val previewSurface = Surface(texture)
            previewRequestBuilder.addTarget(previewSurface)

            mCameraDevice?.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        updatePreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(this@CameraPreviewActivity, "Failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun closePreviewSession() {
        captureSession?.close()
        captureSession = null
    }

    /**
     * Update the camera preview. [startPreview] needs to be called in advance.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun updatePreview() {
        if (mCameraDevice == null) return

        try {
            setUpCaptureRequestBuilder(previewRequestBuilder)
            HandlerThread("CameraPreview").start()
            captureSession?.setRepeatingRequest(
                previewRequestBuilder.build(),
                null, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder?) {
        builder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }


    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `textureView`.
     * This method should not to be called until the camera preview size is determined in
     * openCamera, or until the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val rotation = windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, texture_view.height.toFloat(), texture_view.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                viewHeight.toFloat() / texture_view.height,
                viewWidth.toFloat() / texture_view.width
            )
            with(matrix) {
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        }
        texture_view.setTransform(matrix)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    class TextureVideoViewOutlineProvider(private val mRadius: Float) :
        ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            val leftMargin = 0
            val topMargin = 0
            val selfRect = Rect(
                leftMargin, topMargin,
                rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin
            )
            outline.setRoundRect(selfRect, mRadius)
        }

    }


}