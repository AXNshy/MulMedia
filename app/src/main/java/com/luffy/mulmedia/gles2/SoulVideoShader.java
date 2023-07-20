package com.luffy.mulmedia.gles2;

import android.content.Context;
import android.text.TextUtils;

import com.luffyxu.opengles.base.shader.IGLShader;
import com.luffyxu.opengles.base.utils.FileUtils;

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
            vertexShader =  FileUtils.getStringFromAssets(context, "gles2/shader_vertex_video_soul.glsl");
        }
        return vertexShader;
    }

    @Override
    public String fragmentShader() {
        if (TextUtils.isEmpty(fragShader)) {
            fragShader = FileUtils.getStringFromAssets(context, "gles2/shader_frag_video_soul.glsl");
        }
        return fragShader;
    }
}
