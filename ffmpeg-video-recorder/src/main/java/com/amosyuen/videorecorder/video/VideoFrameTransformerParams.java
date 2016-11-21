package com.amosyuen.videorecorder.video;


/**
 * Parameters for transforming video frames.
 */
public interface VideoFrameTransformerParams {

    /**
     * How the video should be scaled to the desired width and height. Only used if one of
     * videoWidth or videoHeight are non-zero.
     */
    ImageSize.ScaleType getVideoScaleType();

    /**
     * Whether to allow upscaling. If set to false, if the video would be scaled up to fit the
     * desired video dimensions, then no scaling will be preformed. The image will be padded to the
     * desired size instead.
     */
    boolean canUpscaleVideo();

    /** Desired video width. A value of 0 will not do any scaling for the width. */
    int getVideoWidth();

    /** Desired video height. A value of 0 will not do any scaling for the height. */
    int getVideoHeight();

    class Builder {
        private ImageSize.ScaleType videoScaleType = ImageSize.ScaleType.FILL;
        private boolean canUpscaleVideo;
        private int videoWidth;
        private int videoHeight;

        public Builder() {}

        public Builder merge(VideoFrameTransformerParams copy) {
            videoScaleType = copy.getVideoScaleType();
            canUpscaleVideo = copy.canUpscaleVideo();
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

        public VideoFrameTransformerParams build() {
            return new VideoFrameTransformerParamsImpl(this);
        }

        static class VideoFrameTransformerParamsImpl implements VideoFrameTransformerParams {
            private final ImageSize.ScaleType videoScaleType;
            private final boolean canUpscaleVideo;
            private final int videoWidth;
            private final int videoHeight;

            protected VideoFrameTransformerParamsImpl(Builder builder) {
                videoScaleType = builder.videoScaleType;
                canUpscaleVideo = builder.canUpscaleVideo;
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