package com.amosyuen.videorecorder.video;


import com.amosyuen.videorecorder.BuildConfig;
/**
 * Simple size class
 */
public class ImageSize {

    public static final int SIZE_UNDEFINED = 0;

    public int width;
    public int height;

    public ImageSize() {
        width = SIZE_UNDEFINED;
        height = SIZE_UNDEFINED;
    }

    public ImageSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public ImageSize clone() {
        return new ImageSize(width, height);
    }

    public void setSize(ImageSize size) {
        width = size.width;
        height = size.height;
    }

    public boolean areDimensionsDefined() {
        return width != SIZE_UNDEFINED && height != SIZE_UNDEFINED;
    }

    public boolean isAtLeastOneDimensionDefined() {
        return width != SIZE_UNDEFINED || height != SIZE_UNDEFINED;
    }

    /**
     * Calculates the undefined dimensions in this size using the aspect ratio from the source size.
     */
    public boolean calculateUndefinedDimensions(ImageSize source) {
        if (width == SIZE_UNDEFINED) {
            width = source.width * height / source.height;
            return true;
        } else if (height == SIZE_UNDEFINED) {
            height = source.height * width / source.width;
            return true;
        }
        return false;
    }

    public void scaleBy(float scale) {
        if (width != SIZE_UNDEFINED) {
            width *= scale;
        }
        if (height != SIZE_UNDEFINED) {
            height *= scale;
        }
    }

    /**
     * @param targetSize must be a defined size.
     */
    public float scale(ImageSize targetSize, ScaleType scaleType, boolean allowUpscale) {
        switch (scaleType) {
            case FILL:
                return scaleToFill(targetSize, allowUpscale);
            case FIT:
                return scaleToFit(targetSize, allowUpscale);
        }
        return 1f;
    }

    /**
     * @param targetSize must be a defined size.
     */
    public float scaleToFill(ImageSize targetSize, boolean allowUpscale) {
        if (BuildConfig.DEBUG && !targetSize.areDimensionsDefined()) {
            throw new IllegalArgumentException("targetSize is not defined");
        }
        return scaleIfAllowed(
                Math.max((float) targetSize.width / width, (float) targetSize.height / height),
                allowUpscale);
    }

    /**
     * @param targetSize must be a defined size.
     */
    public float scaleToFit(ImageSize targetSize, boolean allowUpscale) {
        if (BuildConfig.DEBUG && !targetSize.areDimensionsDefined()) {
            throw new IllegalArgumentException("targetSize is not defined");
        }
        return scaleIfAllowed(
                Math.min((float) targetSize.width / width, (float) targetSize.height / height),
                allowUpscale);
    }

    protected float scaleIfAllowed(float scale, boolean allowUpscale) {
        if (scale < 1.0f || (scale > 1.0f && allowUpscale)) {
            scaleBy(scale);
            return scale;
        }
        return 1f;
    }
    
    public boolean cropTo(ImageSize targetSize) {
        boolean cropped = false;
        if (width > targetSize.width) {
            cropped = true;
            width = targetSize.width;
        }
        if (height > targetSize.height) {
            cropped = true;
            height = targetSize.height;
        }
        return cropped;
    }
    
    public boolean padTo(ImageSize targetSize) {
        boolean padded = false;
        if (width < targetSize.width) {
            padded = true;
            width = targetSize.width;
        }
        if (height < targetSize.height) {
            padded = true;
            height = targetSize.height;
        }
        return padded;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(width=" + (width == SIZE_UNDEFINED ? "undefined" : width)
                + ", height=" + (height == SIZE_UNDEFINED ? "undefined" : height) + ")";
    }

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
}
