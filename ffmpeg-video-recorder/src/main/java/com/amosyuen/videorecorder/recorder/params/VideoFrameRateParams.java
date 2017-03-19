package com.amosyuen.videorecorder.recorder.params;

/**
 * Common utils for {@link VideoFrameRateParamsI}.
 */
public final class VideoFrameRateParams {

    private VideoFrameRateParams() {}

    public static final class Builder {

        public static <T extends VideoFrameRateParamsI.BuilderI<T>> T setOnlyClassDefaults(
                T builder) {
            return builder;
        }

        public static <T extends VideoFrameRateParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, VideoFrameRateParamsI params) {
            return builder.setVideoFrameRate(params.getVideoFrameRate());
        }

        private Builder() {}
    }
}
