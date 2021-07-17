package com.luffy.mulmedia.codec;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import com.luffy.mulmedia.extractor.AudioExtractor;
import com.luffy.mulmedia.extractor.IExtractor;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;

public class AudioDecoder extends BaseDecoder {
    public static final String TAG = "AudioDecoder";

    public int mSampleRate = -1;

    private int mChannel = -1;

    private int mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack mAudioTrack;

    private short[] mAudioOutTempBuf;

    public AudioDecoder(String mFilePath) {
        super(mFilePath);
    }

    @Override
    public void render(ByteBuffer outputBuffers, MediaCodec.BufferInfo bufferInfo) {
        if (mAudioOutTempBuf.length < (bufferInfo.size / 2)) {
            mAudioOutTempBuf = new short[bufferInfo.size / 2];
        }

        outputBuffers.position(0);
        outputBuffers.asShortBuffer().get(mAudioOutTempBuf, 0, bufferInfo.size / 2);
        mAudioTrack.write(mAudioOutTempBuf, 0, bufferInfo.size / 2);
    }

    @Override
    public void doneDecode() {
        mAudioTrack.stop();
        mAudioTrack.release();
    }

    @Override
    protected boolean configureCodec(MediaCodec codec, MediaFormat format) {
        codec.configure(format, null, null, 0);
        return true;
    }

    @Override
    protected boolean initRender() {
//        int channel = mChannel == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        int channel = AudioFormat.CHANNEL_OUT_STEREO;
        int minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMEncodeBit);
        Log.d(TAG, String.format("AudioTrack param: sampleRate:%d,channel:%d,PCMBit:%d,minBufferSize:%d", mSampleRate, channel, mPCMEncodeBit, minBufferSize));
        mAudioOutTempBuf = new short[minBufferSize / 2];
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mSampleRate,
                channel,
                mPCMEncodeBit,
                minBufferSize,
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();
        return true;
    }

    @Override
    protected void initSpecParams(MediaFormat format) {
        try {
            mChannel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
                mPCMEncodeBit = format.getInteger(MediaFormat.KEY_PCM_ENCODING);
            } else {
                mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT;
            }
            Log.d(TAG, "channel : " + mChannel + ",sampleRate : " + mSampleRate + ",pcmEncodeBit : " + mPCMEncodeBit);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    protected IExtractor initExtractor(String mFilePath) {
        return new AudioExtractor(mFilePath);
    }

    @Override
    protected IExtractor initExtractor(FileDescriptor descriptor) {
        return new AudioExtractor(descriptor);
    }
    @Override
    protected boolean check() {
        return true;
    }
}
