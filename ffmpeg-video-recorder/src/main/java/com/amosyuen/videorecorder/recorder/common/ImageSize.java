package com.amosyuen.videorecorder.recorder.common;


import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * Immutable class representing an integer based size.
 */
public class ImageSize implements Serializable {

    public static final int SIZE_UNDEFINED = 0;
    public static ImageSize UNDEFINED = new ImageSize(SIZE_UNDEFINED, SIZE_UNDEFINED);

    public final int width;
    public final int height;

    public ImageSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean areDimensionsDefined() {
        return width != SIZE_UNDEFINED && height != SIZE_UNDEFINED;
    }

    public boolean isAtLeastOneDimensionDefined() {
        return width != SIZE_UNDEFINED || height != SIZE_UNDEFINED;
    }

    public float getAspectRatio() {
        return (float) width / height;
    }

    public long getArea() { return (long) width * height; }

    public Builder toBuilder() {
        return new Builder(width, height);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageSize)) {
            return false;
        }
        ImageSize other = (ImageSize) obj;
        return width == other.width && height == other.height;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "(width=" + (width == SIZE_UNDEFINED ? "undefined" : width)
                + ", height=" + (height == SIZE_UNDEFINED ? "undefined" : height) + ")";
    }

    /**
     * AbstractBuilder class for image size that supports common operations.
     */
    public static class Builder {
        public int width;
        public int height;

        public Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder clone() {
            return new Builder(width, height);
        }

        public boolean areDimensionsDefined() {
            return width != SIZE_UNDEFINED && height != SIZE_UNDEFINED;
        }

        public boolean isAtLeastOneDimensionDefined() {
            return width != SIZE_UNDEFINED || height != SIZE_UNDEFINED;
        }

        public float getAspectRatio() {
            return (float) width / height;
        }

        public long getArea() { return (long) width * height; }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder setSize(ImageSize imageSize) {
            setSize(imageSize.width, imageSize.height);
            return this;
        }

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder max(ImageSize imageSize) {
            width = Math.max(width, imageSize.width);
            height = Math.max(height, imageSize.height);
            return this;
        }

        public Builder min(ImageSize imageSize) {
            width = Math.min(width, imageSize.width);
            height = Math.min(height, imageSize.height);
            return this;
        }

        public Builder invert() {
            int temp = width;
            //noinspection SuspiciousNameCombination
            width = height;
            height = temp;
            return this;
        }

        public Builder roundWidthUpToEvenAndMaintainAspectRatio() {
            if (width != SIZE_UNDEFINED && width % 2 != 0) {
                int roundedWidth = width + 1;
                if (height != SIZE_UNDEFINED) {
                    height = (roundedWidth * height + width / 2) / width;
                }
                width = roundedWidth;
            }
            return this;
        }

        /**
         * Calculates the undefined dimensions in this size using the aspect ratio from the source size.
         */
        public Builder calculateUndefinedDimensions(ImageSize source) {
            if (width == SIZE_UNDEFINED) {
                width = source.width * height / source.height;
            } else if (height == SIZE_UNDEFINED) {
                height = source.height * width / source.width;
            }
            return this;
        }

        /**
         * @param targetSize must be a defined size.
         */
        public Builder scale(ImageSize targetSize, ImageFit imageFit, ImageScale imageScale) {
            switch (imageFit) {
                case FILL:
                    scaleToFill(targetSize, imageScale);
                    break;
                case FIT:
                    scaleToFit(targetSize, imageScale);
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unsupported scale fit %s", imageFit));
            }
            return this;
        }

        /**
         * @param targetSize must be a defined size.
         */
        public Builder scaleToFill(ImageSize targetSize, ImageScale imageScale) {
            scale(targetSize, imageScale, true);
            return this;
        }

        /**
         * @param targetSize must be a defined size.
         */
        public Builder scaleToFit(ImageSize targetSize, ImageScale imageScale) {
            scale(targetSize, imageScale, false);
            return this;
        }

        /**
         * @param targetSize must be a defined size.
         */
        protected void scale(ImageSize targetSize, ImageScale imageScale, boolean isFill) {
            Preconditions.checkArgument(targetSize.areDimensionsDefined());
            boolean isAspectRatioGreaterThan = targetSize.getAspectRatio() > getAspectRatio();
            if (isAspectRatioGreaterThan ^ isFill) {
                if ((targetSize.height < height && ImageScale.DOWNSCALE.intersects(imageScale))
                        || (targetSize.height > height && ImageScale.UPSCALE.intersects(imageScale))) {
                    width = width * targetSize.height / height;
                    height = targetSize.height;
                }
            } else if ((targetSize.width < width && ImageScale.DOWNSCALE.intersects(imageScale))
                    || (targetSize.width > width && ImageScale.UPSCALE.intersects(imageScale))) {
                height = height * targetSize.width / width;
                width = targetSize.width;
            }
        }

        public Builder cropTo(ImageSize targetSize) {
            if (width > targetSize.width) {
                width = targetSize.width;
            }
            if (height > targetSize.height) {
                height = targetSize.height;
            }
            return this;
        }

        public Builder padTo(ImageSize targetSize) {
            if (width < targetSize.width) {
                width = targetSize.width;
            }
            if (height < targetSize.height) {
                height = targetSize.height;
            }
            return this;
        }

        public ImageSize build() {
            return new ImageSize(width, height);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Builder)) {
                return false;
            }
            ImageSize other = (ImageSize) obj;
            return width == other.width && height == other.height;
        }

        @Override
        public int hashCode() {
            int result = width;
            result = 31 * result + height;
            return result;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()
                    + "(width=" + (width == SIZE_UNDEFINED ? "undefined" : width)
                    + ", height=" + (height == SIZE_UNDEFINED ? "undefined" : height) + ")";
        }
    }

}
