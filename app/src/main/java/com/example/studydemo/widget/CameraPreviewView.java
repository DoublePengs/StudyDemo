package com.example.studydemo.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import com.example.studydemo.R;
import com.example.studydemo.utils.CameraRecycleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW;

/**
 * Description: 相机预览  适用于Android 5.0 及之上版本
 * Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP  API level 21
 *
 * @author: glp
 * @date: 2020/8/20
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraPreviewView extends FrameLayout implements CameraPreviewListener {

    private TextureView mTextureView;
    private float mRadius;
    private CameraManager mCameraManager;
    private String mCameraId;
    private static final String TAG = "CameraPreviewView";

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * A reference to the opened [android.hardware.camera2.CameraDevice].
     */
    private CameraDevice mCameraDevice;
    /**
     * A reference to the current [android.hardware.camera2.CameraCaptureSession] for preview.
     */
    private CameraCaptureSession mCaptureSession;
    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * A [Handler] for running tasks in the background.
     */
    private Handler mBackgroundHandler;


    public CameraPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public CameraPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CameraPreviewView, defStyleAttr, 0);
        mRadius = a.getDimension(R.styleable.CameraPreviewView_camera_radius, 0);
        a.recycle();

        LayoutInflater.from(getContext()).inflate(R.layout.widget_camera_preview_view, this);
        mTextureView = (TextureView) findViewById(R.id.texture_view);
        // 设置TextureView圆角
        mTextureView.setOutlineProvider(new TextureVideoViewOutlineProvider(mRadius));
        mTextureView.setClipToOutline(true);
    }

    @SuppressLint("MissingPermission")
    public void openCamera() {
        mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        try {
            //获取可用摄像头列表
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                // 获取摄像头方向
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null) {
                    continue;
                }
                if (CameraCharacteristics.LENS_FACING_FRONT == facing) {
                    Log.i(TAG, "-------->> 前置摄像头 cameraId=" + cameraId);
                    mCameraId = cameraId;
                } else if (CameraCharacteristics.LENS_FACING_BACK == facing) {
                    Log.i(TAG, "-------->> 后置摄像头 cameraId=" + cameraId);
                } else {
                    Log.i(TAG, "-------->> 外置摄像头 cameraId=" + cameraId);
                }

                if (cameraId.equals(mCameraId)) {
                    Log.i(TAG, "-------->> 目标摄像头可用");
                    mCameraManager.openCamera(mCameraId, mStateCallback, null);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // 不支持Camera2API
            Toast.makeText(getContext(), "不支持Camera2API", Toast.LENGTH_SHORT).show();
        }
    }

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startPreview() {
        if (mCameraDevice == null || !mTextureView.isAvailable()) {
            return;
        }

        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewRequestBuilder.addTarget(previewSurface);

            List<Surface> outputs = new ArrayList<>();
            outputs.add(previewSurface);

            mCameraDevice.createCaptureSession(outputs, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    mCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.i(TAG, "-------->> Failed");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.toString());
        }

    }

    private void updatePreview() {
        if (mCameraDevice == null) {
            return;
        }

        try {
            setUpCaptureRequestBuilder(mPreviewRequestBuilder);
            new HandlerThread("CameraPreview").start();
            if (mCaptureSession != null) {
                mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, null);
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, e.toString());
        }

    }

    private void closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
        }
        mCaptureSession = null;
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        if (builder != null) {
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        }
    }


    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            startPreview();

            ViewGroup.LayoutParams lp = mTextureView.getLayoutParams();
            lp.height = mTextureView.getWidth() * 4 / 3;
            mTextureView.setLayoutParams(lp);

            configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

    };

    private void configureTransform(int viewWidth, int viewHeight) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0f, 0f, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0f, 0f, mTextureView.getHeight(), mTextureView.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            int scale = Math.max(viewHeight / mTextureView.getHeight(), viewWidth / mTextureView.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    @Override
    public void onResume() {
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.e(TAG, "------------->>> onCaptureStarted");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.e(TAG, "------------->>> onCaptureProgressed");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.e(TAG, "------------->>> onCaptureCompleted");
            CameraRecycleUtil.recycle(result);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.e(TAG, "------------->>> onCaptureFailed");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.e(TAG, "------------->>> onCaptureSequenceCompleted");
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.e(TAG, "------------->>> onCaptureSequenceAborted");
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.e(TAG, "------------->>> onCaptureBufferLost");
        }
    };

    /**
     * Android 5.0 之后提供的对 TextureView 进行裁剪的圆角效果
     */
    private static class TextureVideoViewOutlineProvider extends ViewOutlineProvider {
        private float mRadius;

        public TextureVideoViewOutlineProvider(float radius) {
            super();
            mRadius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            Rect rect = new Rect();
            view.getGlobalVisibleRect(rect);
            int leftMargin = 0;
            int topMargin = 0;
            Rect selfRect = new Rect(leftMargin, topMargin,
                    rect.right - rect.left - leftMargin,
                    rect.bottom - rect.top - topMargin);
            outline.setRoundRect(selfRect, mRadius);
        }
    }
}
