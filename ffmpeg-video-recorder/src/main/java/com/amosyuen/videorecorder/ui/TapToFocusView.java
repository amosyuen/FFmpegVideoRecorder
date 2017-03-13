package com.amosyuen.videorecorder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.util.VideoUtil;

/**
 * View to draw an indicator for a rectangle.
 */
public class TapToFocusView extends View implements View.OnLayoutChangeListener {

    private static final int CAMERA_FOCUS_MIN = -1000;
    private static final int CAMERA_FOCUS_MAX = 1000;

    protected CameraPreviewView mCameraPreviewView;
    protected Matrix mFocusMatrix;
    protected float mFocusSize;
    protected int mFocusWeight;
    protected Paint mPaint;
    protected RectF mRect;

    public TapToFocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreviewView getCameraPreviewView() {
        return mCameraPreviewView;
    }

    public void setCameraPreviewView(CameraPreviewView cameraPreviewView) {
        if (mCameraPreviewView != null) {
            mCameraPreviewView.removeOnLayoutChangeListener(this);
        }
        mCameraPreviewView = cameraPreviewView;
        if (mCameraPreviewView != null) {
            mCameraPreviewView.addOnLayoutChangeListener(this);
        }
    }

    public float getFocusSize() {
        return mFocusSize;
    }

    /**
     * Set the focus size in pixels. A focus size of 0 disables tap to focus.
     */
    public void setFocusSize(float focusSize) {
        mFocusSize = focusSize;
    }

    public int getFocusWeight() {
        return mFocusWeight;
    }

    public void setFocusWeight(int focusWeight) {
        mFocusWeight = focusWeight;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(@NonNull Paint paint) {
        mPaint = paint;
    }

    public RectF getRect() {
        return mRect;
    }

    public void setRect(@NonNull RectF rect) {
        if (mRect == null || !mRect.equals(rect)) {
            mRect = rect;
            invalidate();
        }
    }

    public void clearRect() {
        if (mRect != null) {
            mRect = null;
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mPaint != null && mRect != null){
            canvas.drawRect(mRect, mPaint);
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int
            oldTop, int oldRight, int oldBottom) {
        if (mCameraPreviewView == null) {
            return;
        }
        CameraControllerI cameraController = mCameraPreviewView.getCameraController();
        if (cameraController == null || !cameraController.canFocusOnRect()) {
            return;
        }
        // Calculate matrix for transforming preview x:y coordinates into the camera -1000:1000
        // coordinate space, taking into account camera rotation and flipping
        // Note: Use float so that division is float division
        float focusSize = CAMERA_FOCUS_MAX - CAMERA_FOCUS_MIN;
        int orientationDegrees =
                VideoUtil.determineCameraSurfaceOrientation(
                        VideoUtil.getContextOrientationDegrees(getContext()),
                        cameraController.getCameraOrientationDegrees(),
                        cameraController.getCameraFacing());
        mFocusMatrix = new Matrix();
        mFocusMatrix.postScale(focusSize / (right - left), focusSize / (top - bottom));
        mFocusMatrix.postTranslate(-0.5f * focusSize, -0.5f * focusSize);
        mFocusMatrix.postRotate(-orientationDegrees);
    }

    /**
     * Convert touch position x:y to camera position -1000:-1000 to 1000:1000.
     */
    protected RectF calculateTapArea(float x, float y) {
        ImageSize scaledTargetSize = mCameraPreviewView.getScaledTargetSize();
        ImageSize scaledPreviewSize = mCameraPreviewView.getScaledPreviewSize();

        // Convert UI coordinates to the top and left bound
        ImageSize size = new ImageSize(getWidth(), getHeight());
        float left = x - 0.5f * mFocusSize;
        float top = y - 0.5f * mFocusSize;

        // Convert UI coordinates into scaled target size coordinates
        left += 0.5f * (scaledTargetSize.width - size.width);
        top += 0.5f * (scaledTargetSize.height - size.height);

        // Clamp coordinates by scaled target size
        left = Math.max(0, Math.min(scaledTargetSize.width - mFocusSize, left));
        top = Math.max(0, Math.min(scaledTargetSize.height - mFocusSize, top));

        // Translate the scaled target size coordinates into the scaled preview size coordinates
        left += 0.5f * (scaledPreviewSize.width - scaledTargetSize.width);
        top += 0.5f * (scaledPreviewSize.height - scaledTargetSize.height);

        return new RectF(left, top, left + mFocusSize, top + mFocusSize);
    }
}
