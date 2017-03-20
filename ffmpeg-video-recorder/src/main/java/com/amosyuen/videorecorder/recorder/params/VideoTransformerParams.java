package com.amosyuen.videorecorder.recorder.params;

import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.auto.value.AutoValue;

/**
 * Parameters for transforming video.
 */
@AutoValue
public abstract class VideoTransformerParams implements VideoTransformerParamsI {

    protected VideoTransformerParams() {}

    @Override
    public abstract ImageSize getVideoSize();

    @Override
    public abstract ImageFit getVideoImageFit();

    @Override
    public abstract ImageScale getVideoImageScale();

    @Override
    public abstract boolean getShouldCropVideo();

    @Override
    public abstract boolean getShouldPadVideo();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return VideoTransformerParams.Builder
                .<Builder>setDefaults(new AutoValue_VideoTransformerParams.Builder());
    }

    @AutoValue.Builder
    public abstract static class Builder implements VideoTransformerParamsI.BuilderI<Builder> {

        public static <T extends VideoTransformerParamsI.BuilderI<T>> T setOnlyClassDefaults(
                T builder) {
            return builder;
        }

        public static <T extends VideoTransformerParamsI.BuilderI<T>> T setDefaults(T builder) {
            VideoScaleParams.Builder.setOnlyClassDefaults(builder);
            VideoSizeParams.Builder.setOnlyClassDefaults(builder);
            return setOnlyClassDefaults(builder);
        }

        public static <T extends VideoTransformerParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, VideoTransformerParamsI params) {
            return builder;
        }

        public static <T extends VideoTransformerParamsI.BuilderI<T>> T merge(
                T builder, VideoTransformerParamsI params) {
            VideoScaleParams.Builder.mergeOnlyClass(builder, params);
            VideoSizeParams.Builder.mergeOnlyClass(builder, params);
            return mergeOnlyClass(builder, params);
        }

        protected Builder() {}

        @Override
        public abstract Builder setVideoSize(ImageSize imageSize);

        @Override
        public abstract Builder setVideoImageScale(ImageScale val);

        @Override
        public abstract Builder setVideoImageFit(ImageFit val);

        @Override
        public abstract Builder setShouldCropVideo(boolean val);

        @Override
        public abstract Builder setShouldPadVideo(boolean val);

        public abstract VideoTransformerParams build();
    }
}
