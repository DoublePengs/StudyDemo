package com.example.studydemo.activity;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;
import com.example.studydemo.widget.CameraPreviewView;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/8/26
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraViewActivity extends AppCompatActivity {

    private CameraPreviewView mCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        mCameraView = findViewById(R.id.camera_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }
}
