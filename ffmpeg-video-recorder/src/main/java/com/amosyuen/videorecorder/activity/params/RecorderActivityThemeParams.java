package com.amosyuen.videorecorder.activity.params;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.v4.app.ActivityCompat;

import com.amosyuen.videorecorder.R;
import com.amosyuen.videorecorder.ui.ViewUtil;
import com.google.auto.value.AutoValue;

/**
 * Parameters for the video recorder activity theme.
 */
@AutoValue
public abstract class RecorderActivityThemeParams
        implements ActivityThemeParamsI, RecorderActivityThemeParamsI {

    protected RecorderActivityThemeParams() {}

    @Override
    @ColorInt
    public abstract int getStatusBarColor();

    @Override
    @ColorInt
    public abstract int getToolbarColor();

    @Override
    @ColorInt
    public abstract int getToolbarWidgetColor();

    @Override
    @ColorInt
    public abstract int getProgressColor();

    @Override
    @ColorInt
    public abstract int getProgressCursorColor();

    @Override
    @ColorInt
    public abstract int getProgressMinProgressColor();

    @Override
    @ColorInt
    public abstract int getWidgetColor();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_RecorderActivityThemeParams.Builder();
    }

    /**
     * Creates a builder with colors initialized to values from the context theme.
     */
    public static Builder builder(Context context) {
        return RecorderActivityThemeParams.Builder.setDefaults(builder(), context);
    }

    @AutoValue.Builder
    public abstract static class Builder implements RecorderActivityThemeParamsI.BuilderI<Builder> {

        public static <T extends RecorderActivityThemeParamsI.BuilderI<T>> T setOnlyClassDefaults(
                T builder, Context context) {
            Resources.Theme theme = context.getTheme();
            return builder
                    .setProgressCursorColor(ViewUtil.getThemeColorAttribute(theme, R.attr.colorPrimaryDark))
                    .setProgressMinProgressColor(ViewUtil.getThemeColorAttribute(theme, R.attr.colorPrimary))
                    .setWidgetColor(ActivityCompat.getColor(context, android.R.color.white));
        }

        public static <T extends RecorderActivityThemeParamsI.BuilderI<T>> T setDefaults(
                T builder, Context context) {
            ActivityThemeParams.Builder.setOnlyClassDefaults(builder, context);
            return setOnlyClassDefaults(builder, context);
        }

        public static <T extends RecorderActivityThemeParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, RecorderActivityThemeParamsI params) {
            return builder
                    .setProgressCursorColor(params.getProgressCursorColor())
                    .setProgressMinProgressColor(params.getProgressMinProgressColor())
                    .setWidgetColor(params.getWidgetColor());
        }

        public static <T extends RecorderActivityThemeParamsI.BuilderI<T>> T merge(
                T builder, RecorderActivityThemeParamsI params) {
            ActivityThemeParams.Builder.mergeOnlyClass(builder, params);
            return mergeOnlyClass(builder, params);
        }

        protected Builder() {}

        @Override
        public abstract Builder setStatusBarColor(@ColorInt int val);

        @Override
        public abstract Builder setToolbarColor(@ColorInt int val);

        @Override
        public abstract Builder setToolbarWidgetColor(@ColorInt int val);

        @Override
        public abstract Builder setProgressColor(@ColorInt int val);

        @Override
        public abstract Builder setProgressCursorColor(@ColorInt int val);

        @Override
        public abstract Builder setProgressMinProgressColor(@ColorInt int val);

        @Override
        public abstract Builder setWidgetColor(@ColorInt int val);

        @Override
        public abstract RecorderActivityThemeParams build();
    }
}
