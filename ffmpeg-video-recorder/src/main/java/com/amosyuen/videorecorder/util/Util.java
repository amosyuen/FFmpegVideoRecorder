package com.amosyuen.videorecorder.util;


import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;

/**
 * General utilities
 */
public class Util {

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
}
