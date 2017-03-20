package com.amosyuen.videorecorder.activity.params;


import java.io.Serializable;

/**
 * Parameters for user interaction.
 */
public interface InteractionParamsI extends Serializable {

    /**
     * Get the max recording file size in bytes.
     */
    long getMaxFileSizeBytes();

    /**
     * Get the min recording length in milliseconds.
     */
    int getMinRecordingMillis();

    /**
     * Get the max recording length in milliseconds.
     */
    int getMaxRecordingMillis();

    /**
     * Get how long in milliseconds the focus should be held at a tapped location before reverting
     * to auto focus.
     */
    int getTapToFocusHoldTimeMillis();

    /**
     * Get the percentage size of the preview area that the camera should focus on when tapped.
     */
    float getTapToFocusSizePercent();

    interface BuilderI<T extends BuilderI<T>> {

        /**
         * Set the max recording file size in bytes. Default value is {@link Long#MAX_VALUE}.
         */
        T setMaxFileSizeBytes(long val);

        /**
         * Set the min recording length in milliseconds. Default value is {@code 1}.
         */
        T setMinRecordingMillis(int val);

        /**
         * Set the max recording length in milliseconds. Default value is {@link Long#MAX_VALUE}.
         */
        T setMaxRecordingMillis(int val);

        /**
         * Set how long in milliseconds the focus should be held at a tapped location before
         * reverting to auto focus. Default to 5 seconds.
         */
        T setTapToFocusHoldTimeMillis(int val);

        /**
         * Set the percentage size of the preview area that the camera should focus on when tapped.
         * Default value is {@code 0.1}.
         */
        T setTapToFocusSizePercent(float val);

        InteractionParamsI build();
    }
}