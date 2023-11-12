package com.example.studydemo.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;


import com.example.studydemo.MyApplication;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LbbUriUtils {

    private static final String TAG = "UriUtils";

    @SuppressLint("NewApi")
    public static String getPathByUri(Context context, Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + split[1];
                    }
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException ex) {
            Log.i(TAG, String.format(Locale.getDefault(), "getDataColumn: _data - [%s]", ex.getMessage()));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * 判断是否是Google相册的图片，类似于content://com.google.android.apps.photos.content/...
     **/
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * 判断是否是Google相册的图片，类似于content://com.google.android.apps.photos.contentprovider/0/1/mediakey:/local%3A821abd2f-9f8c-4931-bbe9-a975d1f5fabc/ORIGINAL/NONE/1075342619
     **/
    public static boolean isGooglePlayPhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    public static void getLocalCachePath(Uri uri, String parentPath, final IHanlderCallback callback) {
        try {
            if (callback == null || TextUtils.isEmpty(parentPath)) {
                return;
            }
            String urlStr = uri.toString();
            if (urlStr.toLowerCase().startsWith("content")) {
                copyToLocalFile(uri, parentPath, new IHanlderCallback() {
                    @Override
                    public void onSuccess(Object... object) {
                        callback.onSuccess((String) object[0]);
                    }

                    @Override
                    public void onFail(Object... object) {
                        callback.onFail();
                    }
                });
            } else {
                String path = LbbUriUtils.getPathByUri(MyApplication.getContext(), uri);
                if (!TextUtils.isEmpty(path)) {
                    callback.onSuccess(path);
                } else {
                    callback.onFail();
                }
            }
        } catch (Exception e) {
            callback.onFail();
        }

    }


    private static void copyToLocalFile(final Uri uri, final String parentPath, final IHanlderCallback callback) {
        if (callback == null) {
            return;
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Cursor cursor = MyApplication.getContext().getContentResolver().query(uri, null, null, null, null);
                    if (cursor == null) {
                        callback.onFail();
                        return;
                    }
                    String fileName = null;
                    if (cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                        if (index == -1) {
                            callback.onFail();
                            return;
                        }
                        fileName = cursor.getString(index);
                    }
                    if (TextUtils.isEmpty(fileName)) {
                        callback.onFail();
                        return;
                    }
                    final String dstFileName = fileName;
                    InputStream inputStream = MyApplication.getContext().getContentResolver().openInputStream(uri);
                    String path = parentPath + File.separator + dstFileName;
                    File outFile = new File(path);
                    LbbFileUtils.copy(inputStream, outFile);
                    callback.onSuccess(path);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFail();
                }
            }
        });
    }

}
