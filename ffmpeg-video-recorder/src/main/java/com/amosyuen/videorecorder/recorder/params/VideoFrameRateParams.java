package com.amosyuen.videorecorder.recorder.params;


import java.io.Serializable;

/**
 * Parameters for video frame rate.
 */
public interface VideoFrameRateParams extends Serializable {

    /**
     * Get the desired frame rate in frames per second.
     */
    int getVideoFrameRate();

    interface BuilderI<T extends BuilderI<T>> {

        /**
         * Set the desired frame rate in frames per second.
         */
        T videoFrameRate(int val);

        VideoFrameRateParams build();
    }
}
