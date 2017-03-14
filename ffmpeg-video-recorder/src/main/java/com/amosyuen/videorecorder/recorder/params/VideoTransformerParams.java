package com.amosyuen.videorecorder.recorder.params;


import java.io.Serializable;

/**
 * Parameters for transforming video.
 */
public interface VideoTransformerParams extends VideoScaleParams, VideoSizeParams, Serializable {

    /**
     * Whether to allow padding the video to reach the desired size.
     */
    boolean canPadVideo();

    interface BuilderI<T extends BuilderI<T>>
            extends VideoScaleParams.BuilderI<T>, VideoSizeParams.BuilderI<T> {

        /**
         * Set whether to allow padding the video to reach the desired size.
         */
        T canPadVideo(boolean val);

        VideoTransformerParams build();
    }
}
