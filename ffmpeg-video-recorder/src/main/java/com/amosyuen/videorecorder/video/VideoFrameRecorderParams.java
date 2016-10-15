package com.amosyuen.videorecorder.video;


import android.hardware.Camera.CameraInfo;

import java.io.Serializable;

/**
 * Params for video frame only (no audio) recording.
 */
public interface VideoFrameRecorderParams extends VideoFrameTransformerParams, Serializable {

    int getVideoCameraFacing();
    int getVideoFrameRate();

    class Builder {
        private ImageSize.ScaleType videoScaleType = ImageSize.ScaleType.FILL;
        private boolean canUpscaleVideo;
        private int videoCameraFacing = CameraInfo.CAMERA_FACING_BACK;
        private int videoFrameRate = 24;
        private int videoWidth;
        private int videoHeight;

        public Builder() {}

        public Builder merge(VideoFrameRecorderParams copy) {
            videoScaleType = copy.getVideoScaleType();
            canUpscaleVideo = copy.canUpscaleVideo();
            videoCameraFacing = copy.getVideoCameraFacing();
            videoFrameRate = copy.getVideoFrameRate();
            videoWidth = copy.getVideoWidth();
            videoHeight = copy.getVideoHeight();
            return this;
        }

        public Builder videoScaleType(ImageSize.ScaleType val) {
            videoScaleType = val;
            return this;
        }

        public Builder canUpscaleVideo(boolean val) {
            canUpscaleVideo = val;
            return this;
        }

        public Builder videoCameraFacing(int val) {
            videoCameraFacing = val;
            return this;
        }

        public Builder videoFrameRate(int val) {
            videoFrameRate = val;
            return this;
        }

        /**
         * Width will be rounded up to the closest multiple of 2. If height is defined, height will
         * be recalculated to maintain the aspect ratio. Recommended to use a multiple of 16 for
         * best encoding efficiency.
         */
        public Builder videoWidth(int val) {
            videoWidth = val;
            return this;
        }

        /**
         * If only height is specified, the height will be changed to make sure the width is a
         * multiple of 2 while maintain the camera aspect ratio.
         */
        public Builder videoHeight(int val) {
            videoHeight = val;
            return this;
        }

        public VideoFrameRecorderParams build() { return new VideoFrameRecorderParamsImpl(this); }

        static class VideoFrameRecorderParamsImpl implements VideoFrameRecorderParams {
            private final ImageSize.ScaleType videoScaleType;
            private final boolean canUpscaleVideo;
            private final int videoCameraFacing;
            private final int videoFrameRate;
            private final int videoWidth;
            private final int videoHeight;

            protected VideoFrameRecorderParamsImpl(VideoFrameRecorderParams.Builder builder) {
                videoScaleType = builder.videoScaleType;
                canUpscaleVideo = builder.canUpscaleVideo;
                videoCameraFacing = builder.videoCameraFacing;
                videoFrameRate = builder.videoFrameRate;
                videoWidth = builder.videoWidth;
                videoHeight = builder.videoHeight;
            }

            @Override
            public ImageSize.ScaleType getVideoScaleType() {
                return videoScaleType;
            }

            public boolean canUpscaleVideo() {
                return canUpscaleVideo;
            }

            @Override
            public int getVideoCameraFacing() {
                return videoCameraFacing;
            }

            @Override
            public int getVideoFrameRate() {
                return videoFrameRate;
            }

            @Override
            public int getVideoWidth() {
                return videoWidth;
            }

            @Override
            public int getVideoHeight() {
                return videoHeight;
            }
        }
    }
}