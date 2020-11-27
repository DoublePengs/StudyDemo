package com.example.studydemo.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Description:
 *
 * @author: glp
 * @date: 2020/9/9
 */
public class ImageUtils {
    private static final String TAG = "ImageUtils";

    /**
     * 保存图片到图库
     *
     * @param bmp
     */
    public static void saveImageToGallery(Bitmap bmp, String bitName) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(),
                "StudyDemo");
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        String fileName = bitName + ".jpg";
        File file = new File(appDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 直接保存到系统相册，这个时候只会有一张图片

    /**
     * 保存图片到系统相册
     */
    public static void saveBitmapToCamera(Context context, Bitmap bitmap, String fileName) {
        Log.d(TAG, "-------------->> Build.BRAND: " + Build.BRAND);
        String filePath;
        File file;

        String cameraPath = Environment.getExternalStorageDirectory() + File.separator
                + Environment.DIRECTORY_DCIM + File.separator
                + "Camera" + File.separator;

        File rootFile = new File(cameraPath);
        if (!rootFile.exists()) {
            Log.i(TAG, "-------------->> 文件路径不存在: " + rootFile.getAbsolutePath());
            cameraPath = Environment.getExternalStorageDirectory() + File.separator
                    + Environment.DIRECTORY_PICTURES + File.separator;
        }
        filePath = cameraPath + fileName + ".jpg";
        Log.i(TAG, "-------------->> 文件路径: " + filePath);

        file = new File(filePath);
        if (file.exists()) {
            boolean delete = file.delete();
            Log.i(TAG, "-------------->> 文件已存在 delete = " + delete);
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
                // 插入图库
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + filePath)));
                }
                MediaScannerConnection.scanFile(context, new String[]{filePath}, null, null);

                Toast.makeText(context, "图片保存成功", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 保存到自定义路径，然后再刷新到系统相册，这个时候会有两张图片
    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "QrCode");
        if (!appDir.exists()) {

            appDir.mkdir();
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 判断SDK版本是不是4.4或者高于4.4
            String[] paths = new String[]{file.getAbsolutePath()};
            MediaScannerConnection.scanFile(context, paths, null, null);
        } else {
            final Intent intent;
            if (file.isDirectory()) {
                intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
                intent.setClassName("com.android.providers.media", "com.android.providers.media.MediaScannerReceiver");
                intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            } else {
                intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
            }
            context.sendBroadcast(intent);
        }

    }

}
