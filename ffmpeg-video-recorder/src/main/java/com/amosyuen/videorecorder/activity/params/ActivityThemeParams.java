package com.amosyuen.videorecorder.activity.params;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;

import com.amosyuen.videorecorder.R;
import com.amosyuen.videorecorder.ui.ViewUtil;
import com.google.auto.value.AutoValue;

/**
 * Parameters for customizing the activity theme dynamically.
 */
@AutoValue
public abstract class ActivityThemeParams implements ActivityThemeParamsI {

    protected ActivityThemeParams() {}

    @Override
    @ColorInt public abstract int getStatusBarColor();

    @Override
    @ColorInt public abstract int getToolbarColor();

    @Override
    @ColorInt public abstract int getToolbarWidgetColor();

    @Override
    @ColorInt public abstract int getProgressColor();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_ActivityThemeParams.Builder();
    }

    /**
     * Creates a builder with colors initialized to values from the context theme.
     */
    public static Builder builder(Context context) {
        return ActivityThemeParams.Builder.setOnlyClassDefaults(builder(), context);
    }

    @AutoValue.Builder
    public abstract static class Builder implements ActivityThemeParamsI.BuilderI<Builder> {

        public static <T extends ActivityThemeParamsI.BuilderI<T>> T setOnlyClassDefaults(
                T builder, Context context) {
            Resources.Theme theme = context.getTheme();
            return builder
                    .setProgressColor(ViewUtil.getThemeColorAttribute(theme, R.attr.colorAccent))
                    .setStatusBarColor(ViewUtil.getThemeColorAttribute(theme, R.attr.colorPrimaryDark))
                    .setToolbarColor(ViewUtil.getThemeColorAttribute(theme, R.attr.colorPrimary))
                    .setToolbarWidgetColor(ViewUtil.getThemeColorAttribute(theme, android.R.attr.textColor));
        }

        public static <T extends ActivityThemeParamsI.BuilderI<T>> T mergeOnlyClass(
                T builder, ActivityThemeParamsI params) {
            return builder
                    .setStatusBarColor(params.getStatusBarColor())
                    .setToolbarColor(params.getToolbarColor())
                    .setToolbarWidgetColor(params.getToolbarWidgetColor())
                    .setProgressColor(params.getProgressColor());
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

        public abstract ActivityThemeParams build();
    }
}
