package com.amosyuen.videorecorder.recorder.params;


import com.amosyuen.videorecorder.recorder.common.ScaleType;

import java.io.Serializable;

/**
 * Parameters for transforming video.
 */
public interface VideoTransformerParams extends VideoSizeParams, Serializable {

    /**
     * How the video should be scaled to the desired width and height. Only used if one of
     * the dimensions in video size are greater than 0.
     */
    ScaleType getVideoScaleType();

    /**
     * Whether to allow upscaling the video to reach the desired size.
     */
    boolean canUpscaleVideo();

    /**
     * Whether to allow padding the video to reach the desired size.
     */
    boolean canPadVideo();

    interface BuilderI<T extends BuilderI<T>> extends VideoSizeParams.BuilderI<T> {

        /**
         * Set how the video should be scaled to the desired width and height. Only used if one of
         * the dimensions in video size are greater than 0.
         */
        T videoScaleType(ScaleType val);

        /**
         * Set whether to allow upscaling the video to reach the desired size.
         */
        T canUpscaleVideo(boolean val);

        /**
         * Set whether to allow padding the video to reach the desired size.
         */
        T canPadVideo(boolean val);

        VideoTransformerParams build();
    }
}
