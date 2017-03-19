package com.amosyuen.videorecorder.activity.params;


import android.support.annotation.ColorInt;

import java.io.Serializable;

/**
 * Parameters for the video recorder activity theme.
 */
public interface RecorderActivityThemeParamsI extends ActivityThemeParamsI, Serializable {

    @ColorInt
    int getProgressCursorColor();

    @ColorInt
    int getProgressMinProgressColor();

    @ColorInt
    int getWidgetColor();

    interface BuilderI<T extends BuilderI<T>> extends ActivityThemeParamsI.BuilderI<T> {

        T setProgressCursorColor(@ColorInt int val);

        T setProgressMinProgressColor(@ColorInt int val);

        T setWidgetColor(@ColorInt int val);

        @Override
        RecorderActivityThemeParamsI build();
    }
}