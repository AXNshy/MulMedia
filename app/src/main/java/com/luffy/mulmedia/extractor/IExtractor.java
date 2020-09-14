package com.luffy.mulmedia.extractor;

import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface IExtractor {
    MediaFormat getFormat();

    int readBuffer(ByteBuffer buffer);

    long getCurrentTimestamp();

    int getSimpleFlag();

    void seek(long position);

    void setStartPos(long pos);

    void stop();
}
