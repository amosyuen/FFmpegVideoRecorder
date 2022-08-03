package com.amosyuen.videorecorder.camera;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;

import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.CameraParamsI;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Camera manager for camera 1 API.
 */
public class CameraController implements CameraControllerI {

    protected static final String LOG_TAG = "CameraController";

    // Note: ImmutableSet keeps the order that items are passed in.
    protected static final ImmutableSet<String> FOCUS_MODE_PREFERRED_ORDER = ImmutableSet.of(
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
            Camera.Parameters.FOCUS_MODE_FIXED
    );

    protected static final ImmutableBiMap<Facing, Integer> FACING_MAP = ImmutableBiMap.of(
            Facing.BACK, Camera.CameraInfo.CAMERA_FACING_BACK,
            Facing.FRONT, Camera.CameraInfo.CAMERA_FACING_FRONT
    );

    // Note: Parameters keeps the order that items are passed in.
    protected static final ImmutableSetMultimap<FlashMode, String> FLASH_MODE_MAP =
            ImmutableSetMultimap.of(
                    FlashMode.AUTO, Camera.Parameters.FLASH_MODE_AUTO,
                    FlashMode.OFF, Camera.Parameters.FLASH_MODE_OFF,
                    FlashMode.ON, Camera.Parameters.FLASH_MODE_TORCH,
                    FlashMode.ON, Camera.Parameters.FLASH_MODE_ON
            );
    protected static final ImmutableSetMultimap<String, FlashMode> FLASH_MODE_INVERSE_MAP =
            FLASH_MODE_MAP.inverse();

    protected Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    @Nullable protected Camera mCamera;
    protected Camera.Parameters mParameters;
    protected int mPreviewDisplayOrientationDegrees;
    protected CameraPreviewCallback mCameraPreviewCallback = new CameraPreviewCallback();
    protected boolean mIsPreviewing;
    protected List<CameraListener> mListeners = new ArrayList<>();

    @Override
    public void addListener(CameraListener listener) {
        mListeners.add(listener);
    }

    @Override
    public boolean removeListener(CameraListener listener) {
        return mListeners.remove(listener);
    }

    @Override
    public synchronized boolean isCameraOpen() {
        return mCamera != null;
    }

    @Override
    public int getCameraCount() {
        return Camera.getNumberOfCameras();
    }

    @Nullable
    public synchronized Camera getCamera() {
        return mCamera;
    }

    @Override
    public synchronized void openCamera(CameraParamsI params, int surfaceRotationDegrees) {
        if (mCamera != null) {
            closeCamera();
        }

        Log.d(LOG_TAG, String.format(
                "Opening camera params %s orientation %d", params, surfaceRotationDegrees));
        // Choose a camera matching the desired facing
        Facing cameraFacing = Preconditions.checkNotNull(params.getVideoCameraFacing());
        Camera camera;
        if (mCamera == null || getCameraFacing() != cameraFacing) {
            if (mCamera != null) {
                closeCamera();
            }
            int camera1Facing = FACING_MAP.get(cameraFacing);
            int cameraId = -1;
            int numberOfCameras = getCameraCount();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, mCameraInfo);
                if (mCameraInfo.facing == camera1Facing) {
                    cameraId = i;
                    break;
                }
            }
            // If we didn't find a camera with our desired facing, choose the first one
            if (cameraId < 0) {
                Log.v(LOG_TAG, "Didn't find a camera matching facing. Choosing camera 0");
                cameraId = 0;
                Camera.getCameraInfo(cameraId, mCameraInfo);
                cameraFacing = FACING_MAP.inverse().get(mCameraInfo.facing);
            }
            Log.v(LOG_TAG, String.format("Opening camera id %d", cameraId));
            camera = Camera.open(cameraId);
        } else {
            camera = mCamera;
        }
        Camera.Parameters parameters = camera.getParameters();

        // Display orientation
        mPreviewDisplayOrientationDegrees = CameraUtil.determineCameraDisplayRotation(
                surfaceRotationDegrees, mCameraInfo.orientation, cameraFacing);
        camera.setDisplayOrientation(mPreviewDisplayOrientationDegrees);
        Log.v(LOG_TAG, String.format(
                "Set camera display orientation to %d", mPreviewDisplayOrientationDegrees));

        // Preview and picture sizes
        ImageSize targetSize = params.getVideoSize();
        if (targetSize.isAtLeastOneDimensionDefined()) {
            if (com.amosyuen.videorecorder.util.Util.isLandscapeAngle(surfaceRotationDegrees)) {
                targetSize = targetSize.toBuilder().invert().build();
            }
            ImageFit imageFit = params.getVideoImageFit();
            ImageScale videoImageScale = params.getVideoImageScale();
            ImageSize bestPictureSize = getBestImageSize(
                    camera.getParameters().getSupportedPictureSizes(),
                    targetSize, imageFit, videoImageScale);
            parameters.setPictureSize(
                    bestPictureSize.getWidthUnchecked(), bestPictureSize.getHeightUnchecked());
            Log.v(LOG_TAG, String.format("Set camera picture size %s", bestPictureSize));

            ImageSize bestPreviewSize = getBestImageSize(
                    camera.getParameters().getSupportedPreviewSizes(),
                    bestPictureSize, imageFit, videoImageScale);
            parameters.setPreviewSize(
                    bestPreviewSize.getWidthUnchecked(), bestPreviewSize.getHeightUnchecked());
            Log.v(LOG_TAG, String.format("Set camera preview size %s", bestPreviewSize));
        }

        // Preview frame rate
        if (params.getVideoFrameRate().isPresent()) {
            List<int[]> fpsRanges = parameters.getSupportedPreviewFpsRange();
            int[] bestFpsRange =
                    CameraUtil.getBestFpsRange(fpsRanges, params.getVideoFrameRate().get());

            for (int[] range : fpsRanges)
                Log.v(LOG_TAG, String.format(
                        "FPS Range [%d,%d]", range[0], range[1]));
            parameters.setPreviewFpsRange(bestFpsRange[0], bestFpsRange[1]);
            Log.v(LOG_TAG, String.format(
                    "Set camera fps range to [%d, %d]", bestFpsRange[0], bestFpsRange[1]));
        }

        // Flash Mode
        FlashMode flashMode = FlashMode.OFF;
        setFlashModeParams(flashMode, parameters);

        // Exposure
        if (parameters.isAutoExposureLockSupported()) {
            parameters.setAutoExposureLock(false);
        }

        // Scene Mode
        if (parameters.getSupportedSceneModes()!=null && parameters.getSupportedSceneModes().contains(Parameters.SCENE_MODE_AUTO)) {
            parameters.setSceneMode(Parameters.SCENE_MODE_AUTO);
        }

        // White Balance Mode
        if (parameters.getSupportedWhiteBalance().contains(Parameters.WHITE_BALANCE_AUTO)) {
            parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
        }
        if (parameters.isAutoWhiteBalanceLockSupported()) {
            parameters.setAutoWhiteBalanceLock(false);
        }

        camera.setParameters(parameters);

        mCamera = camera;
        mParameters = parameters;
        for (CameraListener listener : mListeners) {
            listener.onFlashModeChanged(flashMode);
        }

        Log.d(LOG_TAG, "Opened camera");
        for (CameraListener listener : mListeners) {
            listener.onCameraOpen();
        }
    }

    protected static ImageSize getBestImageSize(
            List<Camera.Size> supportedSizes,
            ImageSize targetSize, ImageFit imageFit, ImageScale videoImageScale) {
        List<ImageSize> pictureSizes =
                Lists.newArrayListWithCapacity(supportedSizes.size());
        for (Camera.Size size : supportedSizes) {
            pictureSizes.add(new ImageSize(size.width, size.height));
        }
        return CameraUtil.getBestResolution(pictureSizes, targetSize, imageFit, videoImageScale);
    }

    @Override
    public synchronized void closeCamera() {
        if (isCameraOpen()) {
            stopPreview();
            mCamera.release();
            mCamera = null;
            mParameters = null;

            for (CameraListener listener : mListeners) {
                listener.onCameraClose();
            }
        }
    }

    @Override
    public synchronized void lock() {
        if (!isCameraOpen()) {
            return;
        }
        mCamera.lock();
    }

    @Override
    public synchronized void unlock() {
        if (!isCameraOpen()) {
            return;
        }
        mCamera.unlock();
    }

    @Override
    public ImageSize getPreviewSize() {
        if (mParameters == null) {
            return null;
        }
        Camera.Size size = mParameters.getPreviewSize();
        return new ImageSize(size.width, size.height);
    }

    @Override
    public ImageSize getPictureSize() {
        if (mParameters == null) {
            return null;
        }
        Camera.Size size = mParameters.getPictureSize();
        return new ImageSize(size.width, size.height);
    }

    @Override
    public synchronized void setMediaRecorder(MediaRecorder recorder) {
        if (!isCameraOpen()) {
            return;
        }
        recorder.setCamera(mCamera);
    }

    @Override
    public synchronized void setPreviewCallback(@Nullable final PreviewCallback callback) {
        if (!isCameraOpen()) {
            return;
        }
        mCameraPreviewCallback.setPreviewCallback(callback);
        
    }

    public synchronized void setPreviewDisplay(@Nullable SurfaceHolder holder) throws IOException {
        if (!isCameraOpen()) {
            return;
        }
        mCamera.setPreviewDisplay(holder);
       
    }

    @Override
    public synchronized void startPreview() {
        if (!isCameraOpen() || mIsPreviewing) {
            return;
        }
        mCamera.startPreview();
        mCamera.setPreviewCallback(mCameraPreviewCallback);
        mCameraPreviewCallback.setFirstCall(true);
        mIsPreviewing = true;

        // Start with auto focus. Must be called after starting preview
        autoFocus();
    }

    @Override
    public synchronized void stopPreview() {
        if (!isCameraOpen() || !mIsPreviewing) {
            return;
        }
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mIsPreviewing = false;
        for (CameraListener listener : mListeners) {
            listener.onCameraStopPreview();
        }
    }

    @Override
    public Facing getCameraFacing() {
        return mParameters == null ? null : FACING_MAP.inverse().get(mCameraInfo.facing);
    }

    @Override
    public int getCameraOrientationDegrees() {
        return mParameters == null ? -1 : mCameraInfo.orientation;
    }

    @Override
    public int getPreviewDisplayOrientationDegrees() {
        return mParameters == null ? -1 : mPreviewDisplayOrientationDegrees;
    }

    @Override
    @Nullable
    public int[] getFrameRateRange() {
        if (mParameters == null) {
            return null;
        }
        int[] frameRateRange = new int[2];
        mParameters.getPreviewFpsRange(frameRateRange);
        return frameRateRange;
    }

    @Override
    public FlashMode getFlashMode() {
        if (mParameters == null) {
            return null;
        }
        ImmutableSet<FlashMode> values =
                FLASH_MODE_INVERSE_MAP.get(mParameters.getFlashMode());
        return values.isEmpty() ? null : values.iterator().next();
    }

    @Override
    public boolean supportsFlashMode(FlashMode flashMode) {
        return mParameters != null
                && mParameters.getSupportedFlashModes() != null
                && !Sets.intersection(
                        ImmutableSet.copyOf(mParameters.getSupportedFlashModes()),
                        FLASH_MODE_MAP.get(flashMode))
                .isEmpty();
    }

    @Override
    public synchronized boolean setFlashMode(FlashMode flashMode) {
        if (isCameraOpen()) {
            Camera.Parameters parameters = mCamera.getParameters();
            boolean setFlashMode = setFlashModeParams(flashMode, parameters);
            if (setFlashMode) {
                mCamera.setParameters(parameters);
                mParameters = parameters;
            }
            if (setFlashMode) {
                for (CameraListener listener : mListeners) {
                    listener.onFlashModeChanged(flashMode);
                }
            }
        }
        return false;
    }

    protected synchronized boolean setFlashModeParams(FlashMode flashMode, Camera.Parameters params) {
        if (params.getSupportedFlashModes() != null) {
            Set<String> supportedFlashModes = ImmutableSet.copyOf(params.getSupportedFlashModes());
            for (String cameraFlashMode : FLASH_MODE_MAP.get(flashMode)) {
                if (supportedFlashModes.contains(cameraFlashMode)) {
                    if (params.getFlashMode().equals(cameraFlashMode)) {
                        return false;
                    }
                    params.setFlashMode(cameraFlashMode);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canFocusOnRect() {
        return mParameters != null && mParameters.getMaxNumFocusAreas() > 0;
    }

    @Override
    public synchronized boolean focusOnRect(Rect rect, int focusWeight) {
        if (isCameraOpen()) {
            Log.v(LOG_TAG, String.format("Focus on %s with weight %d", rect, focusWeight));
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
            focusAreas.add(new Camera.Area(rect, focusWeight));
            parameters.setFocusAreas(focusAreas);
            if (parameters.getMaxNumMeteringAreas() > 0) {
                parameters.setMeteringAreas(focusAreas);
            }

            mCamera.cancelAutoFocus();
            mCamera.setParameters(parameters);
            mParameters = parameters;
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mCamera.cancelAutoFocus();
                    Log.v(LOG_TAG, "Finished focusing");
                }
            });

            for (CameraListener listener : mListeners) {
                listener.onCameraFocusOnRect(rect);
            }
        }
        return true;
    }

    @Override
    public boolean canAutoFocus() {
        return mParameters != null
                && !Sets.intersection(
                        ImmutableSet.copyOf(mParameters.getSupportedFocusModes()),
                        FOCUS_MODE_PREFERRED_ORDER)
                .isEmpty();
    }

    @Override
    public synchronized boolean autoFocus() {
        if (isCameraOpen()) {
            Camera.Parameters parameters = mCamera.getParameters();
            ImmutableSet<String> focusModes =
                    ImmutableSet.copyOf(parameters.getSupportedFocusModes());
            String selectedFocusMode = null;
            for (String focusMode : FOCUS_MODE_PREFERRED_ORDER) {
                if (focusModes.contains(focusMode)) {
                    selectedFocusMode = focusMode;
                    break;
                }
            }
            if (selectedFocusMode != null) {
                parameters.setFocusMode(selectedFocusMode);
                mCamera.setParameters(parameters);
                mParameters = parameters;
                mCamera.autoFocus(null);
                for (CameraListener listener : mListeners) {
                    listener.onCameraAutoFocus();
                }
                return true;
            }
        }
        return false;
    }

    protected class CameraPreviewCallback implements Camera.PreviewCallback {

        protected boolean mIsFirstCall = true;
        protected PreviewCallback mPreviewCallback;

        public boolean isFirstCall() {
            return mIsFirstCall;
        }

        public void setFirstCall(boolean firstCall) {
            mIsFirstCall = firstCall;
        }

        public PreviewCallback getPreviewCallback() {
            return mPreviewCallback;
        }

        public void setPreviewCallback(PreviewCallback previewCallback) {
            mPreviewCallback = previewCallback;
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mIsFirstCall) {
                mIsFirstCall = false;
                for (CameraListener listener : mListeners) {
                    listener.onCameraStartPreview();
                }
            }
            if (mPreviewCallback != null) {
                mPreviewCallback.onPreviewFrame(data);
            }
        }
    }
}
