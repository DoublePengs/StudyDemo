package com.example.studydemo.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK
import android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
import android.hardware.camera2.CameraDevice
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.studydemo.R
import kotlinx.android.synthetic.main.activity_camera_preview.*
import java.util.*
import java.util.concurrent.Semaphore


/**
 *
 * Description: 打开相机预览（Android 5.0以下版本）
 * Author: glp
 * CreateDate: 2020-06-11
 */
class CameraPreviewLowerActivity : AppCompatActivity(), View.OnClickListener {

    private val MY_REQUEST_CODE = 1
    private val TAG = "CameraPreviewActivity"

    /**
     *  CAMERA_FACING_BACK = 0;  后置摄像头
     *  CAMERA_FACING_FRONT = 1; 前置摄像头
     */
    private var mCameraId: String = "1"
    private lateinit var mCamera: Camera
    private var cameraWidth: Int = 0
    private var cameraHeight: Int = 0


    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val cameraOpenCloseLock = Semaphore(1)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_preview)
        open_camera.setOnClickListener(this)
        close_camera.setOnClickListener(this)
        change_camera.setOnClickListener(this)
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
        initCamera()
    }

    /**
     * Close the [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            mCamera.stopPreview()
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

        // Android 5.0以下版本
        try {
            // CAMERA_FACING_BACK = 0;  后置摄像头
            // CAMERA_FACING_FRONT = 1; 前置摄像头

            mCamera = Camera.open(if (mCameraId == "0") CAMERA_FACING_BACK else CAMERA_FACING_FRONT)
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(
                if (mCameraId == "0") CAMERA_FACING_BACK else CAMERA_FACING_FRONT,
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
        Camera.getCameraInfo(
            if (mCameraId == "0") CAMERA_FACING_BACK else CAMERA_FACING_FRONT,
            info
        )
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

}