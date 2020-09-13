package com.luffy.mulmedia.extractor;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.provider.MediaStore;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MExtractor {
    private String path;

    public MExtractor(String path) {
        this.path = path;
        init();
    }

    private MediaExtractor mExtractor;

    private int mAudioTrack = -1;

    private int mVideoTrack = -1;

    private long mCurrentSampleTimestamp = 0;

    private long mStartPos = 0;

    public void init() {
        mExtractor = new MediaExtractor();
        try {
            mExtractor.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaFormat getAudioFormat() {
        if (mExtractor == null) {
            return null;
        }
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                mAudioTrack = i;
                break;
            }
        }
        return mAudioTrack >= 0 ? mExtractor.getTrackFormat(mAudioTrack) : null;
    }


    public MediaFormat getVideoFormat() {
        if (mExtractor == null) {
            return null;
        }
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                mVideoTrack = i;
                break;
            }
        }
        return mVideoTrack >= 0 ? mExtractor.getTrackFormat(mVideoTrack) : null;
    }

    public int readBuffer(ByteBuffer buffer) {
        buffer.clear();
        selectSourceTrack();
        int readSampleCount = mExtractor.readSampleData(buffer, 0);
        if (readSampleCount < 0) return -1;
        mCurrentSampleTimestamp = mExtractor.getSampleTime();
        mExtractor.advance();
        return readSampleCount;
    }

    private void selectSourceTrack() {
        if (mExtractor != null) {
            if (mVideoTrack >= 0) {
                mExtractor.selectTrack(mVideoTrack);
            } else if (mAudioTrack >= 0) {
                mExtractor.selectTrack(mAudioTrack);
            }
        }
    }

    public long seek(long position) {
        if (mExtractor != null) {
            mExtractor.seekTo(position, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
            return mExtractor.getSampleTime();
        }
        return -1;
    }

    public void stop() {
        if (mExtractor != null) {
            mExtractor.release();
        }
        mExtractor = null;
    }

    public int getVideoTrack() {
        return mVideoTrack;
    }

    public int getAudioTrack() {
        return mAudioTrack;
    }

    public void setStartPos(long pos) {
        mStartPos = pos;
    }

    public long getCurrentSampleTimestamp() {
        return mCurrentSampleTimestamp;
    }
}
