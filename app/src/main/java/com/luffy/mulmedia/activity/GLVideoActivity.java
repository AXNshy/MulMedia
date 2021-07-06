package com.luffy.mulmedia.activity;

import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.luffy.mulmedia.IVideoListener;
import com.luffy.mulmedia.R;
import com.luffy.mulmedia.codec.AudioDecoder;
import com.luffy.mulmedia.codec.DecoderStateListener;
import com.luffy.mulmedia.codec.VideoDecoder;
import com.luffy.mulmedia.opengl.DragGLSurfaceView;
import com.luffy.mulmedia.opengl.SimpleRenderer;
import com.luffy.mulmedia.opengl.TextureCallback;
import com.luffy.mulmedia.opengl.VideoDrawer;
import com.luffy.mulmedia.opengl.VideoShader;

import java.io.File;
import java.io.FileDescriptor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GLVideoActivity extends BaseActivity {

    private Button playBtn;
    private DragGLSurfaceView glSurfaceView;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(2);
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

                initPlayer(fileDescriptor,path, mSurface);
            }
        });

//        initPlayer(null);
    }

    @Override
    protected void onUriAction(Uri uri) {

    }

    @Override
    protected void onUriAction(FileDescriptor uri) {

    }

    private void initPlayer(FileDescriptor descriptor, Uri path, Surface surface) {
        mVideoDecoder = new VideoDecoder(path.getPath(), null, surface);
        mVideoDecoder.setStateListener(new DecoderStateListener() {
        });
        mVideoDecoder.setVideoListener(new IVideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height) {
                mVideoDrawer.setVideoSize(width, height);
            }
        });

        mAudioDecoder = new AudioDecoder(descriptor);
        mAudioDecoder.setStateListener(new DecoderStateListener());
        mExecutor.execute(mVideoDecoder);
        mExecutor.execute(mAudioDecoder);
    }

    private void playVideo() {
        mVideoDecoder.goOn();
        mAudioDecoder.goOn();
    }


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
