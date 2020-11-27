package com.example.studydemo.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/9/9
 */
public class PermissionUtil {
    private static int MY_REQUEST_CODE = 1;

    private static final String TAG = "PermissionUtil";

    public static boolean hasWritePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 及以上版本

            int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            // PERMISSION_GRANTED = 0 有权限
            // PERMISSION_DENIED = -1 没权限

            if (result == PackageManager.PERMISSION_GRANTED) {
                // 拥有权限
                Log.e(TAG, "---------->> 拥有权限");
                return true;

            } else {

                // 检查用户是否禁止申请该权限
                boolean denied = ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (denied) {
                    // 用户禁止
                    Log.e(TAG, "---------->> 用户拒绝过权限");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_CODE);

                } else {
                    // 用户勾选禁止后不再提示、其他
                    Log.e(TAG, "---------->> 用户没有拒绝,尝试申请权限");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
                }
                return false;
            }

        }

        return true;
    }

    public static boolean hasReadPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 及以上版本

            int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

            // PERMISSION_GRANTED = 0 有权限
            // PERMISSION_DENIED = -1 没权限

            if (result == PackageManager.PERMISSION_GRANTED) {
                // 拥有权限
                Log.e(TAG, "---------->> 拥有权限");
                return true;

            } else {

                // 检查用户是否禁止申请该权限
                boolean denied = ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (denied) {
                    // 用户禁止
                    Log.e(TAG, "---------->> 用户拒绝过权限");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE);

                } else {
                    // 用户勾选禁止后不再提示、其他
                    Log.e(TAG, "---------->> 用户没有拒绝,尝试申请权限");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
                }
                return false;
            }

        }

        return true;
    }

}
