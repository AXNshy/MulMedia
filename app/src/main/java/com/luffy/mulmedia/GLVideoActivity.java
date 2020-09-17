package com.luffy.mulmedia;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.luffy.mulmedia.codec.AudioDecoder;
import com.luffy.mulmedia.codec.DecoderStateListener;
import com.luffy.mulmedia.codec.VideoDecoder;
import com.luffy.mulmedia.opengl.SimpleRenderer;
import com.luffy.mulmedia.opengl.VideoDrawer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GLVideoActivity extends AppCompatActivity {

    private Button playBtn;
    private GLSurfaceView glSurfaceView;
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
        mVideoDrawer.setCallback(new VideoDrawer.TextureCallback() {
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

                initPlayer(null, mSurface);
            }
        });

//        initPlayer(null);
    }

    private String getMp4Path() {
        File appOwnDic = getExternalFilesDir(null);
        Log.d("tag", "appOwnDic " + appOwnDic.getAbsolutePath());
        String file = appOwnDic.getAbsolutePath() + "/test.mp4";
        return file;
    }

    private String getWebpPath() {
        File appOwnDic = getExternalFilesDir(null);
        Log.d("tag", "appOwnDic " + appOwnDic.getAbsolutePath());
        String file = appOwnDic.getAbsolutePath() + "/new.mp4";
        return file;
    }

    private void initPlayer(Uri path, Surface surface) {
        String file = getMp4Path();
        mVideoDecoder = new VideoDecoder(file, null, surface);
        mVideoDecoder.setStateListener(new DecoderStateListener() {
        });
        mVideoDecoder.setVideoListener(new IVideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height) {
                mVideoDrawer.setVideoSize(width, height);
            }
        });

        mAudioDecoder = new AudioDecoder(file);
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
