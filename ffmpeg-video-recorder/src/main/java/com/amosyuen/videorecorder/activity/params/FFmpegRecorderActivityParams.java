package com.amosyuen.videorecorder.activity.params;

import android.content.Context;
import android.net.Uri;

import com.amosyuen.videorecorder.activity.FFmpegRecorderActivity;
import com.amosyuen.videorecorder.recorder.params.RecorderParams;
import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;

import java.io.File;
import java.io.Serializable;

/**
 * Parameters for {@link FFmpegRecorderActivity}.
 */
@AutoValue
public abstract class FFmpegRecorderActivityParams implements Serializable {

    protected FFmpegRecorderActivityParams() {}

    /**
     * Get the URI of the file to output the video to.
     */
    public abstract String getVideoOutputFileUri();

    /**
     * Get the URI of the file to output the video thumbnail to.
     */
    public abstract Optional<String> getVideoThumbnailOutputFileUri();

    /**
     * Get the interactions params.
     */
    public abstract InteractionParams getInteractionParams();

    /**
     * Get the recorder params.
     */
    public abstract RecorderParams getRecorderParams();

    /**
     * Get the theme params.
     */
    public abstract RecorderActivityThemeParams getThemeParams();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_FFmpegRecorderActivityParams.Builder();
    }

    /**
     * Creates a builder with colors initialized to values from the context theme.
     */
    public static Builder builder(Context context) {
        Builder builder = builder();
        builder.setThemeParams(RecorderActivityThemeParams.builder(context).build());
        return builder;
    }

    @AutoValue.Builder
    public abstract static class Builder {

        protected Builder() {}

        /**
         * Set the file to output the video to.
         */
        public Builder setVideoOutputFileUri(File file) {
            return setVideoOutputFileUri(Uri.fromFile(file));
        }
        /**
         * Set the URI of the file to output the video to.
         */
        public Builder setVideoOutputFileUri(Uri uri) {
            return setVideoOutputFileUri(uri.toString());
        }
        /**
         * Set the string URI of the file to output the video to.
         */
        public abstract Builder setVideoOutputFileUri(String val);

        /**
         * Set the file to output the video thumbnail to. If not set, no thumbnail will be output.
         */
        public Builder setVideoThumbnailOutputFileUri(File file) {
            return setVideoThumbnailOutputFileUri(Uri.fromFile(file));
        }
        /**
         * Set the URI of the file to output the video thumbnail to. If not set, no thumbnail will
         * be output.
         */
        public Builder setVideoThumbnailOutputFileUri(Uri uri) {
            return setVideoThumbnailOutputFileUri(uri.toString());
        }
        /**
         * Set the string URI of the file to output the video thumbnail to. If not set, no thumbnail
         * will be output.
         */
        public Builder setVideoThumbnailOutputFileUri(String val) {
            return setVideoThumbnailOutputFileUri(Optional.of(val));
        }
        /**
         * Set the string URI of the file to output the video thumbnail to. If not set, no thumbnail
         * will be output.
         */
        public abstract Builder setVideoThumbnailOutputFileUri(Optional<String> val);

        /**
         * Get a builder for the interaction params.
         */
        public abstract InteractionParams.Builder interactionParamsBuilder();
        /**
         * Set the interaction params.
         */
        public abstract Builder setInteractionParams(InteractionParams val);

        /**
         * Get a builder for the recorder params.
         */
        public abstract RecorderParams.Builder recorderParamsBuilder();
        /**
         * Set the recorder params.
         */
        public abstract Builder setRecorderParams(RecorderParams val);

        /**
         * Get a builder for the theme params.
         */
        public abstract RecorderActivityThemeParams.Builder themeParamsBuilder();
        /**
         * Set the theme params.
         */
        public abstract Builder setThemeParams(RecorderActivityThemeParams val);

        public abstract FFmpegRecorderActivityParams build();
    }
}
