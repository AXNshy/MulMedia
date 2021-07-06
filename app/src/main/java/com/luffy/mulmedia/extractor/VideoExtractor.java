package com.luffy.mulmedia.extractor;

import android.media.MediaFormat;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;

public class VideoExtractor implements IExtractor {

    private MExtractor mExtractor;

    public VideoExtractor(String path) {
        mExtractor = new MExtractor(path);
    }

    public VideoExtractor(FileDescriptor descriptor) {
        mExtractor = new MExtractor(descriptor);
    }


    @Override
    public MediaFormat getFormat() {

        return mExtractor.getVideoFormat();
    }

    @Override
    public int readBuffer(ByteBuffer buffer) {
        return mExtractor.readBuffer(buffer);
    }

    @Override
    public long getCurrentTimestamp() {
        return mExtractor.getCurrentSampleTimestamp();
    }

    @Override
    public void seek(long position) {
        mExtractor.seek(position);
    }

    @Override
    public void setStartPos(long pos) {
        mExtractor.setStartPos(pos);
    }

    @Override
    public void stop() {
        mExtractor.stop();
    }

    @Override
    public int getSimpleFlag() {
        return mExtractor.getSimpleFlag();
    }
}
