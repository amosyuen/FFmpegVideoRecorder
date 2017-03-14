package com.amosyuen.videorecorder.recorder.common;

import java.security.InvalidParameterException;

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

    public ImageFit invert() {
        switch (this) {
            case FILL:
                return ImageFit.FIT;
            case FIT:
                return ImageFit.FILL;
            default:
                throw new InvalidParameterException(
                        String.format("Unsupported image fit %s", this));
        }
    }
}
