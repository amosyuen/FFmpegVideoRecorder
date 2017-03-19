package com.amosyuen.videorecorder.recorder.params;


import java.io.Serializable;

/**
 * Parameters for recording video.
 */
public interface RecorderParamsI extends
        CameraParamsI,
        EncoderParamsI,
        VideoFrameRateParamsI,
        VideoScaleParamsI,
        VideoSizeParamsI,
        VideoTransformerParamsI,
        Serializable {

    interface BuilderI<T extends BuilderI<T>> extends
            CameraParamsI.BuilderI<T>,
            EncoderParamsI.BuilderI<T>,
            VideoFrameRateParamsI.BuilderI<T>,
            VideoScaleParamsI.BuilderI<T>,
            VideoSizeParamsI.BuilderI<T>,
            VideoTransformerParamsI.BuilderI<T> {

        @Override
        RecorderParamsI build();
    }
}