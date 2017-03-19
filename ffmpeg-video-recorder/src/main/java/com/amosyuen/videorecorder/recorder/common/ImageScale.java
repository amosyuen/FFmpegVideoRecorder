package com.amosyuen.videorecorder.recorder.common;

import java.security.InvalidParameterException;

/**
 * The way to scale an image.
 */
public enum ImageScale {
    /**
     * Disallow any scaling.
     */
    NONE(0),

    /**
     * Allow scaling images down.
     */
    DOWNSCALE(1),

    /**
     * Allow scaling images up.
     */
    UPSCALE(1 << 1),

    /**
     * Allow any scaling.
     */
    ANY(DOWNSCALE.mBitMask | UPSCALE.mBitMask);

    private final int mBitMask;

    ImageScale(int bitMask) {
        mBitMask = bitMask;
    }

    public boolean intersects(ImageScale direction) {
        return (mBitMask | direction.mBitMask) != 0;
    }

    public ImageScale invert() {
        switch (this) {
            case NONE:
                return ImageScale.NONE;
            case DOWNSCALE:
                return ImageScale.UPSCALE;
            case ANY:
                return ImageScale.ANY;
            case UPSCALE:
                return ImageScale.DOWNSCALE;
            default:
                throw new InvalidParameterException(
                        String.format("Unsupported image scale %s", this));
        }
    }
}
