package com.luffy.mulmedia.activity;

import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.luffy.mulmedia.IVideoListener;
import com.luffy.mulmedia.R;
import com.luffy.mulmedia.codec.AudioDecoder;
import com.luffy.mulmedia.codec.DecoderStateListener;
import com.luffy.mulmedia.codec.VideoDecoder;
import com.luffy.mulmedia.egl.CustomGLRender;
import com.luffyxu.base.activity.BaseActivity;
import com.luffyxu.gles2.DragSurfaceView;
import com.luffyxu.gles2.ReliefFilter;
import com.luffyxu.gles2.ReliefVideoDrawer;
import com.luffyxu.opengles.base.egl.TextureCallback;

import java.io.FileDescriptor;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EGLVideoActivity extends BaseActivity {

    private volatile boolean render;

    private Button playBtn;
    private DragSurfaceView eglSurfaceView;
    private Surface mSurface;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    VideoDecoder mVideoDecoder;
    AudioDecoder mAudioDecoder;
    private CustomGLRender mVideoRender;
    private ReliefVideoDrawer mVideoDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl);

        eglSurfaceView = findViewById(R.id.gl_surfaceview);

        mVideoDrawer = new ReliefVideoDrawer();
        mVideoDrawer.setShader(new ReliefFilter(this));
        eglSurfaceView.setDrawer(mVideoDrawer);
        mVideoDrawer.setCallback(new TextureCallback() {
            @Override
            public void texture(SurfaceTexture surface) {
                mSurface = new Surface(surface);
            }
        });
        mVideoRender = new CustomGLRender(Collections.singletonList(mVideoDrawer), 2);
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

        mAudioDecoder = new AudioDecoder(path.toString());
        mAudioDecoder.setStateListener(new DecoderStateListener());
        mExecutor.execute(mVideoDecoder);
        mExecutor.execute(mAudioDecoder);
    }

    private void playVideo() {
        mVideoDecoder.goOn();
        mAudioDecoder.goOn();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String findPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null);
        if (cursor != null) {
            while (cursor.moveToFirst()) {
//                String file = cursor.getString(MediaStore.Video.VideoColumns.);
            }
        }
        return null;
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
