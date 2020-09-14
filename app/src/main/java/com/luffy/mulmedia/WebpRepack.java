package com.luffy.mulmedia;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.luffy.mulmedia.extractor.AudioExtractor;
import com.luffy.mulmedia.extractor.VideoExtractor;
import com.luffy.mulmedia.muxer.MMuxer;

import java.nio.ByteBuffer;

public class WebpRepack {
    public static final String TAG = "Mp4Repack";
    private String path;

    private AudioExtractor mAudioExtractor;
    private VideoExtractor mVideoExtractor;

    private MMuxer mMuxer;

    public WebpRepack(String path, String destPath) {
        this.path = path;
        mAudioExtractor = new AudioExtractor(path);
        mVideoExtractor = new VideoExtractor(path);
        mMuxer = new MMuxer(destPath);
    }

    public void start() {
        final MediaFormat audioFormat = mAudioExtractor.getFormat();

        if (audioFormat == null || "audio/mpeg".equals(audioFormat.getString(MediaFormat.KEY_MIME))) {
            mMuxer.setNoAudio();
        } else {
            mMuxer.addAudioTrack(audioFormat);
        }

        final MediaFormat videoFormat = mVideoExtractor.getFormat();
        if (videoFormat == null) {
            mMuxer.setNoVideo();
        } else {
            mMuxer.addVideoTrack(videoFormat);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                if (audioFormat != null && !"audio/mpeg".equals(audioFormat.getString(MediaFormat.KEY_MIME))) {
                    int size = mAudioExtractor.readBuffer(byteBuffer);
                    while (size > 0) {
                        bufferInfo.set(0, size, mAudioExtractor.getCurrentTimestamp(), mAudioExtractor.getSimpleFlag());

                        mMuxer.writeAudioData(byteBuffer, bufferInfo);
                        size = mAudioExtractor.readBuffer(byteBuffer);
                    }
                }

                if (videoFormat != null) {
                    int size = mVideoExtractor.readBuffer(byteBuffer);
                    while (size > 0) {
//                        Log.d(TAG," timestamp " + mVideoExtractor.getCurrentTimestamp());
                        bufferInfo.set(0, size, mVideoExtractor.getCurrentTimestamp(), mVideoExtractor.getSimpleFlag());

                        mMuxer.writeVideoData(byteBuffer, bufferInfo);
                        size = mVideoExtractor.readBuffer(byteBuffer);
                    }
                }


                mAudioExtractor.stop();
                mVideoExtractor.stop();
                mMuxer.release();
                Log.d(TAG, "pack webp finish");
            }
        }).start();
    }
}
