package com.amosyuen.videorecorder.recorder.common;

import java.security.InvalidParameterException;

/**
 * The way to scale an image.
 */
public enum ImageScale {
    /**
     * Allow scaling images down.
     */
    DOWNSCALE(1),

    /**
     * Allow scaling images up.
     */
    UPSCALE(1 << 1),

    /**
     * Allow scaling images down or up.
     */
    DOWNSCALE_OR_UPSCALE(DOWNSCALE.mBitMask | UPSCALE.mBitMask);

    private final int mBitMask;

    ImageScale(int bitMask) {
        mBitMask = bitMask;
    }

    public boolean intersects(ImageScale direction) {
        return (mBitMask | direction.mBitMask) != 0;
    }

    public ImageScale invert() {
        switch (this) {
            case DOWNSCALE:
                return ImageScale.UPSCALE;
            case DOWNSCALE_OR_UPSCALE:
                return ImageScale.DOWNSCALE_OR_UPSCALE;
            case UPSCALE:
                return ImageScale.DOWNSCALE;
            default:
                throw new InvalidParameterException(
                        String.format("Unsupported image scale %s", this));
        }
    }
}
