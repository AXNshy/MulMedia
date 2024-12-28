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
import com.luffyxu.base.activity.BaseActivity;
import com.luffyxu.gles2.DragGLSurfaceView;
import com.luffyxu.gles2.SimpleRenderer;
import com.luffyxu.gles2.VideoDrawer;
import com.luffyxu.gles2.VideoShader;
import com.luffyxu.opengles.base.egl.TextureCallback;

import java.io.FileDescriptor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GLVideoActivity extends BaseActivity {

    private Button playBtn;
    private DragGLSurfaceView glSurfaceView;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    VideoDecoder mVideoDecoder;
    AudioDecoder mAudioDecoder;
    private Surface mSurface;
    VideoDrawer mVideoDrawer;


    private int width;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glvideo);

        glSurfaceView = findViewById(R.id.gl_surfaceview);
        glSurfaceView.setEGLContextClientVersion(2);

        mVideoDrawer = new VideoDrawer();
        mVideoDrawer.setShader(new VideoShader(this));
        glSurfaceView.setDrawer(mVideoDrawer);
        mVideoDrawer.setCallback(new TextureCallback() {
            @Override
            public void texture(SurfaceTexture surface) {
                mSurface = new Surface(surface);
            }
        });
        glSurfaceView.setRenderer(new SimpleRenderer(mVideoDrawer));

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
}
