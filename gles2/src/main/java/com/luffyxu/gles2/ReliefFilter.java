package com.luffyxu.gles2;

import android.content.Context;

import com.luffyxu.base.utils.FileUtils;
import com.luffyxu.opengles.base.shader.IGLShader;

public class ReliefFilter implements IGLShader {
    private final Context context;

    public ReliefFilter(Context context) {
        this.context = context;
    }

    @Override
    public String vertexShader() {
        return FileUtils.getStringFromAssets(context, "gles2/shader_vertex_video.glsl");
    }

    @Override
    public String fragmentShader() {
        return FileUtils.getStringFromAssets(context, "gles2/shader_frag_video_relief.glsl");
    }
}
