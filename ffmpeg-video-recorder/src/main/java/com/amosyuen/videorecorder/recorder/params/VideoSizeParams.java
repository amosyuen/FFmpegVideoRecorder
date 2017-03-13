package com.amosyuen.videorecorder.recorder.params;


import com.amosyuen.videorecorder.recorder.common.ImageSize;

import java.io.Serializable;

/**
 * Parameters for video size.
 */
public interface VideoSizeParams extends Serializable {

    /**
     * Desired video size. A value of 0 will default to the camera resolution dimension.
     */
    ImageSize getVideoSize();

    interface BuilderI<T extends BuilderI<T>> {

        /**
         * Width will be rounded up to the closest multiple of 2. Height will be recalculated to
         * maintain the aspect ratio. Recommended to use a multiple of 16 for best encoding
         * efficiency.
         */
        T videoWidth(int val);

        /**
         * The height will be changed to make sure the width is a multiple of 2 while maintain the
         * desired aspect ratio.
         */
        T videoHeight(int val);

        /**
         * Width will be rounded up to the closest multiple of 2. Height will be recalculated to
         * maintain the aspect ratio. Recommended to use a multiple of 16 for the width for the best
         * encoding efficiency.
         */
        T videoSize(int width, int height);

        /**
         * Width will be rounded up to the closest multiple of 2. Height will be recalculated to
         * maintain the aspect ratio. Recommended to use a multiple of 16 for the width for the best
         * encoding efficiency.
         */
        T videoSize(ImageSize imageSize);

        VideoSizeParams build();
    }
}
