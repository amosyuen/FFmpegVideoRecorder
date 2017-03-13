package com.amosyuen.videorecorder.activity;

import android.support.annotation.ColorInt;

import java.io.Serializable;

/**
 * Parameters for customizing the activity theme dynamically.
 */
public interface ActivityThemeParams extends Serializable {

    @ColorInt int getStatusBarColor();
    @ColorInt int getToolbarColor();
    @ColorInt int getToolbarWidgetColor();
    @ColorInt int getProgressColor();

    interface BuilderI<T extends BuilderI<T>> {

        BuilderI statusBarColor(@ColorInt int val);

        BuilderI toolbarColor(@ColorInt int val);

        BuilderI toolbarWidgetColor(@ColorInt int val);

        BuilderI progressColor(@ColorInt int val);

        ActivityThemeParams build();
    }

    class Builder implements BuilderI<Builder> {
        @ColorInt protected int statusBarColor;
        @ColorInt protected int toolbarColor;
        @ColorInt protected int toolbarWidgetColor;
        @ColorInt protected int progressColor;

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
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                ActivityThemeParamsImpl that = (ActivityThemeParamsImpl) o;

                if (statusBarColor != that.statusBarColor) return false;
                if (toolbarColor != that.toolbarColor) return false;
                if (toolbarWidgetColor != that.toolbarWidgetColor) return false;
                return progressColor == that.progressColor;

            }

            @Override
            public int hashCode() {
                int result = statusBarColor;
                result = 31 * result + toolbarColor;
                result = 31 * result + toolbarWidgetColor;
                result = 31 * result + progressColor;
                return result;
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