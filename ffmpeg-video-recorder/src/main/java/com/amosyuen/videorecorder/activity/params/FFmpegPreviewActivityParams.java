package com.amosyuen.videorecorder.activity.params;

import android.content.Context;
import android.net.Uri;

import com.amosyuen.videorecorder.activity.FFmpegPreviewActivity;
import com.google.auto.value.AutoValue;

import java.io.File;
import java.io.Serializable;

/**
 * Parameters for {@link FFmpegPreviewActivity}.
 */
@AutoValue
public abstract class FFmpegPreviewActivityParams implements Serializable {

    protected FFmpegPreviewActivityParams() {}

    /**
     * Get the URI of the video file to preview.
     */
    public abstract String getVideoFileUri();

    /**
     * Get whether the activity is for confirming the saving of the video.
     */
    public abstract boolean isConfirmation();

    /**
     * Get the theme params.
     */
    public abstract ActivityThemeParams getThemeParams();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_FFmpegPreviewActivityParams.Builder()
                .setConfirmation(false);
    }

    /**
     * Creates a builder with colors initialized to values from the context theme.
     */
    public static Builder builder(Context context) {
        Builder builder = builder();
        builder.setThemeParams(ActivityThemeParams.builder(context).build());
        return builder;
    }

    @AutoValue.Builder
    public abstract static class Builder {

        protected Builder() {}

        /**
         * Set the video file to preview.
         */
        public Builder setVideoFileUri(File file) {
            return setVideoFileUri(Uri.fromFile(file));
        }
        /**
         * Set the URI of the video file to preview.
         */
        public Builder setVideoFileUri(Uri uri) {
            return setVideoFileUri(uri.toString());
        }
        /**
         * Set the string URI of the video file to preview.
         */
        public abstract Builder setVideoFileUri(String val);

        /**
         * Set whether the activity is for confirming the saving of the video.
         */
        public abstract Builder setConfirmation(boolean val);

        /**
         * Get a builder for the theme params.
         */
        public abstract ActivityThemeParams.Builder themeParamsBuilder();
        /**
         * Set the theme params.
         */
        public abstract Builder setThemeParams(ActivityThemeParams val);

        public abstract FFmpegPreviewActivityParams build();
    }
}
