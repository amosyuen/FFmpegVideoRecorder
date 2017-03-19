package com.amosyuen.videorecorder.camera;

import android.graphics.Rect;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;

import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.CameraParamsI;

import java.io.IOException;

/**
 * Controller interface for a camera.
 */
public interface CameraControllerI {

    /**
     * Adds a camera listener.
     */
    void addListener(CameraListener listener);

    /**
     * Returns whether the specified camera listener was removed.
     */
    boolean removeListener(CameraListener listener);

    /**
     * Return whether a camera is currently open.
     */
    boolean isCameraOpen();

    /**
     * Return the number of cameras.
     */
    int getCameraCount();

    /**
     * Open the camera that best matches the specified params.
     * This should be run on a background thread to avoid blocking the UI thread.
     */
    void openCamera(CameraParamsI params, int surfaceRotationDegrees);

    /**
     * Close the camera and free up resources.
     */
    void closeCamera();

    /**
     * Lock the camera for use only on the current thread.
     */
    void lock();

    /**
     * Unlock the camera so that it can be locked by another thread.
     */
    void unlock();

    /**
     * Return the preview size of the camera.
     */
    ImageSize getPreviewSize();

    /**
     * Return the picture size of the camera.
     */
    ImageSize getPictureSize();

    /**
     * Set the media recorder to use the camera.
     */
    void setMediaRecorder(MediaRecorder recorder);

    /**
     * Sets a callback that will be called on every frame of the preview.
     */
    void setPreviewCallback(@Nullable PreviewCallback callback);

    /**
     * Sets a display to preview the camera.
     */
    void setPreviewDisplay(@Nullable SurfaceHolder holder) throws IOException;

    /**
     * Start previewing the camera.
     */
    void startPreview();

    /**
     * Stop previewing the camera.
     */
    void stopPreview();

    /**
     * Return the camera facing.
     */
    Facing getCameraFacing();

    /**
     * Return the camera orientation in degrees.
     */
    int getCameraOrientationDegrees();

    /**
     * Return the camera preview display orientation in degrees.
     */
    int getPreviewDisplayOrientationDegrees();

    /**
     * Return the camera frame rate range.
     */
    int[] getFrameRateRange();

    /**
     * Return the current flash mode.
     */
    FlashMode getFlashMode();

    /**
     * Returns whether the camera supports the specified flash mode.
     */
    boolean supportsFlashMode(FlashMode flashMode);

    /**
     * Sets the flash mode.
     * @return whether the camera was able to set the flash mode.
     */
    boolean setFlashMode(FlashMode flashMode);

    /**
     * Returns whether the camera supports focusing on a rectangle.
     */
    boolean canFocusOnRect();

    /**
     * Set camera to focus on a rectangle in camera space with specified weight.
     * @param rect Should be in the space of -1000:-1000 to 1000:1000
     * @param focusWeight
     * @return whether the camera was able to focus on a rect.
     */
    boolean focusOnRect(Rect rect, int focusWeight);

    /**
     * Returns whether the camera supports auto focusing.
     */
    boolean canAutoFocus();

    /**
     * Set camera to auto focus.
     * @return whether the camera was able to autofocus.
     */
    boolean autoFocus();

    /**
     * Enum for the direction the camera is facing.
     */
    enum Facing {
        BACK,
        FRONT,
    }

    /**
     * Enum for the camera flash mode.
     */
    enum FlashMode {
        AUTO,
        OFF,
        ON,
    }

    interface CameraListener {
        void onCameraOpen();
        void onCameraClose();
        void onCameraStartPreview();
        void onCameraStopPreview();
        void onFlashModeChanged(FlashMode flashMode);
        void onCameraFocusOnRect(Rect rect);
        void onCameraAutoFocus();
    }

    interface PreviewCallback {
        void onPreviewFrame(byte[] data);
    }
}
