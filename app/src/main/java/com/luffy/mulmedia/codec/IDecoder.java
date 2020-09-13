package com.luffy.mulmedia.codec;

public interface IDecoder extends Runnable {
    boolean init();

    void pause();

    void goOn();

    void stop();

    void release();

    boolean isDecoding();

    boolean isSeeking();

    boolean isStop();

    void setStateListener(IDecoderStateListener listener);

    int getWidth();

    int getHeight();

    long getDuration();

    long getCurrentPosition();

    int getRotationAngle();

    int getTrack();

    String getFilePath();
}
