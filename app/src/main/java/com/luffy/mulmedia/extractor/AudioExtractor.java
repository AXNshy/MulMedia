package com.luffy.mulmedia.extractor;

import android.media.MediaFormat;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;

public class AudioExtractor implements IExtractor {

    private MExtractor mExtractor;

    public AudioExtractor(String path) {
        mExtractor = new MExtractor(path);
    }

    public AudioExtractor(FileDescriptor descriptor) {
        mExtractor = new MExtractor(descriptor);
    }

    @Override
    public MediaFormat getFormat() {
        return mExtractor.getAudioFormat();
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
    public int getSimpleFlag() {
        return mExtractor.getSimpleFlag();
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
}
