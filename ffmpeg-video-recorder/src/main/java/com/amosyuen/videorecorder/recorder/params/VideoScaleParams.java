package com.amosyuen.videorecorder.recorder.params;


import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;

import java.io.Serializable;

/**
 * Parameters for video scale fit.
 */
public interface VideoScaleParams extends VideoSizeParams, Serializable {

    /**
     * Get how the video should be scaled to fit the desired width and height. Only used if one of
     * the dimensions in video size are greater than 0.
     */
    ImageFit getVideoImageFit();

    /**
     * Get the video scaling direction that is allowed.
     */
    ImageScale getVideoImageScale();

    interface BuilderI<T extends BuilderI<T>> extends VideoSizeParams.BuilderI<T> {

        /**
         * Set how the video should be scaled to the desired width and height. Only used if one of
         * the dimensions in video size are greater than 0.
         */
        T videoScaleFit(ImageFit val);

        /**
         * Set the video scaling that is allowed to reach the video size.
         */
        T videoScaleDirection(ImageScale val);

        VideoScaleParams build();
    }
}
