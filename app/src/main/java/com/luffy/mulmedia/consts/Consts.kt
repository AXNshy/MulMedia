package com.luffy.mulmedia.consts

object Consts {
    const val NAV_SELECT = "Select"
    const val NAV_GLES3 = "GLES3.0"
    const val NAV_GLES3_VIDEO = "GLES3.0 Video"
    const val NAV_MEDIACODEC = "MediaCodec"
    const val NAV_GL_VIDEO = "GlSurfaceView播放视频"
    const val NAV_EGL_VIDEO = "EGL播放视频"
    const val NAV_EGL_VIDEO_FILTER = "视频滤镜"
    const val NAV_SURFACEVIEW_VIDEO = "SurfaceView测试"
    const val NAV_MEDIA_INFO = "媒体信息"
    const val NAV_GLES3_LESSION = "GLES3.0课程"
    const val NAV_CAMERA = "相机"

    fun titleArray() = arrayOf(
        NAV_SELECT,
        NAV_GLES3,
        NAV_GLES3_VIDEO,
        NAV_MEDIACODEC,
        NAV_GL_VIDEO,
        NAV_EGL_VIDEO,
        NAV_EGL_VIDEO_FILTER,
        NAV_SURFACEVIEW_VIDEO,
        NAV_MEDIA_INFO,
        NAV_GLES3_LESSION,
        NAV_CAMERA,
    )
}