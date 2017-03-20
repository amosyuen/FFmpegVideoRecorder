package com.amosyuen.videorecorder.recorder.params;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

/**
 * Parameters for recording video.
 */
@AutoValue
public abstract class RecorderParams implements RecorderParamsI {

    protected RecorderParams() {}

    @Override
    public abstract Optional<Integer> getAudioBitrate();

    @Override
    public abstract Optional<Integer> getAudioChannelCount();

    @Override
    public abstract AudioCodec getAudioCodec();

    @Override
    public abstract Optional<Integer> getAudioSamplingRateHz();

    @Override
    public abstract CameraControllerI.Facing getVideoCameraFacing();

    @Override
    public abstract Optional<Integer> getVideoFrameRate();

    @Override
    public abstract ImageSize getVideoSize();

    @Override
    public abstract ImageScale getVideoImageScale();

    @Override
    public abstract ImageFit getVideoImageFit();

    @Override
    public abstract boolean getShouldCropVideo();

    @Override
    public abstract boolean getShouldPadVideo();

    @Override
    public abstract Optional<Integer> getVideoBitrate();

    @Override
    public abstract VideoCodec getVideoCodec();

    @Override
    public abstract OutputFormat getOutputFormat();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return RecorderParams.Builder.<Builder>setDefaults(new AutoValue_RecorderParams.Builder());
    }

    @AutoValue.Builder
    public abstract static class Builder implements RecorderParamsI.BuilderI<Builder> {

        public static <T extends RecorderParamsI.BuilderI<T>> T setOnlyClassDefaults(T builder) {
            return builder;
        }

        public static <T extends RecorderParamsI.BuilderI<T>> T setDefaults(T builder) {
            CameraParams.Builder.setOnlyClassDefaults(builder);
            EncoderParams.Builder.setOnlyClassDefaults(builder);
            VideoFrameRateParams.Builder.setOnlyClassDefaults(builder);
            VideoScaleParams.Builder.setOnlyClassDefaults(builder);
            VideoSizeParams.Builder.setOnlyClassDefaults(builder);
            VideoTransformerParams.Builder.setOnlyClassDefaults(builder);
            return setOnlyClassDefaults(builder);
        }

        public static <T extends RecorderParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, RecorderParamsI params) {
            return builder;
        }

        public static <T extends RecorderParamsI.BuilderI<T>> T merge(T builder, RecorderParamsI params) {
            CameraParams.Builder.mergeOnlyClass(builder, params);
            EncoderParams.Builder.mergeOnlyClass(builder, params);
            VideoFrameRateParams.Builder.mergeOnlyClass(builder, params);
            VideoScaleParams.Builder.mergeOnlyClass(builder, params);
            VideoSizeParams.Builder.mergeOnlyClass(builder, params);
            VideoTransformerParams.Builder.mergeOnlyClass(builder, params);
            return mergeOnlyClass(builder, params);
        }

        protected Builder() {}

        @Override
        public Builder setAudioBitrate(int val) {
            return setAudioBitrate(Optional.of(val));
        }
        @Override
        public abstract Builder setAudioBitrate(Optional<Integer> val);

        @Override
        public Builder setAudioChannelCount(int val) {
            return setAudioChannelCount(Optional.of(val));
        }
        @Override
        public abstract Builder setAudioChannelCount(Optional<Integer> val);

        @Override
        public Builder setAudioSamplingRateHz(int val) {
            return setAudioSamplingRateHz(Optional.of(val));
        }
        @Override
        public abstract Builder setAudioSamplingRateHz(Optional<Integer> val);

        @Override
        public abstract Builder setAudioCodec(AudioCodec val);

        @Override
        public abstract Builder setVideoCameraFacing(CameraControllerI.Facing val);

        @Override
        public Builder setVideoFrameRate(int val) {
            return setVideoFrameRate(Optional.of(val));
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
        public abstract Builder setShouldCropVideo(boolean val);

        @Override
        public abstract Builder setShouldPadVideo(boolean val);

        @Override
        public Builder setVideoBitrate(int val) {
            return setVideoBitrate(Optional.of(val));
        }
        @Override
        public abstract Builder setVideoBitrate(Optional<Integer> val);

        @Override
        public abstract Builder setVideoCodec(VideoCodec val);

        @Override
        public abstract Builder setOutputFormat(OutputFormat val);

        @Override
        public abstract RecorderParams build();
    }
}
