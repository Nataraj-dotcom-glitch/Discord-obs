# Proguard / R8 rules for DARK ALISE OBS
# Keep MediaCodec and MediaProjection native callbacks intact
-keepclassmembers class * {
    @android.media.* <methods>;
}

# Keep Compose models and DataStore serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Coroutines internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# CameraX
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
