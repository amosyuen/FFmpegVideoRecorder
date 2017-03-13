package com.amosyuen.videorecorder.recorder.common;

/**
 * Image scale type.
 */
public enum ScaleType {
    /**
     * Scale the image uniformly so that both dimension will be equal to or greater than the
     * desired dimensions.
     */
    FILL,

    /**
     * Scale the image uniformly so that both dimension will be equal to or less than the
     * desired dimensions.
     */
    FIT,
}
