package com.amosyuen.videorecorder.recorder.params;

import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;

/**
 * Common utils for {@link VideoScaleParamsI}.
 */
public final class VideoScaleParams {

    private VideoScaleParams() {}

    public static final class Builder {

        public static <T extends VideoScaleParamsI.BuilderI<T>> T setOnlyClassDefaults(T builder) {
            return builder
                    .setVideoImageFit(ImageFit.FILL)
                    .setVideoImageScale(ImageScale.DOWNSCALE);
        }

        public static <T extends VideoScaleParamsI.BuilderI<T>> T setDefaults(T builder) {
            VideoSizeParams.Builder.setOnlyClassDefaults(builder);
            return setOnlyClassDefaults(builder);
        }

        public static <T extends VideoScaleParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, VideoScaleParamsI params) {
            return builder
                    .setVideoImageFit(params.getVideoImageFit())
                    .setVideoImageScale(params.getVideoImageScale());
        }

        public static <T extends VideoScaleParamsI.BuilderI<T>> T merge(
                T builder, VideoScaleParamsI params) {
            VideoSizeParams.Builder.mergeOnlyClass(builder, params);
            return mergeOnlyClass(builder, params);
        }

        private Builder() {}
    }
}
