package com.amosyuen.videorecorder.recorder.common;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * Shared logic for ImageSize and its Builder.
 */
public abstract class ImageSizeOrBuilder {

    protected static int UNDEFINED = 0;

    protected abstract int width();

    /**
     * Returns the width. Will throw an exception if the width is undefined.
     */
    public int getWidthUnchecked() {
        int width = width();
        Preconditions.checkState(width != UNDEFINED);
        return width;
    }

    public Optional<Integer> getWidth() {
        int width = width();
        return width == UNDEFINED ? Optional.<Integer>absent() : Optional.of(width);
    }

    protected abstract int height();

    /**
     * Returns the height. Will throw an exception if the height is undefined.
     */
    public int getHeightUnchecked() {
        int height = height();
        Preconditions.checkState(height != UNDEFINED);
        return height;
    }

    public Optional<Integer> getHeight() {
        int height = height();
        return height == UNDEFINED ? Optional.<Integer>absent() : Optional.of(height);
    }

    /**
     * Returns whether both dimensions are defined.
     */
    public boolean areBothDimensionsDefined() {
        return width() != UNDEFINED && height() != UNDEFINED;
    }

    /**
     * Returns whether at least one dimensions is defined.
     */
    public boolean isAtLeastOneDimensionDefined() {
        return width() != UNDEFINED || height() != UNDEFINED;
    }

    /**
     * Returns whether at least one dimensions is defined.
     */
    public boolean isOneDimensionDefined() {
        return (width() != UNDEFINED) ^ (height() != UNDEFINED);
    }

    /**
     * Returns the aspect ratio of width divided by height. Some edge cases:
     * <ul>
     * <li>If no dimension is defined then the aspect ratio will be {@link Float#NaN}</li>
     * <li>If only height is defined then the aspect ratio will be 0</li>
     * <li>If only width is defined then the aspect ratio will be
     * {@link Float#POSITIVE_INFINITY}</li>
     * </ul>
     */
    public float getAspectRatio() {
        int width = width();
        int height = height();
        return (float) (width == UNDEFINED ? 0 : width) / (height == UNDEFINED ? 0 : height);
    }

    /**
     * Return the width multipled by the height. If either dimension is not defined the area
     * will be 0.
     */
    public long getArea() {
        int width = width();
        int height = height();
        return (long) (width == UNDEFINED ? 0 : width) * (height == UNDEFINED ? 0 : height); }

    protected static int checkDimension(int dimension) {
        Preconditions.checkArgument(dimension >= 0);
        return dimension;
    }

    protected static int checkDimension(Optional<Integer> dimension) {
        if (Preconditions.checkNotNull(dimension).isPresent()) {
            int size = dimension.get();
            Preconditions.checkArgument(size >= 0);
            return size;
        }
        return UNDEFINED;
    }

    @Override
    public int hashCode() {
        int result = width();
        result = 31 * result + height();
        return result;
    }

    @Override
    public String toString() {
        int width = width();
        int height = height();
        return getClass().getSimpleName()
                + "(width=" + (width == UNDEFINED ? "absent" : width )
                + ", height=" + (height == UNDEFINED ? "absent" : height) + ")";
    }
}
