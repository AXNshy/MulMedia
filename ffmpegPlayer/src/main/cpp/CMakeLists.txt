# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/app/src/main/jniLibs/${ANDROID_ABI})

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
set(ffmpeg_lib_dir ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

set(ffmpeg_head_dir ${CMAKE_SOURCE_DIR})

# 2. 添加头文件目录
include_directories(${ffmpeg_head_dir}/include)
# Declares and names the project.

project("NPlayer")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library(avcodec SHARED IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libavcodec.so)

add_library(avdevice SHARED IMPORTED)
set_target_properties(avdevice PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libavdevice.so)

add_library(avfilter SHARED IMPORTED)
set_target_properties( avfilter PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libavfilter.so)

add_library(avformat SHARED IMPORTED)
set_target_properties( avformat PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libavformat.so)

add_library(avutil SHARED IMPORTED)
set_target_properties( avutil PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libavutil.so)

add_library(swresample SHARED IMPORTED)
set_target_properties( swresample PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libswresample.so)

add_library(swscale SHARED IMPORTED)
set_target_properties(swscale PROPERTIES IMPORTED_LOCATION ${ffmpeg_lib_dir}/libswscale.so)


include_directories(${ffmpeg_head_dir}/include)

add_library( # Sets the name of the library.
        NPlayer
        # Sets the library as a shared library.
        SHARED


        # Provides a relative path to your source file(s).
        media/decoder/base_decoder.cpp
        media/decoder/decode_state.h
        media/decoder/i_decoder.h
        media/decoder/video/v_decoder.cpp
        media/decoder/audio/a_decoder.cpp
        media/render/video/video_render.h
        media/render/audio/audio_render.h
        media/render/video/native_render/native_render.cpp
        media/render/audio/opensl/opensl_render.cpp
        media/one_frame.h

        media/ffmpeg_player.cpp
        media/ffmpeg_player.h

        utils/logger.h
        utils/timer.c

        NPlayer.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(log-lib log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#set_property(TARGET nativelib PROPERTY IMPORTED_NO_SONAME 1)
#set_target_properties(native-lib PROPERTIES IMPORTED_NO_SONAME 1)

target_link_libraries(NPlayer

        # ffmpeg so
        avutil
        swresample
        avcodec
        avfilter
        swscale
        avformat
        avdevice

        -landroid
        OpenSLES
        nativewindow
        ${log-lib} )