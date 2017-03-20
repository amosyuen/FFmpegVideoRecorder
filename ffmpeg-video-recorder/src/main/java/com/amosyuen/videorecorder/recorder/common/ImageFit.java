package com.amosyuen.videorecorder.recorder.common;

/**
 * How to fit an image to desired dimensions.
 */
public enum ImageFit {
    /**
     * Fill the bounds so that both dimension will be equal to or greater than the desired
     * dimensions while maintaining the aspect ratio.
     */
    FILL,

    /**
     * Fit the bounds so that both dimension will be equal to or less than the desired dimensions
     * while maintaining the aspect ratio.
     */
    FIT;
}
