package com.luffy.mulmedia.opengl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.luffy.mulmedia.utils.FileUtils;

public class SoulVideoShader implements IGLShader {
    public static final String TAG = SoulVideoShader.class.getSimpleName();
    private Context context;

    private String vertexShader = null;
    private String fragShader = null;

    public SoulVideoShader(Context context) {
        this.context = context;
    }


    @Override
    public String vertexShader() {
        if (TextUtils.isEmpty(vertexShader)) {
            vertexShader =  FileUtils.getStringFromAssets(context, "shader_vertex_video_soul.glsl");
        }
        Log.d(TAG,"vertexShader :" + vertexShader);
        return vertexShader;
    }

    @Override
    public String fragmentShader() {
        if (TextUtils.isEmpty(fragShader)) {
            fragShader = FileUtils.getStringFromAssets(context, "shader_frag_video_soul.glsl");
        }
        Log.d(TAG,"vertexShader :" + fragShader);
        return fragShader;
    }
}
