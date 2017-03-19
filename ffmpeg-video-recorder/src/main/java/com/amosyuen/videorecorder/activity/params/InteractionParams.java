package com.amosyuen.videorecorder.activity.params;

import com.google.auto.value.AutoValue;

/**
 * Parameters for the video recorder activity.
 */
@AutoValue
public abstract class InteractionParams implements InteractionParamsI {

    protected InteractionParams() {}

    public abstract long getMaxFileSizeBytes();

    public abstract long getMinRecordingMillis();

    public abstract long getMaxRecordingMillis();

    public abstract long getTapToFocusHoldTimeMillis();

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
                    .setTapToFocusHoldTimeMillis(5000);
        }

        public static <T extends InteractionParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, InteractionParamsI params) {
            return builder
                    .setMaxFileSizeBytes(params.getMaxFileSizeBytes())
                    .setMinRecordingMillis(params.getMaxRecordingMillis())
                    .setMaxRecordingMillis(params.getMaxRecordingMillis())
                    .setTapToFocusHoldTimeMillis(params.getTapToFocusHoldTimeMillis());
        }

        protected Builder() {}

        public abstract Builder setMaxFileSizeBytes(long val);

        public abstract Builder setMinRecordingMillis(long val);

        public abstract Builder setMaxRecordingMillis(long val);

        public abstract Builder setTapToFocusHoldTimeMillis(long val);

        @Override
        public abstract InteractionParams build();
    }
}
