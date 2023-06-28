object Versions {
    const val compileSdk = 31
    const val minSdk = 24
    const val targetSdk = 31
    const val ndkVersion = "25.1.8937393"

    const val kotlin = "1.8.20"
}

object Libs {
    const val appcompat = "androidx.appcompat:appcompat:1.1.0"
    const val recyclerview = "androidx.recyclerview:recyclerview:1.2.0"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"
    const val viewpager2 = "androidx.viewpager2:viewpager2:1.0.0"
    const val core_ktx = "androidx.core:core-ktx:1.1.0"
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val kotlin_stdlib_jdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlinx_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9"
    const val material = "com.google.android.material:material:1.3.0"
    const val lifecycle_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0"
}