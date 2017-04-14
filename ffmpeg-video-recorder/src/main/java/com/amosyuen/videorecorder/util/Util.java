package com.amosyuen.videorecorder.util;


import android.media.MediaRecorder;

import com.amosyuen.videorecorder.activity.params.InteractionParamsI;
import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.FFmpegFrameRecorder;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.EncoderParamsI;
import com.google.common.base.Preconditions;

import java.io.File;

/**
 * General utilities
 */
public class Util {

    public static boolean isLandscapeAngle(int orientationDegrees) {
        return (orientationDegrees > 45 && orientationDegrees < 135)
                || (orientationDegrees > 225 && orientationDegrees < 315);
    }

    public static int roundOrientation(int orientation) {
        return ((orientation + 45) / 90 * 90) % 360;
    }

    public static void setMediaRecorderEncoderParams(
            MediaRecorder recorder, EncoderParamsI params) {
        // Sources must be set first
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Output format second
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        if (params.getAudioChannelCount().isPresent()) {
            recorder.setAudioChannels(params.getAudioChannelCount().get());
        }
        if (params.getAudioBitrate().isPresent()) {
            recorder.setAudioEncodingBitRate(params.getAudioBitrate().get());
        }
        if (params.getAudioSamplingRateHz().isPresent()) {
            recorder.setAudioSamplingRate(params.getAudioSamplingRateHz().get());
        }

        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if (params.getVideoBitrate().isPresent()) {
            recorder.setVideoEncodingBitRate(params.getVideoBitrate().get());
        }
    }

    public static void setMediaRecorderInteractionParams(
            MediaRecorder recorder, InteractionParamsI params,
            int millisRecorded,
            long bytesRecorded) {
        if (params.getMinRecordingMillis() > 0) {
            int remainingMillis = params.getMaxRecordingMillis() - millisRecorded;
            Preconditions.checkArgument(remainingMillis > 0);
            recorder.setMaxDuration(remainingMillis);
        }
        if (params.getMaxFileSizeBytes() > 0) {
            long remainingBytes = params.getMaxFileSizeBytes() - bytesRecorded;
            Preconditions.checkArgument(remainingBytes > 0);
            recorder.setMaxFileSize(remainingBytes);
        }
    }

    /**
     * Some params should be set directly from the camera as they may not exactly match the desired
     * params.
     */
    public static void setMediaRecorderCameraParams(
            MediaRecorder recorder, CameraControllerI cameraController) {
        ImageSize pictureSize = cameraController.getPictureSize();
        recorder.setVideoSize(pictureSize.getWidthUnchecked(), pictureSize.getHeightUnchecked());
        recorder.setOrientationHint(cameraController.getPreviewDisplayOrientationDegrees());

        int[] frameRateRange = cameraController.getFrameRateRange();
        int fps = frameRateRange[1] / 1000;
        recorder.setVideoFrameRate(fps);
    }

    public static FFmpegFrameRecorder createFrameRecorder(File outputFile, EncoderParamsI params) {
        // Task transformer will set the audio channel count based on input files if it is < 0.
        FFmpegFrameRecorder recorder =
                new FFmpegFrameRecorder(outputFile, params.getAudioChannelCount().or(-1));

        recorder.setVideoCodec(params.getVideoCodec().ffmpegCodecValue);
        if (params.getVideoBitrate().isPresent()) {
            recorder.setVideoBitrate(params.getVideoBitrate().get());
        }
        if (params.getVideoFrameRate().isPresent()) {
            recorder.setFrameRate(params.getVideoFrameRate().get());
        }

        recorder.setAudioCodec(params.getAudioCodec().ffmpegCodecValue);
        if (params.getAudioBitrate().isPresent()) {
            recorder.setAudioBitrate(params.getAudioBitrate().get());
        }
        if (params.getAudioSamplingRateHz().isPresent()) {
            recorder.setSampleRate(params.getAudioSamplingRateHz().get());
        }

        recorder.setFormat(params.getOutputFormat().getFileExtension());

        return recorder;
    }
}
