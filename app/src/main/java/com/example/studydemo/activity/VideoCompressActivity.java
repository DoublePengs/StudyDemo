package com.example.studydemo.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studydemo.R;
import com.example.studydemo.utils.IHanlderCallback;
import com.example.studydemo.utils.LbbFileUtils;
import com.example.studydemo.utils.LbbUriUtils;
import com.example.studydemo.utils.videocompress.VideoCompress;
import com.googlecode.mp4parser.util.Matrix;
import com.hw.videoprocessor.VideoProcessor;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description:
 *
 * @author glp
 * @date 2023/8/30
 */
public class VideoCompressActivity extends AppCompatActivity {

    public static final int QUEST_PICK_VIDEO = 1;
    private final int REQUEST_VIDEO = 101;
    public static final String VIDEO_PATH = "video_path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_compress);
        findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, QUEST_PICK_VIDEO);
                } catch (ActivityNotFoundException ex) {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QUEST_PICK_VIDEO) {
            // 选择本地视频之后成功的结果回调
            if (data != null) {
                final Uri uri = data.getData();
                try {
                    String urlStr = uri.toString();
                    if (urlStr.toLowerCase().startsWith("content")) {
                        copFile(uri, new IHanlderCallback() {
                            @Override
                            public void onSuccess(Object... object) {
                                String path = (String) object[0];
                                if (path != null && path.length() > 0) {
                                    setUploadVideoData(path);
                                }
                            }

                            @Override
                            public void onFail(Object... object) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(VideoCompressActivity.this, "CopyFile onFail", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    } else {
                        String path = LbbUriUtils.getPathByUri(this, uri);
                        if (path != null && path.length() > 0) {
                            setUploadVideoData(path);
                        }
                    }
                } catch (Exception e) {
                    finish();
                }
            }
        }
    }

    private void setUploadVideoData(String videoUrl) {
        // TODO 压缩视频
        String type = videoUrl.substring(videoUrl.lastIndexOf(".") + 1);
        final String outputPath = videoUrl.substring(0, videoUrl.lastIndexOf(File.separator)) + File.separator + System.currentTimeMillis() + "_compress." + type;
        Matrix matrix = Matrix.ROTATE_0;
        Log.i("VideoCompress", "------>> videoUrl=" + videoUrl);
        Log.i("VideoCompress", "------>> outputPath=" + outputPath);

//        VideoCompressUtil.compressVideo(videoUrl, outputPath);

        executeScaleVideo(videoUrl, outputPath);

//        VideoCompress.compressVideoHigh(videoUrl, outputPath, new VideoCompress.CompressListener() {
//            @Override
//            public void onStart() {
//                Log.i("VideoCompress", "------>> onStart");
//            }
//
//            @Override
//            public void onSuccess() {
//                Log.i("VideoCompress", "------>> onSuccess outputPath=" + outputPath);
//            }
//
//            @Override
//            public void onFail() {
//                Log.i("VideoCompress", "------>> onFail");
//            }
//
//            @Override
//            public void onProgress(float percent) {
//                Log.i("VideoCompress", "------>> onProgress:" + percent);
//            }
//        });
    }

    public void copFile(final Uri uri, final IHanlderCallback callback) {
        if (callback == null) {
            return;
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    String type = dstFileName.substring(dstFileName.lastIndexOf(".") + 1);
                    String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "CopyVideo" + File.separator + System.currentTimeMillis() + "." + type;
                    File outFile = new File(path);
                    LbbFileUtils.copy(inputStream, outFile);
                    callback.onSuccess(path);
                } catch (Exception e) {
                    callback.onFail();
                }
            }
        });
    }

    private void executeScaleVideo(final String inputPath, final String outputPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                try {
                    Log.e("VideoCompress", "------>> start compress");
                    Uri uri = Uri.parse(inputPath);
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(VideoCompressActivity.this, uri);
                    int originWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    int originHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    int bitrate = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));

                    int outWidth = originWidth / 2;
                    int outHeight = originHeight / 2;
                    VideoProcessor.processor(getApplicationContext())
                            .input(inputPath)
                            .output(outputPath)
                            .outWidth(outWidth)
                            .outHeight(outHeight)
                            .bitrate(bitrate / 2)
                            .process();
                } catch (Exception e) {
                    success = false;
                    e.printStackTrace();
                    Log.e("VideoCompress", "------>> compress error exception:" + e);
                }
                if (success) {
                    Log.e("VideoCompress", "------>> compress success filePath:" + outputPath);
                }
            }
        }).start();
    }
}
