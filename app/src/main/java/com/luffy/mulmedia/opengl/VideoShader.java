package com.luffy.mulmedia.opengl;

import android.content.Context;
import android.text.TextUtils;

import com.luffy.mulmedia.utils.FileUtils;

public class VideoShader implements IGLShader {
    private Context context;

    private String vertexShader = null;
    private String fragShader = null;

    public VideoShader(Context context) {
        this.context = context;
    }

    @Override
    public String vertexShader() {
        if (TextUtils.isEmpty(vertexShader)) {
            return FileUtils.getStringFromAssets(context, "shader_vertex_video.glsl");
        }
        return vertexShader;
    }

    @Override
    public String fragmentShader() {
        if (TextUtils.isEmpty(fragShader)) {
            fragShader = FileUtils.getStringFromAssets(context, "shader_frag_video.glsl");
        }
        return fragShader;
    }
}
