package com.luffy.mulmedia.opengl;

import android.content.Context;

import com.luffy.mulmedia.utils.FileUtils;

public class ReliefFilter implements IGLShader {
    private Context context;

    public ReliefFilter(Context context) {
        this.context = context;
    }

    @Override
    public String vertexShader() {
        return FileUtils.getStringFromAssets(context, "shader_vertex_video.glsl");
    }

    @Override
    public String fragmentShader() {
        return FileUtils.getStringFromAssets(context, "shader_frag_video_relief.glsl");
    }
}
