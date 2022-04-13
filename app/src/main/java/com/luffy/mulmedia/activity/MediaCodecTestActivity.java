package com.luffy.mulmedia.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.luffy.mulmedia.IVideoListener;
import com.luffy.mulmedia.R;
import com.luffy.mulmedia.utils.Mp4Repack;
import com.luffy.mulmedia.codec.AudioDecoder;
import com.luffy.mulmedia.codec.DecoderStateListener;
import com.luffy.mulmedia.codec.VideoDecoder;

import java.io.File;
import java.io.FileDescriptor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaCodecTestActivity extends BaseActivity {
    public static final String TAG = "MediaCodecTestActivity";
    private Button selectFileButton;
    private Button repackButton;
    private Button pipButton;
    private TextView fileTv;
    private SurfaceView view;
    private Surface mSurface;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    VideoDecoder mVideoDecoder;
    AudioDecoder mAudioDecoder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = findViewById(R.id.surfaceView);
        view.getHolder().addCallback(new SurfaceHolder.Callback2() {
            @Override
            public void surfaceRedrawNeeded(@NonNull SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                mSurface = holder.getSurface();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//                initPlayer(null);
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

        selectFileButton = findViewById(R.id.btn_file);
        repackButton = findViewById(R.id.btn_repack);
        pipButton = findViewById(R.id.btn_pip);
        pipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterPictureInPictureMode();
            }
        });
        fileTv = findViewById(R.id.tv_file);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                startActivityForResult(intent,1);
                initPlayer(path);

            }
        });

        repackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mp4Repack webpRepack = new Mp4Repack(getMp4Path(), getWebpPath());
                webpRepack.start();
            }
        });
//        initPlayer(null);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG,"dispatchKeyEvent " +event);
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG,"dispatchTouchEvent " +ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onUriAction(Uri uri) {

    }

    @Override
    protected void onUriAction(FileDescriptor uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                Log.d("tag", "文件路径：" + uri.getPath().toString());
                fileTv.setText(uri.toString());
                initPlayer(path);
            }
        }
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

    private void initPlayer(Uri path) {
        mVideoDecoder = new VideoDecoder(path.getPath(), view, mSurface);
        mVideoDecoder.setVideoListener(new IVideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height) {
                Log.d(TAG,"onVideoSizeChanged width:" + width +",height:" +height);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.getHolder().setFixedSize(width,height);
                    }
                });

            }
        });
        mVideoDecoder.setStateListener(new DecoderStateListener());
        mAudioDecoder = new AudioDecoder(path.getPath());
        mAudioDecoder.setStateListener(new DecoderStateListener());
        mExecutor.execute(mVideoDecoder);
        mExecutor.execute(mAudioDecoder);
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