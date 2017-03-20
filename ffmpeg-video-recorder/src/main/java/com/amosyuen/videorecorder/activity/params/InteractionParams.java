package com.amosyuen.videorecorder.activity.params;

import com.amosyuen.videorecorder.recorder.params.EncoderParams;
import com.amosyuen.videorecorder.recorder.params.RecorderParamsI;
import com.amosyuen.videorecorder.recorder.params.VideoFrameRateParams;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

/**
 * Parameters for the video recorder activity.
 */
@AutoValue
public abstract class InteractionParams implements InteractionParamsI {

    protected InteractionParams() {}

    @Override
    public abstract long getMaxFileSizeBytes();

    @Override
    public abstract int getMinRecordingMillis();

    @Override
    public abstract int getMaxRecordingMillis();

    @Override
    public abstract int getTapToFocusHoldTimeMillis();

    @Override
    public abstract float getTapToFocusSizePercent();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return InteractionParams.Builder
                .<Builder>setOnlyClassDefaults(new AutoValue_InteractionParams.Builder());
    }

    @AutoValue.Builder
    public abstract static class Builder implements InteractionParamsI.BuilderI<Builder> {

        public static <T extends InteractionParamsI.BuilderI<T>> T setOnlyClassDefaults(T builder) {
            return builder
                    .setMaxFileSizeBytes(Long.MAX_VALUE)
                    .setMinRecordingMillis(1)
                    .setMaxRecordingMillis(Integer.MAX_VALUE)
                    .setTapToFocusHoldTimeMillis(5000)
                    .setTapToFocusSizePercent(0.15f);
        }

        public static <T extends InteractionParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, InteractionParamsI params) {
            return builder
                    .setMaxFileSizeBytes(params.getMaxFileSizeBytes())
                    .setMinRecordingMillis(params.getMaxRecordingMillis())
                    .setMaxRecordingMillis(params.getMaxRecordingMillis())
                    .setTapToFocusHoldTimeMillis(params.getTapToFocusHoldTimeMillis())
                    .setTapToFocusSizePercent(params.getTapToFocusSizePercent());
        }

        public static <T extends InteractionParamsI> T validateOnlyClass(T params) {
            Preconditions.checkState(params.getMaxFileSizeBytes() > 0);
            Preconditions.checkState(params.getMinRecordingMillis() > 0);
            Preconditions.checkState(params.getMaxRecordingMillis() > 0);
            Preconditions.checkState(
                    params.getMaxRecordingMillis() >= params.getMinRecordingMillis());
            Preconditions.checkState(params.getTapToFocusHoldTimeMillis() > 0);
            Preconditions.checkState(
                    params.getTapToFocusSizePercent() > 0
                            && params.getTapToFocusSizePercent() <= 1f);
            return params;
        }

        protected Builder() {}

        @Override
        public abstract Builder setMaxFileSizeBytes(long val);

        @Override
        public abstract Builder setMinRecordingMillis(int val);

        @Override
        public abstract Builder setMaxRecordingMillis(int val);

        @Override
        public abstract Builder setTapToFocusHoldTimeMillis(int val);

        @Override
        public abstract Builder setTapToFocusSizePercent(float val);

        abstract InteractionParams autoBuild();

        @Override
        public InteractionParams build() {
            return validateOnlyClass(autoBuild());
        }
    }
}
