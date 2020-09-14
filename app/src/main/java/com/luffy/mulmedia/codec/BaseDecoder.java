package com.luffy.mulmedia.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;

import com.luffy.mulmedia.extractor.IExtractor;

import java.io.File;
import java.nio.ByteBuffer;

public abstract class BaseDecoder implements IDecoder {
    public static final String TAG = "BaseDecoder";

    private boolean mIsRunning = true;

    private Object mLock = new Object();

    private boolean isReadyForDecode = false;

    private MediaCodec mCodec = null;

    private IExtractor mExtractor = null;

    private ByteBuffer[] mInputBuffer;
    private ByteBuffer[] mOutputBuffer;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private DecodeState mState = DecodeState.STOP;

    protected IDecoderStateListener mStateListener = null;

    private boolean mIsEos = false;

    private int mVideoWidth;

    private int mVideoHeight;

    private long mDuration;

    private long mEndPos;

    private String mFilePath;

    /**
     * 从解码开始时系统时间，当解码暂停/恢复时，通过 当前系统时间-当前视频帧的时间戳来重新定位
     */
    private long mStartTimeForAsync = -1l;

    public BaseDecoder(String mFilePath) {
        this.mFilePath = mFilePath;
    }

    @Override
    public void pause() {
        mState = DecodeState.DECODING;
    }

    @Override
    public void goOn() {
        mState = DecodeState.DECODING;
        notifyDecode();
    }

    @Override
    public void stop() {
        mState = DecodeState.STOP;
        mIsRunning = false;
        notifyDecode();
    }

    @Override
    public void release() {
        Log.d(TAG, "release");
        try {
            mState = DecodeState.STOP;
            mIsEos = false;
            if (mExtractor != null) {
                mExtractor.stop();
            }
            if (mCodec != null) {
                mCodec.stop();
                mCodec.release();
            }
            if (mStateListener != null) {
                mStateListener.decoderDestroy(this);
            }

        } catch (Exception e) {

        }
    }

    @Override
    public boolean isDecoding() {
        return mState == DecodeState.DECODING;
    }

    @Override
    public boolean isSeeking() {
        return mState == DecodeState.SEEKING;
    }

    @Override
    public boolean isStop() {
        return mState == DecodeState.STOP;
    }

    @Override
    public void setStateListener(IDecoderStateListener listener) {
        this.mStateListener = listener;
    }

    @Override
    public int getWidth() {
        return mVideoWidth;
    }

    @Override
    public int getHeight() {
        return mVideoHeight;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }


    /**
     * @return 当前帧需要渲染的时间戳
     */
    public long getCurrentTimestamp() {
        return mBufferInfo.presentationTimeUs / 1000;
    }

    @Override
    public int getRotationAngle() {
        return 0;
    }

    @Override
    public int getTrack() {
        return 0;
    }

    @Override
    public String getFilePath() {
        return mFilePath;
    }

    @Override
    public void run() {
        mState = DecodeState.START;
        if (mStateListener != null) {
            mStateListener.decoderPrepared(this);
        }
        if (!init()) return;
        while (mIsRunning) {
            if (mState != DecodeState.START &&
                    mState != DecodeState.DECODING &&
                    mState != DecodeState.SEEKING) {
                Log.d(TAG, "decoder wait");
                waitDecode();

                mStartTimeForAsync = System.currentTimeMillis() - getCurrentTimestamp();
            }

            if (!mIsRunning || mState == DecodeState.STOP) {
                mIsRunning = false;
                Log.d(TAG, "decoder end");
                break;
            }

            if (mStartTimeForAsync == -1l) {
                mStartTimeForAsync = System.currentTimeMillis();
            }

            if (!mIsEos) {
                mIsEos = pushBufferToDecoder();
            }

            int index = pullBufferFromDecoder();
            if (index >= 0) {
                if (mState == DecodeState.DECODING) {
                    sleepRender();
                }
                ByteBuffer byteBuffer = mOutputBuffer[index];
                if (byteBuffer != null) {
                    render(byteBuffer, mBufferInfo);

                    if (mCodec != null) {
                        mCodec.releaseOutputBuffer(index, true);
                    }
                    if (mState == DecodeState.START) {
                        mState = DecodeState.PAUSE;
                    }
                }
            }

            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.d(TAG, "decode finish");
                mState = DecodeState.FINISH;
                if (mStateListener != null) {
                    mStateListener.decoderFinish(this);
                }
            }
        }
        doneDecode();
        release();
    }

    /**
     *
     */
    private void sleepRender() {
        try {
            //计算当前系统流逝的时间
            long passTime = System.currentTimeMillis() - mStartTimeForAsync;
            //获取当前帧时间戳
            long curTime = getCurrentTimestamp();
            if (curTime > passTime) {
                Thread.sleep(curTime - passTime);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected int pullBufferFromDecoder() {

        int index = mCodec.dequeueOutputBuffer(mBufferInfo, 1000);
        if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

        } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {

        } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            mOutputBuffer = mCodec.getOutputBuffers();
        } else {
            return index;
        }

        return -1;
    }

    protected boolean pushBufferToDecoder() {
        int inputBufferIndex = mCodec.dequeueInputBuffer(-1);
        boolean isEndOfStream = false;
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mInputBuffer[inputBufferIndex];
//            if (inputBuffer != null) {
            int sampleSize = mExtractor.readBuffer(inputBuffer);
            if (sampleSize < 0) {
                mCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isEndOfStream = true;
            } else {
                mCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mExtractor.getCurrentTimestamp(), 0);
            }
//            }
        }
        return isEndOfStream;
    }

    protected void waitDecode() {

        try {
            if (mState == DecodeState.PAUSE) {
                if (mStateListener != null) {
                    mStateListener.decoderPaused(this);
                }
            }
            synchronized (mLock) {
                mLock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void notifyDecode() {
        try {
            synchronized (mLock) {
                mLock.notifyAll();
            }
            if (mState == DecodeState.DECODING) {
                if (mStateListener != null) {
                    mStateListener.decoderRunning(this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void render(ByteBuffer outputBuffers, MediaCodec.BufferInfo bufferInfo);

    public abstract void doneDecode();

    @Override
    public boolean init() {
        if (TextUtils.isEmpty(mFilePath)
                || !new File(mFilePath).exists()) {
            Log.d(TAG, "file path is null");
            if (mStateListener != null) {
                mStateListener.decoderError(this, 1000);
            }
            return false;
        }
        if (!check()) {
            return false;
        }
        mExtractor = initExtractor(mFilePath);
        if (mExtractor == null || mExtractor.getFormat() == null) {
            return false;
        }

        if (!initParams()) return false;

        if (!initRender()) return false;

        if (!initCodec()) return false;

        return true;
    }

    protected boolean initCodec() {
        try {
            String type = mExtractor.getFormat().getString(MediaFormat.KEY_MIME);
            mCodec = MediaCodec.createDecoderByType(type);
            if (!configureCodec(mCodec, mExtractor.getFormat())) {
                waitDecode();
            }
            mCodec.start();

            mInputBuffer = mCodec.getInputBuffers();
            mOutputBuffer = mCodec.getOutputBuffers();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            if (mStateListener != null) {
                mStateListener.decoderError(this, 1002);
            }
            return false;
        }
        return true;
    }

    protected abstract boolean configureCodec(MediaCodec codec, MediaFormat format);


    protected abstract boolean initRender();

    protected boolean initParams() {
        try {
            MediaFormat format = mExtractor.getFormat();
            long mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000;
            if (mEndPos == 0l) mEndPos = mDuration;
            initSpecParams(format);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            if (mStateListener != null) {
                mStateListener.decoderError(this, 1001);
            }
            return false;
        }
        return true;
    }

    protected abstract void initSpecParams(MediaFormat format);

    protected abstract IExtractor initExtractor(String mFilePath);

    protected abstract boolean check();
}
