package com.amosyuen.videorecorder.activity.params;

import com.google.auto.value.AutoValue;

/**
 * Parameters for the video recorder activity.
 */
@AutoValue
public abstract class InteractionParams implements InteractionParamsI {

    protected InteractionParams() {}

    @Override
    public abstract long getMaxFileSizeBytes();

    @Override
    public abstract long getMinRecordingMillis();

    @Override
    public abstract long getMaxRecordingMillis();

    @Override
    public abstract long getTapToFocusHoldTimeMillis();

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
                    .setMaxRecordingMillis(Long.MAX_VALUE)
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

        protected Builder() {}

        @Override
        public abstract Builder setMaxFileSizeBytes(long val);

        @Override
        public abstract Builder setMinRecordingMillis(long val);

        @Override
        public abstract Builder setMaxRecordingMillis(long val);

        @Override
        public abstract Builder setTapToFocusHoldTimeMillis(long val);

        @Override
        public abstract Builder setTapToFocusSizePercent(float val);

        @Override
        public abstract InteractionParams build();
    }
}
