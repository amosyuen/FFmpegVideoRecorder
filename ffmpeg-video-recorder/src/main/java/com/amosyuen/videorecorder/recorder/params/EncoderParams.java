package com.amosyuen.videorecorder.recorder.params;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Parameters for encoding video.
 */
@AutoValue
public abstract class EncoderParams implements EncoderParamsI {

    protected EncoderParams() {}

    @Override
    public abstract Optional<Integer> getAudioBitrate();

    @Override
    public abstract Optional<Integer> getAudioChannelCount();

    @Override
    public abstract AudioCodec getAudioCodec();

    @Override
    public abstract Optional<Integer> getAudioSamplingRateHz();

    @Override
    public abstract Optional<Integer> getVideoFrameRate();

    @Override
    public abstract Optional<Integer> getVideoBitrate();

    @Override
    public abstract VideoCodec getVideoCodec();

    @Override
    public abstract OutputFormat getOutputFormat();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return EncoderParams.Builder.<Builder>setDefaults(new AutoValue_EncoderParams.Builder());
    }

    @AutoValue.Builder
    public abstract static class Builder implements EncoderParamsI.BuilderI<Builder> {

        public static <T extends EncoderParamsI.BuilderI<T>> T setOnlyClassDefaults(T builder) {
            return builder
                    .setAudioCodec(EncoderParamsI.AudioCodec.AAC)
                    .setVideoCodec(EncoderParamsI.VideoCodec.H264)
                    .setOutputFormat(EncoderParamsI.OutputFormat.MP4);
        }

        public static <T extends EncoderParamsI.BuilderI<T>> T setDefaults(T builder) {
            VideoFrameRateParams.Builder.setOnlyClassDefaults(builder);
            return setOnlyClassDefaults(builder);
        }

        public static <T extends EncoderParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, EncoderParamsI params) {
            return builder
                    .setAudioCodec(params.getAudioCodec())
                    .setVideoCodec(params.getVideoCodec())
                    .setOutputFormat(params.getOutputFormat());
        }

        public static <T extends EncoderParamsI.BuilderI<T>> T merge(
                T builder, EncoderParamsI params) {
            VideoFrameRateParams.Builder.mergeOnlyClass(builder, params);
            return mergeOnlyClass(builder, params);
        }

        public static <T extends EncoderParamsI> T validateOnlyClass(T params) {
            Preconditions.checkState(params.getAudioBitrate().or(1) > 0);
            Preconditions.checkState(params.getAudioChannelCount().or(1) > 0);
            Preconditions.checkState(params.getAudioSamplingRateHz().or(1) > 0);
            Preconditions.checkState(params.getVideoBitrate().or(1) > 0);
            return params;
        }

        public static <T extends EncoderParamsI> T validate(T params) {
            VideoFrameRateParams.Builder.validateOnlyClass(params);
            validateOnlyClass(params);
            return params;
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
        public Builder setVideoFrameRate(int val) {
            return setVideoFrameRate(Optional.of(val));
        }
        @Override
        public abstract Builder setVideoFrameRate(Optional<Integer> val);

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

        abstract EncoderParams autoBuild();

        @Override
        public EncoderParams build() {
            return validate(autoBuild());
        }
    }
}
