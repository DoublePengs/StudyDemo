package com.example.studydemo.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

import androidx.core.app.ActivityCompat;

/**
 * Description: 测试张佳楠咨询问过的 Android 设备 androidId deviceid 等值的问题
 *
 * @author glp
 * @date 2022/3/16
 */
public class HardwareUtil {
    private static boolean noPermission = true;
    private static String cacheHardwareId = null;

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getHardwareId(Context aContext) {
        if (!noPermission && !TextUtils.isEmpty(cacheHardwareId)) {
            return cacheHardwareId;
        }

        TelephonyManager tm = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
        String androidId = "";
        String tmDevice = "";
        String tmSerial = "";

        try {
            noPermission = ActivityCompat.checkSelfPermission(aContext, Manifest.permission.READ_PHONE_STATE) !=
                    PackageManager.PERMISSION_GRANTED;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
        } catch (Exception ignored) {
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            androidId = "" + Settings.Secure.getString(aContext.getContentResolver(), "android_id");
        }

        UUID deviceUuid = new UUID(androidId.hashCode(), (long) tmDevice.hashCode() << 32 | (long) tmSerial.hashCode());
        cacheHardwareId = deviceUuid.toString();
        Log.d("deviceid", "deviceid:" + cacheHardwareId);

        return cacheHardwareId;
    }
}