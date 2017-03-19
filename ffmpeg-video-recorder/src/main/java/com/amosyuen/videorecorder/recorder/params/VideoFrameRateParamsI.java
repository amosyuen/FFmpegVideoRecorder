package com.amosyuen.videorecorder.recorder.params;


import com.google.common.base.Optional;

import java.io.Serializable;

/**
 * Parameters for video frame rate.
 */
public interface VideoFrameRateParamsI extends Serializable {

    /**
     * Get the desired frame rate in frames per second.
     */
    Optional<Integer> getVideoFrameRate();

    interface BuilderI<T extends BuilderI<T>> {

        /**
         * Set the desired frame rate in frames per second. If not set, the default camera frame
         * rate will be used.
         */
        T setVideoFrameRate(int val);
        /**
         * Set the desired frame rate in frames per second. If not set, the default camera frame
         * rate will be used.
         */
        T setVideoFrameRate(Optional<Integer> val);

        VideoFrameRateParamsI build();
    }
}
