package com.amosyuen.videorecorder.recorder.params;


import java.io.Serializable;

/**
 * Parameters for transforming video.
 */
public interface VideoTransformerParamsI extends VideoScaleParamsI, VideoSizeParamsI, Serializable {

    /**
     * Gets whether the transformation should crop videos larger than the desired size to the
     * desired size.
     */
    boolean getShouldCropVideo();

    /**
     * Gets whether the transformation should pad videos smaller than the desired size to the
     * desired size.
     */
    boolean getShouldPadVideo();

    interface BuilderI<T extends BuilderI<T>>
            extends VideoScaleParamsI.BuilderI<T>, VideoSizeParamsI.BuilderI<T> {

        /**
         * Set whether the transformation should crop videos larger than the desired size to the
         * desired size. Default value is {@code true}.
         */
        T setShouldCropVideo(boolean val);

        /**
         * Set whether the transformation should pad the videos smaller than the desired size to the
         * desired size. Default value is {@code true}.
         */
        T setShouldPadVideo(boolean val);

        VideoTransformerParamsI build();
    }
}
