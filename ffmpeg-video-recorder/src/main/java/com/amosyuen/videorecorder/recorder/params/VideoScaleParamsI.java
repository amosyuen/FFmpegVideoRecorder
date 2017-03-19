package com.amosyuen.videorecorder.recorder.params;


import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;

import java.io.Serializable;

/**
 * Parameters for video scaling.
 */
public interface VideoScaleParamsI extends VideoSizeParamsI, Serializable {

    /**
     * Get the video scaling to use.
     */
    ImageScale getVideoImageScale();

    /**
     * Get how the video should be scaled to fit the desired width and height. Only used if video
     * size is set.
     */
    ImageFit getVideoImageFit();

    interface BuilderI<T extends BuilderI<T>> extends VideoSizeParamsI.BuilderI<T> {

        /**
         * Set the video scaling to use to reach the video size. Default value is
         * {@link ImageScale#DOWNSCALE}.
         */
        T setVideoImageScale(ImageScale val);

        /**
         * Set how the video should be scaled to the desired width and height. Only used if video
         * size is set. Default value is {@link ImageFit#FILL}.
         */
        T setVideoImageFit(ImageFit val);

        VideoScaleParamsI build();
    }
}
