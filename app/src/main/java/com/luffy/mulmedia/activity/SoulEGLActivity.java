package com.luffy.mulmedia.activity;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import com.luffy.mulmedia.IVideoListener;
import com.luffy.mulmedia.R;
import com.luffy.mulmedia.codec.AudioDecoder;
import com.luffy.mulmedia.codec.DecoderStateListener;
import com.luffy.mulmedia.codec.VideoDecoder;
import com.luffy.mulmedia.gl.CustomGLRender;
import com.luffy.mulmedia.gles2.DragSurfaceView;
import com.luffy.mulmedia.gles2.IDrawer;
import com.luffy.mulmedia.gles2.SoulVideoDrawer;
import com.luffy.mulmedia.gles2.SoulVideoShader;
import com.luffy.mulmedia.gles2.TextureCallback;

import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoulEGLActivity extends BaseActivity {

    private Button playBtn;
    private DragSurfaceView eglSurfaceView;
    private Surface mSurface;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    VideoDecoder mVideoDecoder;
    AudioDecoder mAudioDecoder;
    private CustomGLRender mVideoRender;
    private SoulVideoDrawer mVideoDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl);

        eglSurfaceView = findViewById(R.id.gl_surfaceview);

        mVideoDrawer = new SoulVideoDrawer();
        eglSurfaceView.setDrawer(mVideoDrawer);
        mVideoDrawer.setShader(new SoulVideoShader(this));
        mVideoDrawer.setCallback(new TextureCallback() {
            @Override
            public void texture(SurfaceTexture surface) {
                mSurface = new Surface(surface);
            }
        });
//        mVideoDrawer.setVideoSize(1920, 1080);
        mVideoRender = new CustomGLRender(Arrays.<IDrawer>asList(mVideoDrawer));
        mVideoRender.setSurfaceView(eglSurfaceView);

        playBtn = findViewById(R.id.btn_file);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPlayer(path, mSurface);
            }
        });
    }

    @Override
    protected void onUriAction(Uri uri) {

    }

    @Override
    protected void onUriAction(FileDescriptor uri) {

    }

    private void initPlayer(Uri path, Surface surface) {
        if (path == null || TextUtils.isEmpty(path.toString())) return;
        mVideoDecoder = new VideoDecoder(path.getPath(), null, surface);
        mVideoDecoder.setStateListener(new DecoderStateListener() {
        });
        mVideoDecoder.setVideoListener(new IVideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height) {
                mVideoDrawer.setVideoSize(width, height);
            }
        });
//        mVideoDrawer.setVideoSize(1920, 1080);

        mAudioDecoder = new AudioDecoder(path.getPath());
        mAudioDecoder.setStateListener(new DecoderStateListener());
        mExecutor.execute(mVideoDecoder);
        mExecutor.execute(mAudioDecoder);
    }

    private void playVideo() {
        mVideoDecoder.goOn();
        mAudioDecoder.goOn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mVideoDecoder != null){
            mVideoDecoder.stop();
        }
        if(mAudioDecoder != null) {
            mAudioDecoder.stop();
        }
    }
}
