package com.amosyuen.videorecorder.recorder.params;

import com.google.common.base.Preconditions;

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

        public static <T extends VideoFrameRateParamsI> T validateOnlyClass(T params) {
            Preconditions.checkState(params.getVideoFrameRate().or(1) > 0);
            return params;
        }

        private Builder() {}
    }
}
