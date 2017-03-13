package com.amosyuen.videorecorder.activity;


import android.net.Uri;
import android.support.annotation.ColorInt;

import com.amosyuen.videorecorder.recorder.params.RecorderParams;
import com.google.common.base.MoreObjects;

import java.io.File;
import java.io.Serializable;

/**
 * Parameters for the video recorder activity.
 */
public interface RecorderActivityParams
        extends ActivityThemeParams, RecorderParams, Serializable {

    @ColorInt int getProgressCursorColor();
    @ColorInt int getProgressMinProgressColor();
    @ColorInt int getWidgetColor();

    /**
     * How long in milliseconds that the focus for a tap should be held before reverting back to
     * automatic focus.
     */
    long getTapToFocusHoldTimeMillis();

    abstract class AbstractBuilder<T extends AbstractBuilder<T>>
            extends RecorderParams.AbstractBuilder<T>
            implements ActivityThemeParams.BuilderI<T> {

        @ColorInt
        protected int statusBarColor;
        @ColorInt
        protected int toolbarColor;
        @ColorInt
        protected int toolbarWidgetColor;
        @ColorInt
        protected int progressColor;
        @ColorInt
        protected int progressCursorColor;
        @ColorInt
        protected int progressMinProgressColor;
        @ColorInt
        protected int widgetColor;
        protected long tapToFocusHoldTimeMillis = 5000;

        public AbstractBuilder(File videoOutputFile) {
            super(videoOutputFile);
        }

        public AbstractBuilder(Uri videoOutputUri) {
            super(videoOutputUri);
        }

        public AbstractBuilder(String videoOutputUri) {
            super(videoOutputUri);
        }

        public T merge(RecorderActivityParams copy) {
            super.merge(copy);
            statusBarColor = copy.getStatusBarColor();
            toolbarColor = copy.getToolbarColor();
            toolbarWidgetColor = copy.getToolbarWidgetColor();
            progressColor = copy.getProgressColor();
            progressCursorColor = copy.getProgressCursorColor();
            progressMinProgressColor = copy.getProgressMinProgressColor();
            widgetColor = copy.getWidgetColor();
            tapToFocusHoldTimeMillis = copy.getTapToFocusHoldTimeMillis();
            return (T) this;
        }

        public T statusBarColor(int val) {
            statusBarColor = val;
            return (T) this;
        }

        public T toolbarColor(int val) {
            toolbarColor = val;
            return (T) this;
        }

        public T toolbarWidgetColor(int val) {
            toolbarWidgetColor = val;
            return (T) this;
        }

        public T progressColor(int val) {
            progressColor = val;
            return (T) this;
        }

        public T progressCursorColor(int val) {
            progressCursorColor = val;
            return (T) this;
        }

        public T progressMinProgressColor(int val) {
            progressMinProgressColor = val;
            return (T) this;
        }

        public T widgetColor(int val) {
            widgetColor = val;
            return (T) this;
        }

        public T tapToFocusHoldTimeMillis(long val) {
            tapToFocusHoldTimeMillis = val;
            return (T) this;
        }

        @Override
        public abstract RecorderActivityParams build();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            AbstractBuilder<?> that = (AbstractBuilder<?>) o;

            if (statusBarColor != that.statusBarColor) return false;
            if (toolbarColor != that.toolbarColor) return false;
            if (toolbarWidgetColor != that.toolbarWidgetColor) return false;
            if (progressColor != that.progressColor) return false;
            if (progressCursorColor != that.progressCursorColor) return false;
            if (progressMinProgressColor != that.progressMinProgressColor) return false;
            if (widgetColor != that.widgetColor) return false;
            return tapToFocusHoldTimeMillis == that.tapToFocusHoldTimeMillis;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + statusBarColor;
            result = 31 * result + toolbarColor;
            result = 31 * result + toolbarWidgetColor;
            result = 31 * result + progressColor;
            result = 31 * result + progressCursorColor;
            result = 31 * result + progressMinProgressColor;
            result = 31 * result + widgetColor;
            result = 31 * result + (int) (tapToFocusHoldTimeMillis ^ (tapToFocusHoldTimeMillis >>> 32));
            return result;
        }

        @Override
        public MoreObjects.ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("statusBarColor", statusBarColor)
                    .add("toolbarColor", toolbarColor)
                    .add("toolbarWidgetColor", toolbarWidgetColor)
                    .add("progressColor", progressColor)
                    .add("progressCursorColor", progressCursorColor)
                    .add("progressMinProgressColor", progressMinProgressColor)
                    .add("widgetColor", widgetColor)
                    .add("tapToFocusHoldTimeMillis", tapToFocusHoldTimeMillis);
        }
    }

    class Builder extends AbstractBuilder<Builder> {

        public Builder(File videoOutputFile) {
            super(videoOutputFile);
        }

        public Builder(Uri videoOutputUri) {
            super(videoOutputUri);
        }

        public Builder(String videoOutputUri) {
            super(videoOutputUri);
        }

        public RecorderActivityParams build() {
            return new RecorderActivityParamsImpl(this);
        }

        static class RecorderActivityParamsImpl
                extends RecorderParams.Builder.RecorderParamsImpl
                implements RecorderActivityParams {

            @ColorInt private final int statusBarColor;
            @ColorInt private final int toolbarColor;
            @ColorInt private final int toolbarWidgetColor;
            @ColorInt private final int progressColor;
            @ColorInt private final int progressCursorColor;
            @ColorInt private final int progressMinProgressColor;
            @ColorInt private final int widgetColor;
            private final long tapToFocusHoldTimeMillis ;

            protected RecorderActivityParamsImpl(RecorderActivityParams.AbstractBuilder builder) {
                super(builder);
                statusBarColor = builder.statusBarColor;
                toolbarColor = builder.toolbarColor;
                toolbarWidgetColor = builder.toolbarWidgetColor;
                progressColor = builder.progressColor;
                progressCursorColor = builder.progressCursorColor;
                progressMinProgressColor = builder.progressMinProgressColor;
                widgetColor = builder.widgetColor;
                tapToFocusHoldTimeMillis = builder.tapToFocusHoldTimeMillis;
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
            @ColorInt
            public int getProgressCursorColor() {
                return progressCursorColor;
            }

            @Override
            @ColorInt
            public int getProgressMinProgressColor() {
                return progressMinProgressColor;
            }

            @Override
            @ColorInt
            public int getWidgetColor() {
                return widgetColor;
            }

            @Override
            public long getTapToFocusHoldTimeMillis() {
                return tapToFocusHoldTimeMillis;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                if (!super.equals(o)) return false;

                RecorderActivityParamsImpl that = (RecorderActivityParamsImpl) o;

                if (statusBarColor != that.statusBarColor) return false;
                if (toolbarColor != that.toolbarColor) return false;
                if (toolbarWidgetColor != that.toolbarWidgetColor) return false;
                if (progressColor != that.progressColor) return false;
                if (progressCursorColor != that.progressCursorColor) return false;
                if (progressMinProgressColor != that.progressMinProgressColor) return false;
                if (widgetColor != that.widgetColor) return false;
                return tapToFocusHoldTimeMillis == that.tapToFocusHoldTimeMillis;

            }

            @Override
            public int hashCode() {
                int result = super.hashCode();
                result = 31 * result + statusBarColor;
                result = 31 * result + toolbarColor;
                result = 31 * result + toolbarWidgetColor;
                result = 31 * result + progressColor;
                result = 31 * result + progressCursorColor;
                result = 31 * result + progressMinProgressColor;
                result = 31 * result + widgetColor;
                result = 31 * result + (int) (tapToFocusHoldTimeMillis ^ (tapToFocusHoldTimeMillis >>> 32));
                return result;
            }

            @Override
            public MoreObjects.ToStringHelper toStringHelper() {
                return super.toStringHelper()
                        .add("statusBarColor", statusBarColor)
                        .add("toolbarColor", toolbarColor)
                        .add("toolbarWidgetColor", toolbarWidgetColor)
                        .add("progressColor", progressColor)
                        .add("progressCursorColor", progressCursorColor)
                        .add("progressMinProgressColor", progressMinProgressColor)
                        .add("widgetColor", widgetColor)
                        .add("tapToFocusHoldTimeMillis", tapToFocusHoldTimeMillis);
            }
        }
    }
}