package com.amosyuen.videorecorder.recorder.common;


import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * Immutable class representing an integer based image size. It allows undefined dimensions.
 */
public class ImageSize extends ImageSizeOrBuilder implements Serializable {

    public static final ImageSize UNDEFINED = ImageSize.builder().build();

    protected final int mWidth;
    protected final int mHeight;

    public ImageSize(int width, int height) {
        mWidth = checkDimension(width);
        mHeight = checkDimension(height);
    }

    public ImageSize(Optional<Integer> width, Optional<Integer> height) {
        mWidth = checkDimension(width);
        mHeight = checkDimension(height);
    }

    protected ImageSize(int width, int height, boolean checkDimensions) {
        if (checkDimensions) {
            mWidth = checkDimension(width);
            mHeight = checkDimension(height);
        } else {
            mWidth = width;
            mHeight = height;
        }
    }

    @Override
    public int width() {
        return mWidth;
    }

    @Override
    public int height() {
        return mHeight;
    }

    public Builder toBuilder() {
        return new Builder(mWidth, mHeight);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImageSize)) {
            return false;
        }
        ImageSize other = (ImageSize) obj;
        return mWidth == other.mWidth && mHeight == other.mHeight;
    }

    @Override
    public int hashCode() {
        int result = mWidth;
        result = 31 * result + mHeight;
        return result;
    }

    /**
     * AbstractBuilder class for image size that supports common operations.
     */
    public static class Builder extends ImageSizeOrBuilder {

        protected int mWidth;
        protected int mHeight;

        protected Builder() {
            mWidth = UNDEFINED;
            mHeight = UNDEFINED;
        }

        protected Builder(int width, int height) {
            mWidth = width;
            mHeight = height;
        }

        @Override
        public int width() {
            return mWidth;
        }

        @Override
        public int height() {
            return mHeight;
        }

        public Builder width(int width) {
            mWidth = checkDimension(width);
            return this;
        }

        public Builder width(Optional<Integer> width) {
            mWidth = checkDimension(width);
            return this;
        }

        public Builder height(int height) {
            mHeight = checkDimension(height);
            return this;
        }

        public Builder height(Optional<Integer> height) {
            mHeight = checkDimension(height);
            return this;
        }

        public Builder size(int width, int height) {
            mWidth = checkDimension(width);
            mHeight = checkDimension(height);
            return this;
        }

        public Builder size(Optional<Integer> width, Optional<Integer> height) {
            mWidth = checkDimension(width);
            mHeight = checkDimension(height);
            return this;
        }

        public Builder size(ImageSizeOrBuilder imageSize) {
            mWidth = imageSize.width();
            mHeight = imageSize.height();
            return this;
        }

        /**
         * For each dimensions takes the maximum if the dimension is defined.
         */
        public Builder max(ImageSizeOrBuilder imageSize) {
            int width = imageSize.width();
            int height = imageSize.height();
            if (width != UNDEFINED) {
                mWidth = Math.max(mWidth, width);
            }
            if (height != UNDEFINED) {
                mHeight = Math.max(mHeight, height);
            }
            return this;
        }

        /**
         * For each dimensions takes the minimum if the dimension is defined.
         */
        public Builder min(ImageSizeOrBuilder imageSize) {
            int width = imageSize.width();
            int height = imageSize.height();
            if (width != UNDEFINED) {
                mWidth = mWidth == UNDEFINED ? width : Math.min(mWidth, width);
            }
            if (height != UNDEFINED) {
                mHeight = mHeight == UNDEFINED ? width : Math.min(mHeight, width);
            }
            return this;
        }

        public Builder invert() {
            int temp = mWidth;
            //noinspection SuspiciousNameCombination
            mWidth = mHeight;
            mHeight = temp;
            return this;
        }

        /**
         * Rounds the width up to the nearest even number if defined. If height is defined it scales
         * height to maintain the same aspect ratio.
         */
        public Builder roundWidthUpToEvenAndMaintainAspectRatio() {
            if (mWidth != UNDEFINED) {
                 if (mWidth % 2 != 0) {
                    int roundedWidth = mWidth + 1;
                    if (mHeight != UNDEFINED) {
                        mHeight = (roundedWidth * mHeight + mWidth / 2) / mWidth;
                    }
                    mWidth = roundedWidth;
                }
            }
            return this;
        }

        /**
         * Calculates the undefined dimensions in this size using the aspect ratio from the source
         * size. The size must have at least one dimensions defined.
         * @param source Size whose aspect ratio to use to calculate the undefined dimension. Must
         *               have both dimensions defined.
         */
        public Builder calculateUndefinedDimensions(ImageSizeOrBuilder source) {
            Preconditions.checkState(isAtLeastOneDimensionDefined());
            Preconditions.checkArgument(source.areBothDimensionsDefined());
            if (mHeight != UNDEFINED) {
                if (mWidth == UNDEFINED) {
                    mWidth = source.width() * mHeight / source.height();
                }
            } else if (mWidth != UNDEFINED) {
                mHeight = source.height() * mWidth / source.width();
            }
            return this;
        }

        /**
         * Scale the size to the target size using the image fit if it is the scale allowed by
         * imageScale. Size must have both dimensions defined.
         * @param targetSize Size to scale to. Must have both dimensions defined.
         * @param imageFit How to fit the image to the targetSize.
         * @param imageScale The allowed scale.
         */
        public Builder scale(
                ImageSizeOrBuilder targetSize, ImageFit imageFit, ImageScale imageScale) {
            switch (imageFit) {
                case FILL:
                    scaleToFill(targetSize, imageScale);
                    break;
                case FIT:
                    scaleToFit(targetSize, imageScale);
                    break;
                default:
                    throw new IllegalArgumentException(
                            String.format("Unsupported image fit %s", imageFit));
            }
            return this;
        }

        /**
         * Scale the size to the target size so that it fills the target size if allowed by
         * imageScale. Size must have both dimensions defined.
         * @param targetSize Size to scale to. Must have both dimensions defined.
         * @param imageScale The allowed scale.
         */
        public Builder scaleToFill(ImageSizeOrBuilder targetSize, ImageScale imageScale) {
            scale(targetSize, imageScale, true);
            return this;
        }

        /**
         * Scale the size to the target size so that it fits the target size if allowed by
         * imageScale. Size must have both dimensions defined.
         * @param targetSize Size to scale to. Must have both dimensions defined.
         * @param imageScale The allowed scale.
         */
        public Builder scaleToFit(ImageSizeOrBuilder targetSize, ImageScale imageScale) {
            scale(targetSize, imageScale, false);
            return this;
        }

        protected void scale(ImageSizeOrBuilder targetSize, ImageScale imageScale, boolean isFill) {
            Preconditions.checkArgument(areBothDimensionsDefined());
            Preconditions.checkArgument(targetSize.areBothDimensionsDefined());
            boolean isAspectRatioGreaterThan = targetSize.getAspectRatio() > getAspectRatio();
            if (isAspectRatioGreaterThan ^ isFill) {
                int targetHeight = targetSize.height();
                if ((targetHeight < mHeight && ImageScale.DOWNSCALE.intersects(imageScale))
                        || (targetHeight > mHeight && ImageScale.UPSCALE.intersects(imageScale))) {
                    mWidth = mWidth * targetHeight / mHeight;
                    mHeight = targetHeight;
                }
            } else {
                int targetWidth = targetSize.width();
                if ((targetWidth < mWidth && ImageScale.DOWNSCALE.intersects(imageScale))
                        || (targetWidth > mWidth && ImageScale.UPSCALE.intersects(imageScale))) {
                    mHeight = mHeight * targetWidth / mWidth;
                    mWidth = targetWidth;
                }
            }
        }

        public ImageSize build() {
            return new ImageSize(mWidth, mHeight, false);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ImageSizeOrBuilder)) {
                return false;
            }
            ImageSizeOrBuilder other = (ImageSizeOrBuilder) obj;
            return mWidth == other.width() && mHeight == other.height();
        }
    }
}
