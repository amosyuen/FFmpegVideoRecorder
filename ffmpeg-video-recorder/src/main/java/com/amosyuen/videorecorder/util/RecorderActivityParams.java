package com.amosyuen.videorecorder.util;


import android.net.Uri;
import android.provider.MediaStore.Video;
import android.support.annotation.ColorInt;

import com.amosyuen.videorecorder.video.ImageSize.ScaleType;

import java.io.File;
import java.io.Serializable;

/**
 * Parameters for the video recorder activity.
 */
public interface RecorderActivityParams
        extends ActivityThemeParams, VideoRecorderParams, Serializable {

    @ColorInt int getProgressCursorColor();
    @ColorInt int getProgressMinProgressColor();
    @ColorInt int getWidgetColor();

    /**
     * How long in milliseconds that the focus for a tap should be held before reverting back to
     * automatic focus.
     */
    long getTapToFocusHoldTimeMillis();

    final class Builder extends VideoRecorderParams.Builder {
        @ColorInt private int statusBarColor;
        @ColorInt private int toolbarColor;
        @ColorInt private int toolbarWidgetColor;
        @ColorInt private int progressColor;
        @ColorInt private int progressCursorColor;
        @ColorInt private int progressMinProgressColor;
        @ColorInt private int widgetColor;
        private long tapToFocusHoldTimeMillis = 5000;

        public Builder(File videoOutputFile) {
            super(videoOutputFile);
        }

        public Builder(Uri videoOutputUri) {
            super(videoOutputUri);
        }

        public Builder(String videoOutputUri) {
            super(videoOutputUri);
        }

        public Builder merge(RecorderActivityParams copy) {
            super.merge(copy);
            statusBarColor = copy.getStatusBarColor();
            toolbarColor = copy.getToolbarColor();
            toolbarWidgetColor = copy.getToolbarWidgetColor();
            progressColor = copy.getProgressColor();
            progressCursorColor = copy.getProgressCursorColor();
            progressMinProgressColor = copy.getProgressMinProgressColor();
            widgetColor = copy.getWidgetColor();
            tapToFocusHoldTimeMillis = copy.getTapToFocusHoldTimeMillis();
            return this;
        }
    
        @Override
        public Builder videoBitrate(int val) {
            super.videoBitrate(val);
            return this;
        }
    
        @Override
        public Builder videoCameraFacing(int val) {
            super.videoCameraFacing(val);
            return this;
        }
    
        @Override
        public Builder videoCodec(int val) {
            super.videoCodec(val);
            return this;
        }

        @Override
        public Builder videoCodec(VideoCodec val) {
            super.videoCodec(val);
            return this;
        }
    
        @Override
        public Builder videoFrameRate(int val) {
            super.videoFrameRate(val);
            return this;
        }
    
        @Override
        public Builder videoQuality(int val) {
            super.videoQuality(val);
            return this;
        }
    
        @Override
        public Builder videoWidth(int val) {
            super.videoWidth(val);
            return this;
        }
    
        @Override
        public Builder videoHeight(int val) {
            super.videoHeight(val);
            return this;
        }
    
        @Override
        public Builder audioBitrate(int val) {
            super.audioBitrate(val);
            return this;
        }
    
        @Override
        public Builder audioChannelCount(int val) {
            super.audioChannelCount(val);
            return this;
        }
    
        @Override
        public Builder audioCodec(int val) {
            super.audioCodec(val);
            return this;
        }

        @Override
        public Builder audioCodec(AudioCodec val) {
            super.audioCodec(val);
            return this;
        }
    
        @Override
        public Builder audioQuality(int val) {
            super.audioQuality(val);
            return this;
        }
    
        @Override
        public Builder audioSamplingRateHz(int val) {
            super.audioSamplingRateHz(val);
            return this;
        }
    
        @Override
        public Builder videoOutputFormat(String val) {
            super.videoOutputFormat(val);
            return this;
        }
    
        @Override
        public Builder minRecordingTimeNanos(long val) {
            super.minRecordingTimeNanos(val);
            return this;
        }
    
        @Override
        public Builder maxRecordingTimeNanos(long val) {
            super.maxRecordingTimeNanos(val);
            return this;
        }
    
        @Override
        public Builder videoScaleType(ScaleType val) {
            super.videoScaleType(val);
            return this;
        }
    
        @Override
        public Builder canUpscaleVideo(boolean val) {
            super.canUpscaleVideo(val);
            return this;
        }
    
        @Override
        public Builder videoThumbnailOutput(File file) {
            super.videoThumbnailOutputUri(Uri.fromFile(file).toString());
            return this;
        }
    
        @Override
        public Builder videoThumbnailOutputUri(Uri uri) {
            super.videoThumbnailOutputUri(uri.toString());
            return this;
        }
    
        @Override
        public Builder videoThumbnailOutputUri(String val) {
            super.videoThumbnailOutputUri(val);
            return this;
        }

        public Builder statusBarColor(int val) {
            statusBarColor = val;
            return this;
        }

        public Builder toolbarColor(int val) {
            toolbarColor = val;
            return this;
        }

        public Builder toolbarWidgetColor(int val) {
            toolbarWidgetColor = val;
            return this;
        }

        public Builder progressColor(int val) {
            progressColor = val;
            return this;
        }

        public Builder progressCursorColor(int val) {
            progressCursorColor = val;
            return this;
        }

        public Builder progressMinProgressColor(int val) {
            progressMinProgressColor = val;
            return this;
        }

        public Builder widgetColor(int val) {
            widgetColor = val;
            return this;
        }

        public Builder tapToFocusHoldTimeMillis(long val) {
            tapToFocusHoldTimeMillis = val;
            return this;
        }

        @Override
        public RecorderActivityParams build() { return new RecorderActivityParamsImpl(this); }

        static class RecorderActivityParamsImpl extends VideoRecorderParamsImpl implements RecorderActivityParams {

            @ColorInt private final int statusBarColor;
            @ColorInt private final int toolbarColor;
            @ColorInt private final int toolbarWidgetColor;
            @ColorInt private final int progressColor;
            @ColorInt private final int progressCursorColor;
            @ColorInt private final int progressMinProgressColor;
            @ColorInt private final int widgetColor;
            private final long tapToFocusHoldTimeMillis ;

            protected RecorderActivityParamsImpl(RecorderActivityParams.Builder builder) {
                super(builder);
                statusBarColor = builder.statusBarColor;
                toolbarColor = builder.toolbarColor;
                toolbarWidgetColor = builder.toolbarWidgetColor;
                progressColor = builder.progressColor;
                progressCursorColor = builder.progressCursorColor;
                progressMinProgressColor = builder.progressMinProgressColor;
                widgetColor = builder.widgetColor;
                tapToFocusHoldTimeMillis = builder.tapToFocusHoldTimeMillis;
            }

            @Override
            @ColorInt
            public int getStatusBarColor() {
                return statusBarColor;
            }

            @Override
            @ColorInt
            public int getToolbarColor() {
                return toolbarColor;
            }

            @Override
            @ColorInt
            public int getToolbarWidgetColor() {
                return toolbarWidgetColor;
            }

            @Override
            @ColorInt
            public int getProgressColor() {
                return progressColor;
            }

            @Override
            @ColorInt
            public int getProgressCursorColor() {
                return progressCursorColor;
            }

            @Override
            @ColorInt
            public int getProgressMinProgressColor() {
                return progressMinProgressColor;
            }

            @Override
            @ColorInt
            public int getWidgetColor() {
                return widgetColor;
            }

            @Override
            public long getTapToFocusHoldTimeMillis() {
                return tapToFocusHoldTimeMillis;
            }
        }
    }
}