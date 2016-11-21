package com.amosyuen.videorecorder.util;


import android.hardware.Camera.CameraInfo;
import android.net.Uri;

import com.amosyuen.videorecorder.audio.AudioRecorderParams;
import com.amosyuen.videorecorder.util.FFmpegRecorderParams.Builder;
import com.amosyuen.videorecorder.video.ImageSize.ScaleType;
import com.amosyuen.videorecorder.video.VideoFrameRecorderParams;
import com.amosyuen.videorecorder.video.VideoFrameTransformerParams;

import java.io.File;
import java.io.Serializable;

/**
 * Parameters for the recording video.
 */
public interface VideoRecorderParams extends
        AudioRecorderParams,
        FFmpegRecorderParams,
        VideoFrameRecorderParams,
        VideoFrameTransformerParams,
        Serializable {

    long getMinRecordingTimeNanos();
    /**
     * Max recording length in nano seconds. A value of 0 allows infinite length.
     */
    long getMaxRecordingTimeNanos();

    String getVideoOutputUri();
    /**
     * If no value is set, no thumbnail will be generated.
     */
    String getVideoThumbnailOutputUri();

    class Builder extends FFmpegRecorderParams.Builder {
        private int videoCameraFacing = CameraInfo.CAMERA_FACING_BACK;
        private long minRecordingTimeNanos;
        private long maxRecordingTimeNanos;
        private ScaleType videoScaleType = ScaleType.FILL;
        private boolean canUpscaleVideo;
        private String videoOutputUri;
        private String videoThumbnailOutputUri;

        public Builder(File videoOutputFile) {
            this(Uri.fromFile(videoOutputFile));
        }

        public Builder(Uri videoOutputUri) {
            this(videoOutputUri.toString());
        }

        public Builder(String videoOutputUri) {
            this.videoOutputUri = videoOutputUri;
        }

        public Builder merge(VideoRecorderParams copy) {
            super.merge(copy);
            videoCameraFacing = copy.getVideoCameraFacing();
            minRecordingTimeNanos = copy.getMinRecordingTimeNanos();
            maxRecordingTimeNanos = copy.getMaxRecordingTimeNanos();
            videoScaleType = copy.getVideoScaleType();
            canUpscaleVideo = copy.canUpscaleVideo();
            videoOutputUri = copy.getVideoOutputUri();
            videoThumbnailOutputUri = copy.getVideoThumbnailOutputUri();
            return this;
        }

        @Override
        public Builder videoBitrate(int val) {
            super.videoBitrate(val);
            return this;
        }

        public Builder videoCameraFacing(int val) {
            videoCameraFacing = val;
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

        public Builder minRecordingTimeNanos(long val) {
            minRecordingTimeNanos = val;
            return this;
        }

        public Builder maxRecordingTimeNanos(long val) {
            maxRecordingTimeNanos = val;
            return this;
        }

        public Builder videoScaleType(ScaleType val) {
            videoScaleType = val;
            return this;
        }

        public Builder canUpscaleVideo(boolean val) {
            canUpscaleVideo = val;
            return this;
        }

        public Builder videoThumbnailOutput(File file) {
            videoThumbnailOutputUri = Uri.fromFile(file).toString();
            return this;
        }

        public Builder videoThumbnailOutputUri(Uri uri) {
            videoThumbnailOutputUri = uri.toString();
            return this;
        }

        public Builder videoThumbnailOutputUri(String val) {
            videoThumbnailOutputUri = val;
            return this;
        }

        @Override
        public VideoRecorderParams build() { return new VideoRecorderParamsImpl(this); }

        static class VideoRecorderParamsImpl extends FFmpegRecorderParamsImpl
                implements VideoRecorderParams {
            private final int videoCameraFacing;
            private final long minRecordingTimeNanos;
            private final long maxRecordingTimeNanos;
            private final ScaleType videoScaleType;
            private final boolean canUpscaleVideo;
            private final String videoOutputUri;
            private final String videoThumbnailOutputUri;

            protected VideoRecorderParamsImpl(VideoRecorderParams.Builder builder) {
                super(builder);
                videoCameraFacing = builder.videoCameraFacing;
                minRecordingTimeNanos = builder.minRecordingTimeNanos;
                maxRecordingTimeNanos = builder.maxRecordingTimeNanos;
                videoScaleType = builder.videoScaleType;
                canUpscaleVideo = builder.canUpscaleVideo;
                videoOutputUri = builder.videoOutputUri;
                videoThumbnailOutputUri = builder.videoThumbnailOutputUri;
            }

            @Override
            public String getVideoThumbnailOutputUri() {
                return videoThumbnailOutputUri;
            }

            @Override
            public int getVideoCameraFacing() {
                return videoCameraFacing;
            }

            @Override
            public long getMinRecordingTimeNanos() {
                return minRecordingTimeNanos;
            }

            @Override
            public long getMaxRecordingTimeNanos() {
                return maxRecordingTimeNanos;
            }

            @Override
            public ScaleType getVideoScaleType() {
                return videoScaleType;
            }

            @Override
            public boolean canUpscaleVideo() {
                return canUpscaleVideo;
            }

            @Override
            public String getVideoOutputUri() {
                return videoOutputUri;
            }
        }
    }
}