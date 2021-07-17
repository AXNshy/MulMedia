package com.luffy.mulmedia.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.luffy.mulmedia.extractor.IExtractor;
import com.luffy.mulmedia.extractor.VideoExtractor;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;

public class VideoDecoder extends BaseDecoder {
    private static final String TAG = "VideoDecoder";

    private SurfaceView mSurfaceView;
    private Surface mSurface;

    public VideoDecoder(String path, SurfaceView mSurfaceView, Surface mSurface) {
        super(path);
        this.mSurfaceView = mSurfaceView;
        this.mSurface = mSurface;
    }

    @Override
    public void render(ByteBuffer outputBuffers, MediaCodec.BufferInfo bufferInfo) {

    }

    @Override
    public void doneDecode() {

    }

    public void setSurface(Surface mSurface) {
        this.mSurface = mSurface;
    }

    @Override
    protected boolean configureCodec(final MediaCodec codec, final MediaFormat format) {
        Log.d(TAG, "configureCodec " + Thread.currentThread().getName());
        if (mSurface != null) {
            codec.configure(format, mSurface, null, 0);
            notifyDecode();
        } else {
            mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback2() {
                @Override
                public void surfaceRedrawNeeded(@NonNull SurfaceHolder holder) {

                }

                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    Log.d(TAG, "surfaceCreated " + Thread.currentThread().getName());
                    mSurface = holder.getSurface();
                    configureCodec(codec, format);
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    Log.d(TAG, "surfaceChanged");
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    Log.d(TAG, "surfaceDestroyed");
                }
            });
            return false;
        }
        return true;
    }

    @Override
    protected boolean initRender() {
        return true;
    }

    @Override
    protected void initSpecParams(MediaFormat format) {
        try {
            int width = format.getInteger(MediaFormat.KEY_WIDTH);
            int height = format.getInteger(MediaFormat.KEY_HEIGHT);
            if (mVideoListener != null) {
                mVideoListener.onVideoSizeChanged(width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected IExtractor initExtractor(String mFilePath) {
        return new VideoExtractor(mFilePath);
    }

    @Override
    protected IExtractor initExtractor(FileDescriptor descriptor) {
        return new VideoExtractor(descriptor);
    }

    @Override
    protected boolean check() {
        if (mSurface == null && mSurfaceView == null) {
            Log.e(TAG, "mSurface and mSurfaceView must has one is not null");
            if (mStateListener != null) {
                mStateListener.decoderError(this, 10001);
            }
            return false;
        }
        return true;
    }
}
