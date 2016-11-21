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

    public float getAspectRatio() {
        return (float)width / height;
    }

    public void roundWidthUpToEvenAndMaintainAspectRatio() {
        if (width != SIZE_UNDEFINED && width % 2 != 0) {
            int roundedWidth = width + 1;
            if (height != SIZE_UNDEFINED) {
                height = (roundedWidth * height + width / 2) / width;
            }
            width = roundedWidth;
        }
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

    /**
     * @param targetSize must be a defined size.
     */
    public boolean scale(ImageSize targetSize, ScaleType scaleType, boolean allowUpscale) {
        switch (scaleType) {
            case FILL:
                return scaleToFill(targetSize, allowUpscale);
            case FIT:
                return scaleToFit(targetSize, allowUpscale);
        }
        return false;
    }

    /**
     * @param targetSize must be a defined size.
     */
    public boolean scaleToFill(ImageSize targetSize, boolean allowUpscale) {
        return scale(targetSize, allowUpscale, true);
    }

    /**
     * @param targetSize must be a defined size.
     */
    public boolean scaleToFit(ImageSize targetSize, boolean allowUpscale) {
        return scale(targetSize, allowUpscale, false);
    }

    /**
     * @param targetSize must be a defined size.
     */
    protected boolean scale(ImageSize targetSize, boolean allowUpscale, boolean isFill) {
        if (BuildConfig.DEBUG && !targetSize.areDimensionsDefined()) {
            throw new IllegalArgumentException("targetSize is not defined");
        }
        boolean isAspectRatioGreaterThan = targetSize.getAspectRatio() > getAspectRatio();
        if (isAspectRatioGreaterThan ^ isFill) {
            if (targetSize.height < height || (targetSize.height > height && allowUpscale)) {
                width = width * targetSize.height / height;
                height = targetSize.height;
                return true;
            }
        } else if (targetSize.width < width || (targetSize.width > width && allowUpscale)) {
            height = height * targetSize.width / width;
            width = targetSize.width;
            return true;
        }
        return false;
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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageSize)) {
            return false;
        }
        ImageSize other = (ImageSize) obj;
        return width == other.width && height == other.height;
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
