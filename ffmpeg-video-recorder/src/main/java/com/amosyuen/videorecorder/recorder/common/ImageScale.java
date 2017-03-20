package com.amosyuen.videorecorder.recorder.common;

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
        return (mBitMask & direction.mBitMask) != 0;
    }
}
