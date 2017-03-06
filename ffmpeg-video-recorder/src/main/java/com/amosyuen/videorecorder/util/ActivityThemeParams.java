package com.amosyuen.videorecorder.util;

import android.support.annotation.ColorInt;

import java.io.Serializable;

/**
 * Parameters for customizing the activity theme on the fly.
 */
public interface ActivityThemeParams extends Serializable {

    @ColorInt int getStatusBarColor();
    @ColorInt int getToolbarColor();
    @ColorInt int getToolbarWidgetColor();
    @ColorInt int getProgressColor();

    class Builder {
        @ColorInt private int statusBarColor;
        @ColorInt private int toolbarColor;
        @ColorInt private int toolbarWidgetColor;
        @ColorInt private int progressColor;

        public Builder() {}

        public Builder merge(ActivityThemeParams copy) {
            statusBarColor = copy.getStatusBarColor();
            toolbarColor = copy.getToolbarColor();
            toolbarWidgetColor = copy.getToolbarWidgetColor();
            progressColor = copy.getProgressColor();
            return this;
        }

        public Builder statusBarColor(@ColorInt int val) {
            statusBarColor = val;
            return this;
        }

        public Builder toolbarColor(@ColorInt int val) {
            toolbarColor = val;
            return this;
        }

        public Builder toolbarWidgetColor(@ColorInt int val) {
            toolbarWidgetColor = val;
            return this;
        }

        public Builder progressColor(@ColorInt int val) {
            progressColor = val;
            return this;
        }

        public ActivityThemeParams build() { return new ActivityThemeParamsImpl(this); }

        static class ActivityThemeParamsImpl implements ActivityThemeParams {
            @ColorInt private final int statusBarColor;
            @ColorInt private final int toolbarColor;
            @ColorInt private final int toolbarWidgetColor;
            @ColorInt private final int progressColor;

            private ActivityThemeParamsImpl(Builder builder) {
                statusBarColor = builder.statusBarColor;
                toolbarColor = builder.toolbarColor;
                toolbarWidgetColor = builder.toolbarWidgetColor;
                progressColor = builder.progressColor;
            }

            @Override
            @ColorInt
            public int getStatusBarColor() {
                return statusBarColor;
            }

            @Override
            @ColorInt
            public int getToolbarColor() {
                return toolbarColor;
            }

            @Override
            @ColorInt
            public int getToolbarWidgetColor() {
                return toolbarWidgetColor;
            }

            @Override
            @ColorInt
            public int getProgressColor() {
                return progressColor;
            }

            @Override
            public String toString() {
                return "ActivityThemeParamsImpl{" +
                        "statusBarColor=" + statusBarColor +
                        ", toolbarColor=" + toolbarColor +
                        ", toolbarWidgetColor=" + toolbarWidgetColor +
                        ", progressColor=" + progressColor +
                        '}';
            }
        }
    }
}