package com.luffy.mulmedia.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;

import com.luffy.mulmedia.IVideoListener;
import com.luffy.mulmedia.extractor.IExtractor;

import java.io.File;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;


public abstract class BaseDecoder implements IDecoder {
    public final String TAG = this.getClass().getSimpleName();

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

    protected IVideoListener mVideoListener = null;

    private boolean mIsEos = false;

    private int mVideoWidth;

    private int mVideoHeight;

    private long mDuration;

    private long mEndPos;

    private String mFilePath;

    private FileDescriptor descriptor;

    /**
     * 从解码开始时系统时间，当解码暂停/恢复时，通过 当前系统时间-当前视频帧的时间戳来重新定位
     */
    private long mStartTimeForAsync = -1l;

    public BaseDecoder(String mFilePath) {
        this.mFilePath = mFilePath;
    }

    public BaseDecoder(FileDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public void pause() {
        mState = DecodeState.DECODING;
    }

    /*
    * 设置解码器工作状态为开始解码，并通知挂起线程继续工作
    * */
    @Override
    public void goOn() {
        mState = DecodeState.DECODING;
        notifyDecode();
    }
    /*
     * 设置解码器工作状态为停止解码，设置工作线程循环结束标记为false，并通知挂起线程继续工作
     * */
    @Override
    public void stop() {
        mState = DecodeState.STOP;
        mIsRunning = false;
        notifyDecode();
    }

    /*
     * 设置解码器工作状态为停止解码，停止解码器并释放相关资源
     * */
    private void release() {
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

    public void setVideoListener(IVideoListener mVideoListener) {
        this.mVideoListener = mVideoListener;
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
        //设置解码器工作状态为开始
        mState = DecodeState.START;

        mIsRunning = true;
        // 触发goOn接口， 状态设置为解码中
        if (mStateListener != null) {
            mStateListener.decoderPrepared(this);
        }
        //执行初始化操作，如果初始化失败，直接返回结束工作线程
        if (!init()) return;
        //开始解码，进入一个循环 不停的从提取器中提取编码数据放如MediaCodec中去进行解码，
        // 解码完成后再从输出缓冲队列中获取解码完成后的音视频数据，进行音/视频数据的渲染，
        // 等到提取器中所有数据都解码完成后，视频播放完成
        while (mIsRunning) {
            //判断当前解码器状态，如果状态不为开始，解码中，快进中，那么就需要解码器挂起等待
            if (mState != DecodeState.START &&
                    mState != DecodeState.DECODING &&
                    mState != DecodeState.SEEKING) {
                Log.d(TAG, "decoder wait");
                waitDecode();
                //计算挂起线程恢复后真实的视频开始时间戳  time = 当前时间戳-当前解码时间戳
                mStartTimeForAsync = System.currentTimeMillis() - getCurrentTimestamp();
            }
            /**
             * 判断循环标记是否为真
             * 当调用stop停止当前解码任务时，停止工作线程的执行，并释放MediaCodec相关资源
             *
             * 判断解码器状态是否为停止，如果为真，直接跳出循环，结束工作流程
             */
            if (mState == DecodeState.STOP) {
                mIsRunning = false;
                Log.d(TAG, "decoder end");
                break;
            }

            //判断工作时间戳是否没初始化，否则初始化为当前时间
            if (mStartTimeForAsync == -1l) {
                mStartTimeForAsync = System.currentTimeMillis();
            }
            //判断是否数据提取已结束
            if (!mIsEos) {
                //提取数据，交给解码器进行解码
                mIsEos = pushBufferToDecoder();
            }
            //拉取解码后的数据
            int index = pullBufferFromDecoder();
            //如果能获取到解码后的数据
            if (index >= 0) {
                //如果当前状态为解码中，那么需要进行音视频数据速率设置，
                if (mState == DecodeState.DECODING) {
                    sleepRender();
                }
                //获取输出缓冲队列中待解码缓冲数据
                ByteBuffer byteBuffer = mOutputBuffer[index];
                if (byteBuffer != null) {
                    //渲染数据到硬件
                    render(byteBuffer, mBufferInfo);
                    //释放输出缓冲
                    if (mCodec != null) {
                        mCodec.releaseOutputBuffer(index, true);
                    }
                }
            }
            //如果BufferInfo对象中年flag有BUFFER_FLAG_END_OF_STREAM这个流缓冲结束标志
            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                Log.d(TAG, "decode finish");
                //设置解码器工作状态为解码完成
                mState = DecodeState.FINISH;
                //状态回调
                if (mStateListener != null) {
                    mStateListener.decoderFinish(this);
                }
            }
        }
        //解码结束
        doneDecode();
        //释放解码资源
        release();
    }

    /**
     * 进行解码时间戳与真实时间戳的同步
     */
    private void sleepRender() {
        try {
            //计算真实时间从开始播放到现在时间时长
            long passTime = System.currentTimeMillis() - mStartTimeForAsync;
            //获取当前帧时间戳
            long curTime = getCurrentTimestamp();
            if (curTime > passTime) {
                //线程休眠到解码时间戳与真实时间戳相同
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
        Log.d(TAG, "mFilePath : " + mFilePath);
        if ((TextUtils.isEmpty(mFilePath)
                || !new File(mFilePath).exists()) && descriptor == null) {
            Log.d(TAG, "file path is null");
            if (mStateListener != null) {
                mStateListener.decoderError(this, 1000);
            }
            return false;
        }
        if (!check()) {
            return false;
        }
        if(descriptor != null) {
            mExtractor = initExtractor(descriptor);
        }else {
            mExtractor = initExtractor(mFilePath);
        }
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

    protected abstract IExtractor initExtractor(FileDescriptor descriptor);

    protected abstract boolean check();
}
