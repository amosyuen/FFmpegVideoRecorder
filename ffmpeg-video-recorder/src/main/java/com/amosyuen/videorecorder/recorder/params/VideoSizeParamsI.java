package com.amosyuen.videorecorder.recorder.params;


import com.amosyuen.videorecorder.recorder.common.ImageSize;

import java.io.Serializable;

/**
 * Parameters for video size.
 */
public interface VideoSizeParamsI extends Serializable {

    /**
     * Get the desired video size.
     */
    ImageSize getVideoSize();

    interface BuilderI<T extends BuilderI<T>> {

        /**
         * Set the desired video size. Undefined dimensions will use the camera size.
         *
         * NOTE: Because of limitations in the library, width will be rounded up to the closest
         * multiple of 2. Height will be recalculated to maintain the aspect ratio.
         */
        T setVideoSize(ImageSize imageSize);

        VideoSizeParamsI build();
    }
}
