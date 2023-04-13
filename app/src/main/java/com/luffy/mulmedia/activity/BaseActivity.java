package com.luffy.mulmedia.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.luffyxu.opengles.base.egl.FileUtils;

import java.io.FileDescriptor;

public abstract class BaseActivity  extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    protected Uri path;
    protected FileDescriptor fileDescriptor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "ACTIVITY " + getClass().getSimpleName() + " START");
        super.onCreate(savedInstanceState);
        Uri contentUri = getIntent().getParcelableExtra("uri");
        if (contentUri != null) {
            String realPath = FileUtils.getPath(getApplicationContext(), contentUri);
            path = Uri.parse(realPath);
            Log.d(TAG, "path:" + path);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        onUriAction(path);
//        onUriAction(fileDescriptor);
    }

    protected abstract void onUriAction(Uri uri);

    protected abstract void onUriAction(FileDescriptor uri);

    protected void startPlayback() {

    }

    private void startFileBrowser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            Uri uri = data.getData();
            Log.d(TAG,"onActivityResult " + uri);
            path = uri;
            fileDescriptor = FileUtils.getFilePath(this,path);
        }
    }
}
