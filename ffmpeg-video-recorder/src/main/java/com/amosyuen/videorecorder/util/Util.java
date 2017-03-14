package com.amosyuen.videorecorder.util;


import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.util.TypedValue;

import com.amosyuen.videorecorder.recorder.FFmpegFrameRecorder;
import com.amosyuen.videorecorder.recorder.MediaClipsRecorder;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.EncoderParams;
import com.amosyuen.videorecorder.recorder.params.RecorderParams;
import com.google.common.base.Preconditions;

import java.io.File;

/**
 * General utilities
 */
public class Util {

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

    public static void setMediaRecorderParams(
            MediaRecorder mediaRecorder,
            RecorderParams params,
            long millisRecorded,
            long bytesRecorded) {
        // Sources must be set first
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Output format second
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioChannels(params.getAudioChannelCount());
        mediaRecorder.setAudioEncodingBitRate(params.getAudioBitrate());
        mediaRecorder.setAudioSamplingRate(params.getAudioSamplingRateHz());

        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(params.getVideoBitrate());

        if (params.getMaxRecordingMillis() > 0) {
            int remainingMillis = (int)(params.getMaxRecordingMillis() - millisRecorded);
            Preconditions.checkArgument(remainingMillis > 0);
            mediaRecorder.setMaxDuration(remainingMillis);
        }
        if (params.getMaxFileSizeBytes() > 0) {
            int remainingBytes = (int)(params.getMaxFileSizeBytes() - bytesRecorded);
            Preconditions.checkArgument(remainingBytes > 0);
            mediaRecorder.setMaxFileSize(remainingBytes);
        }
    }

    public static FFmpegFrameRecorder createFrameRecorder(
            File outputFile, EncoderParams params) {
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, params.getAudioChannelCount());

        recorder.setVideoBitrate(params.getVideoBitrate());
        recorder.setVideoCodec(params.getVideoCodec());
        recorder.setFrameRate(params.getVideoFrameRate());

        recorder.setAudioBitrate(params.getAudioBitrate());
        recorder.setAudioCodec(params.getAudioCodec());
        recorder.setSampleRate(params.getAudioSamplingRateHz());

        recorder.setFormat(params.getVideoOutputFormat());

        return recorder;
    }
}
