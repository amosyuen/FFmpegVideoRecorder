package com.amosyuen.videorecorder.util;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.util.TypedValue;

import com.getkeepsafe.relinker.ReLinker;

import org.bytedeco.javacpp.Loader;

import java.io.File;

/**
 * General utilities
 */
public class Util {

    private static boolean sIsLoaded = false;

    // Because of a bug where native libraries are not loaded properly, we need to relink them
    // before use.
    // https://github.com/bytedeco/javacpp-presets/wiki/The-UnsatisfiedLinkError-X-File-(a-real-experience)
    public static void loadFFmpegLibraries(Context context) {
        if (!sIsLoaded) {
            try {
                ReLinker.loadLibrary(context, "avcodec");
                ReLinker.loadLibrary(context, "avformat");
                ReLinker.loadLibrary(context, "avutil");
                ReLinker.loadLibrary(context, "postproc");
                ReLinker.loadLibrary(context, "swresample");
                ReLinker.loadLibrary(context, "swscale");
                ReLinker.loadLibrary(context, "jniavcodec");
                ReLinker.loadLibrary(context, "jniavformat");
                ReLinker.loadLibrary(context, "jniavutil");
                ReLinker.loadLibrary(context, "jnipostproc");
                ReLinker.loadLibrary(context, "jniswresample");
                ReLinker.loadLibrary(context, "jniswscale");
                sIsLoaded = true;
            } catch (Exception e) {
                Log.e("FFmpegLibraries", "Error loading libraries", e);
            }
        }
    }

    public static FFmpegFrameRecorder createFrameRecorder(
            File outputFile, FFmpegRecorderParams params) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile,
                params.getVideoWidth(), params.getVideoHeight(), params.getAudioChannelCount());

        recorder.setVideoBitrate(params.getVideoBitrate());
        recorder.setVideoCodec(params.getVideoCodec());
        recorder.setFrameRate(params.getVideoFrameRate());
        recorder.setVideoQuality(params.getVideoQuality());

        recorder.setAudioBitrate(params.getAudioBitrate());
        recorder.setAudioCodec(params.getAudioCodec());
        recorder.setAudioQuality(params.getAudioQuality());
        recorder.setSampleRate(params.getAudioSamplingRateHz());

        recorder.setFormat(params.getVideoOutputFormat());

        return recorder;
    }

    @ColorInt
    public static int getThemeColorAttribute(Resources.Theme theme, @AttrRes int attribute) {
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attribute, typedValue, true);
        return typedValue.data;
    }

    public static Drawable tintDrawable(Drawable drawable, @ColorInt int colorInt) {
        drawable.mutate();
        drawable.setColorFilter(colorInt, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    public static boolean isContextLandscape(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }
}
