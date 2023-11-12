package com.example.studydemo.utils.videocompress;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Description:
 *
 * @author glp
 * @date 2023/8/29
 */
public class VideoCompressUtil {

    private static final String TAG = "VideoCompressUtil";

    public static void compressVideo(String inputFilePath, String outputFilePath) {
        try {
            // 创建MediaExtractor用于提取视频文件的轨道信息
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(inputFilePath);

            int videoTrackIndex = selectVideoTrack(extractor);
            if (videoTrackIndex < 0) {
                Log.e(TAG, "No video track found in input file");
                return;
            }

            // 获取视频轨道的格式信息
            MediaFormat inputFormat = extractor.getTrackFormat(videoTrackIndex);
//            inputFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);//比特率
//            inputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//            inputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize);

            // 创建MediaCodec用于压缩视频
            MediaCodec codec = MediaCodec.createEncoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
            codec.configure(inputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();

            // 创建MediaMuxer用于将压缩后的数据写入文件
            MediaMuxer muxer = new MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int outputTrackIndex = muxer.addTrack(codec.getOutputFormat());
            muxer.start();

            // 分配输入缓冲区和输出缓冲区
            ByteBuffer[] inputBuffers = codec.getInputBuffers();
            ByteBuffer[] outputBuffers = codec.getOutputBuffers();

            boolean isEOS = false;
            long presentationTimeUs = 0;

            // 循环处理输入数据
            while (!isEOS) {
                int inputBufferIndex = codec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    int sampleSize = extractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isEOS = true;
                    } else {
                        long sampleTime = extractor.getSampleTime();
                        codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, sampleTime, 0);
                        extractor.advance();
                    }
                }

                // 处理输出数据
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);

                    // 将压缩后的数据写入文件
                    muxer.writeSampleData(outputTrackIndex, outputBuffer, bufferInfo);

                    codec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0);
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    isEOS = true;
                }
            }

            // 释放资源
            codec.stop();
            codec.release();
            extractor.release();
            muxer.stop();
            muxer.release();
            Log.i(TAG, "Video compression complete. Output file: " + outputFilePath);
            Log.i("VideoCompress", "------>> onSuccess Video compression complete. Output file: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("VideoCompress", "------>> Exception " + e);
        } catch (Exception e) {
            Log.i("VideoCompress", "------>> Exception " + e);
        }
    }

    private static int selectVideoTrack(MediaExtractor extractor) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }
}
