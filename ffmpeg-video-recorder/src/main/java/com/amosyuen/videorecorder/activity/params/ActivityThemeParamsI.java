package com.amosyuen.videorecorder.activity.params;

import android.support.annotation.ColorInt;

import java.io.Serializable;

/**
 * Parameters for customizing the activity theme dynamically.
 */
public interface ActivityThemeParamsI extends Serializable {

    @ColorInt
    int getStatusBarColor();

    @ColorInt
    int getToolbarColor();

    @ColorInt
    int getToolbarWidgetColor();

    @ColorInt
    int getProgressColor();

    interface BuilderI<T extends BuilderI<T>> {

        T setStatusBarColor(@ColorInt int val);

        T setToolbarColor(@ColorInt int val);

        T setToolbarWidgetColor(@ColorInt int val);

        T setProgressColor(@ColorInt int val);

        ActivityThemeParamsI build();
    }
}