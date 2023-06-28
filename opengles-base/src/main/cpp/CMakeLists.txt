# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
# Declares and names the project.
project("opengles-base")
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        opengles-base

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        egl/GLESRender.cpp
        egl/RenderState.h
        render/IDrawer.cpp
        utils/logger.h
        JniImpl.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(log-lib log)
find_library(libegl EGL)
find_library(libGLESV3 GLESv3)
find_library(libnativewindow nativewindow)
find_library(libandroid android)
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        opengles-base

        # Links the target library to the log library
        # included in the NDK.

        android

        ${libegl}

        ${libnativewindow}

        ${log-lib}
        )