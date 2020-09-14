package com.luffy.mulmedia.muxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;

import androidx.core.os.EnvironmentCompat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MMuxer {
    private static final String TAG = "MMuxer";

    private MediaMuxer mMuxer;

    private String mPath;

    private int mVideoTrackIndex = -1;

    private int mAudioTrackIndex = -1;

    private volatile boolean mIsVideoTrackAdd = false;

    private volatile boolean mIsAudioTrackAdd = false;

    private boolean mIsStart = false;

    public MMuxer(String path) {
        String fileName = "Video_" + new SimpleDateFormat("yyyyMM_dd-HHmmss").format(new Date()) + ".mp4";
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/";
//        mPath = filePath + fileName;
        mPath = path;
        try {
            mMuxer = new MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void addVideoTrack(MediaFormat mediaFormat) {
        Log.d(TAG, "addVideoTrack " + mediaFormat);
        if (mMuxer != null) {
            Log.d(TAG, "video mime : " + mediaFormat.getString(MediaFormat.KEY_MIME));
            try {
                mVideoTrackIndex = mMuxer.addTrack(mediaFormat);
                mIsVideoTrackAdd = true;
                startMuxer();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void addAudioTrack(MediaFormat mediaFormat) {
        Log.d(TAG, "addAudioTrack " + mediaFormat);
        if (mMuxer != null) {
            Log.d(TAG, "audio mime : " + mediaFormat.getString(MediaFormat.KEY_MIME));
            if ("audio/mpeg".equals(mediaFormat.getString(MediaFormat.KEY_MIME))) {
                setNoAudio();
                return;
            }
            try {
                mAudioTrackIndex = mMuxer.addTrack(mediaFormat);
                mIsAudioTrackAdd = true;
                startMuxer();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void setNoVideo() {
        Log.d(TAG, "setNoVideo");
        if (mIsVideoTrackAdd) return;
        mIsVideoTrackAdd = true;
        startMuxer();
    }

    public void setNoAudio() {
        Log.d(TAG, "setNoAudio");
        if (mIsAudioTrackAdd) return;
        mIsAudioTrackAdd = true;
        startMuxer();
    }


    private void startMuxer() {
        if (mIsVideoTrackAdd && mIsAudioTrackAdd) {
            mMuxer.start();
            mIsStart = true;
            Log.d(TAG, "start muxer");
        }
    }

    public void writeVideoData(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mIsStart && mVideoTrackIndex >= 0) {
            mMuxer.writeSampleData(mVideoTrackIndex, byteBuffer, bufferInfo);
        }
    }

    public void writeAudioData(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        if (mIsStart && mAudioTrackIndex >= 0) {
            mMuxer.writeSampleData(mAudioTrackIndex, byteBuffer, bufferInfo);
        }
    }

    public void release() {
        mIsAudioTrackAdd = false;
        mIsVideoTrackAdd = false;
        mMuxer.stop();
        mMuxer.release();
        mMuxer = null;
        Log.d(TAG, "Muxer exit");
    }
}
