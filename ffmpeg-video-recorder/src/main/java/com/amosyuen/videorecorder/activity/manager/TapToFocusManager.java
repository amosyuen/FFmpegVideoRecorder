package com.amosyuen.videorecorder.activity.manager;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.ui.CameraPreviewView;
import com.amosyuen.videorecorder.ui.CameraTapAreaView;
import com.google.common.base.Preconditions;

/**
 * Manages logic for camera focus.
 */
public class TapToFocusManager implements
        CameraControllerI.CameraListener,
        CameraTapAreaView.TapAreaListener,
        View.OnLayoutChangeListener {

    private static final String LOG_TAG = "TapToFocusManager";
    // Ranges from -1000 to 1000
    private static final int CAMERA_FOCUS_SIZE = 2000;

    protected final CameraControllerI mCameraController;
    protected final CameraPreviewView mCameraPreviewView;
    protected final CameraTapAreaView mCameraTapAreaView;
    protected final int mFocusWeight;
    protected final long mFocusHoldMillis;
    protected final Runnable mAutoFocusRunnable;
    protected Matrix mFocusMatrix;

    public TapToFocusManager(
            CameraControllerI cameraController,
            CameraPreviewView cameraPreviewView,
            CameraTapAreaView cameraTapAreaView,
            int focusWeight,
            long focusHoldMillis) {
        mCameraController = Preconditions.checkNotNull(cameraController);
        mCameraController.addListener(this);
        mCameraPreviewView = Preconditions.checkNotNull(cameraPreviewView);
        mCameraPreviewView.addOnLayoutChangeListener(this);
        mCameraTapAreaView = Preconditions.checkNotNull(cameraTapAreaView);
        mCameraTapAreaView.addTapAreaListener(this);
        Preconditions.checkArgument(focusWeight >= 1 && focusWeight <= 1000);
        mFocusWeight = focusWeight;
        mFocusHoldMillis = focusHoldMillis;
        mAutoFocusRunnable = new Runnable() {
            @Override
            public void run() {
                mCameraController.autoFocus();
            }
        };
        calculateTransformation();
    }

    public CameraControllerI getCameraController() {
        return mCameraController;
    }

    public CameraPreviewView getCameraPreviewView() {
        return mCameraPreviewView;
    }

    public CameraTapAreaView getCameraTapAreaView() {
        return mCameraTapAreaView;
    }

    public int getFocusWeight() {
        return mFocusWeight;
    }

    public long getFocusHoldMillis() {
        return mFocusHoldMillis;
    }

    @Override
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        mCameraController.removeListener(this);
        mCameraPreviewView.removeOnLayoutChangeListener(this);
        mCameraTapAreaView.removeTapAreaListener(this);
    }

    public void autoFocusAfterDelay() {
        if (!mCameraController.canAutoFocus() || mFocusHoldMillis <= 0) {
            return;
        }
        cancelDelayedAutoFocus();
        mCameraTapAreaView.postDelayed(mAutoFocusRunnable, mFocusHoldMillis);
    }

    public boolean cancelDelayedAutoFocus() {
        return mCameraTapAreaView.removeCallbacks(mAutoFocusRunnable);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom,
            int oldLeft, int oldTop, int oldRight, int oldBottom) {
        calculateTransformation();
    }

    @Override
    public void onCameraOpen() {
        calculateTransformation();
    }

    @Override
    public void onCameraClose() {}

    @Override
    public void onCameraStartPreview() {}

    @Override
    public void onCameraStopPreview() {
        mCameraTapAreaView.clearTapAreaRect();
    }

    @Override
    public void onFlashModeChanged(CameraControllerI.FlashMode flashMode) {}

    @Override
    public void onCameraFocusOnRect(Rect rect) {}

    @Override
    public void onCameraAutoFocus() {
        mCameraTapAreaView.clearTapAreaRect();
    }

    /**
     * Calculate matrix for transforming target x:y coordinates into the camera -1000:1000
     * coordinate space, taking into account camera rotation
     */
    protected void calculateTransformation() {
        CameraPreviewView cameraPreviewView = getCameraPreviewView();
        ImageSize scaledTargetSize = cameraPreviewView.getScaledTargetSize();
        ImageSize scaledPreviewSize = cameraPreviewView.getScaledPreviewSize();
        if (scaledPreviewSize.areBothDimensionsDefined() && mCameraController.isCameraOpen()) {
            mFocusMatrix = new Matrix();
            // Translate target x:y coordinates in [0:size] space to [-size/2:size/2] space
            mFocusMatrix.postTranslate(
                    -0.5f * scaledTargetSize.getWidthUnchecked(),
                    -0.5f * scaledTargetSize.getHeightUnchecked());
            // Scale coordinates into camera space [-1000:1000]
            mFocusMatrix.postScale(
                    (float) CAMERA_FOCUS_SIZE / scaledPreviewSize.getWidthUnchecked(),
                    (float) CAMERA_FOCUS_SIZE / scaledPreviewSize.getHeightUnchecked());
            // Rotate coordinates by camera orientation
            mFocusMatrix.postRotate(-mCameraController.getPreviewDisplayOrientationDegrees());
        } else {
            mFocusMatrix = null;
        }
    }

    @Override
    public void onTapArea(RectF tapAreaRect) {
        if (mFocusMatrix == null || !mCameraController.canFocusOnRect()) {
            mCameraTapAreaView.clearTapAreaRect();
        } else {
            RectF focusRectF = new RectF();
            mFocusMatrix.mapRect(focusRectF, tapAreaRect);
            Rect focusRect = new Rect(
                    Math.round(focusRectF.left), Math.round(focusRectF.top),
                    Math.round(focusRectF.right), Math.round(focusRectF.bottom));

            try {
                mCameraController.focusOnRect(focusRect, mFocusWeight);
                autoFocusAfterDelay();
                Log.v(LOG_TAG, String.format("Camera focus on %s", focusRect));
            } catch (Exception e) {
                Log.w(LOG_TAG, "Unable to focus", e);
            }
        }
    }

    @Override
    public void onClearTapArea() {}
}
