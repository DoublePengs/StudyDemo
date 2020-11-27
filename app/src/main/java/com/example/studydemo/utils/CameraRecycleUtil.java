package com.example.studydemo.utils;

import android.hardware.camera2.TotalCaptureResult;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/8/26
 */
public class CameraRecycleUtil {
    private static Field sTargetField;
    private static Method sTargetMethod;

    public static void recycle(TotalCaptureResult tcr) {
        try {
            if (sTargetField == null) {
                sTargetField = tcr.getClass().getSuperclass().getDeclaredField("mResults");
                sTargetField.setAccessible(true);
            }
            if (sTargetMethod == null) {
                sTargetMethod = Class.forName("android.hardware.camera2.impl.CameraMetadataNative").getDeclaredMethod("close");
                sTargetMethod.setAccessible(true);
            }
            sTargetMethod.invoke(sTargetField.get(tcr));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
