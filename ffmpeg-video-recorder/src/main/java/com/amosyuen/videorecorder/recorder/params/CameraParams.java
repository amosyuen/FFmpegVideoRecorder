package com.amosyuen.videorecorder.recorder.params;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

/**
 * Params for the camera.
 */
@AutoValue
public abstract class CameraParams implements CameraParamsI {

    protected CameraParams() {}

    @Override
    public abstract CameraControllerI.Facing getVideoCameraFacing();

    @Override
    public abstract Optional<Integer> getVideoFrameRate();

    @Override
    public abstract ImageSize getVideoSize();

    @Override
    public abstract ImageFit getVideoImageFit();

    @Override
    public abstract ImageScale getVideoImageScale();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return CameraParams.Builder.<Builder>setDefaults(new AutoValue_CameraParams.Builder());
    }

    @AutoValue.Builder
    public abstract static class Builder implements CameraParamsI.BuilderI<Builder> {

        public static <T extends CameraParamsI.BuilderI<T>> T setOnlyClassDefaults(T builder) {
            return builder.setVideoCameraFacing(CameraControllerI.Facing.BACK);
        }

        public static <T extends CameraParamsI.BuilderI<T>> T setDefaults(T builder) {
            VideoFrameRateParams.Builder.setOnlyClassDefaults(builder);
            VideoScaleParams.Builder.setOnlyClassDefaults(builder);
            VideoSizeParams.Builder.setOnlyClassDefaults(builder);
            return setOnlyClassDefaults(builder);
        }

        public static <T extends CameraParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, CameraParamsI params) {
            return builder.setVideoCameraFacing(params.getVideoCameraFacing());
        }

        public static <T extends CameraParamsI.BuilderI<T>> T merge(
                T builder, CameraParamsI params) {
            VideoFrameRateParams.Builder.mergeOnlyClass(builder, params);
            VideoScaleParams.Builder.mergeOnlyClass(builder, params);
            VideoSizeParams.Builder.mergeOnlyClass(builder, params);
            return mergeOnlyClass(builder, params);
        }

        protected Builder() {}

        @Override
        public abstract Builder setVideoCameraFacing(CameraControllerI.Facing val);

        @Override
        public Builder setVideoFrameRate(int val) {
            setVideoFrameRate(Optional.of(val));
            return this;
        }
        @Override
        public abstract Builder setVideoFrameRate(Optional<Integer> val);

        @Override
        public abstract Builder setVideoSize(ImageSize imageSize);

        @Override
        public abstract Builder setVideoImageScale(ImageScale val);

        @Override
        public abstract Builder setVideoImageFit(ImageFit val);

        @Override
        public abstract CameraParams build();
    }
}
