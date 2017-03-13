package com.amosyuen.videorecorder.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.VideoTransformerParams;
import com.amosyuen.videorecorder.util.VideoUtil;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * View that previews the camera. Scales and limits the view according to the desired dimensions.
 */
public class CameraPreviewView extends SurfaceView {

    protected static final String LOG_TAG = "VideoFrameRecorderView";

    private static final int CAMERA_FOCUS_MIN = -1000;
    private static final int CAMERA_FOCUS_MAX = 1000;

    // User specified params
    protected VideoTransformerParams mParams;
    protected CameraControllerI mCameraController;
    protected UIInteractionListener mUIInteractionListener;
    @Nullable protected ImageSize mPreviewSize;
    protected boolean mIsPreviewing;
    protected float mFocusSize;
    protected int mFocusWeight;

    // Params
    protected ImageSize mScaledPreviewSize;
    protected ImageSize mScaledTargetSize;
    @ColorInt protected int mBlackColor;
    protected Matrix mFocusMatrix;

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    public CameraPreviewView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        mBlackColor = ContextCompat.getColor(getContext(), android.R.color.black);
        setWillNotDraw(false);
    }

    @Nullable
    public VideoTransformerParams getParams() {
        return mParams;
    }

    public void setParams(@NonNull VideoTransformerParams params) {
        mParams = Preconditions.checkNotNull(params);
    }

    public UIInteractionListener getUIInteractionListener() {
        return mUIInteractionListener;
    }

    public void setUIInteractionListener(UIInteractionListener UIInteractionListener) {
        mUIInteractionListener = UIInteractionListener;
    }

    public ImageSize getScaledPreviewSize() {
        return mScaledPreviewSize;
    }

    public ImageSize getScaledTargetSize() {
        return mScaledTargetSize;
    }

    @Nullable
    public ImageSize getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSize(ImageSize previewSize) {
        if (previewSize != mPreviewSize) {
            mPreviewSize = previewSize;
            if (mPreviewSize != null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        requestLayout();
                    }
                });
            } else if (mIsPreviewing) {
                postInvalidate();
            }
        }
    }

    public boolean isPreviewing() {
        return mIsPreviewing;
    }

    public void setIsPreviewing(boolean isPreviewing) {
        if (isPreviewing != mIsPreviewing) {
            mIsPreviewing = isPreviewing;
            postInvalidate();
        }
    }

    public float getFocusSize() {
        return mFocusSize;
    }

    /**
     * Set the focus size. A focus size of 0 disables tap to focus.
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

    @Nullable
    public CameraControllerI getCameraController() {
        return mCameraController;
    }

    public void setCameraController(@NonNull CameraControllerI cameraController) {
        mCameraController = Preconditions.checkNotNull(cameraController);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v(LOG_TAG, "onMeasure");

        // Get the parent size
        ImageSize parentSize;
        if (getParent() instanceof View) {
            View parent = (View) getParent();
            parentSize = new ImageSize(parent.getWidth(), parent.getHeight());
        } else {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            parentSize = new ImageSize(metrics.widthPixels, metrics.heightPixels);
        }
        if (!parentSize.areDimensionsDefined()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        ImageSize targetSize = mParams == null ? ImageSize.UNDEFINED : mParams.getVideoSize();

        // Get the preview size and rotate it into the correct orientation for the view.
        ImageSize.Builder scalePreviewSizeBuilder;
        if (mPreviewSize == null) {
            // If there is no preview size calculate it by scaling the targetSize to fir the parent
            // or use the parent if the target size doesn't have both dimensions defined.
            mPreviewSize = targetSize.areDimensionsDefined()
                    ? targetSize.toBuilder().scaleToFit(parentSize, true).build()
                    : parentSize;
            scalePreviewSizeBuilder = mPreviewSize.toBuilder();
        } else {
            // Rotate the preview orientation if view orientation is portrait
            boolean isLandscape = VideoUtil.isContextLandscape(getContext());
            scalePreviewSizeBuilder = mPreviewSize.toBuilder();
            if (!isLandscape) {
                scalePreviewSizeBuilder.invert();
            }
        }
        Preconditions.checkState(scalePreviewSizeBuilder.areDimensionsDefined());

        // Calculate any undefined dimensions of the target size and scale the preview size to the
        // target size according to the requested params
        ImageSize.Builder scaleTargetSizeBuilder;
        if (targetSize.isAtLeastOneDimensionDefined()) {
            scaleTargetSizeBuilder = targetSize.toBuilder()
                    .calculateUndefinedDimensions(scalePreviewSizeBuilder.build())
                    .roundWidthUpToEvenAndMaintainAspectRatio();
            scalePreviewSizeBuilder.scale(
                    scaleTargetSizeBuilder.build(),
                    mParams.getVideoScaleType(),
                    mParams.canUpscaleVideo());
        } else {
            // No target size was specified, so just use the preview size.
            scaleTargetSizeBuilder = scalePreviewSizeBuilder.clone()
                    .roundWidthUpToEvenAndMaintainAspectRatio();
        }
        Preconditions.checkState(scaleTargetSizeBuilder.areDimensionsDefined());

        // Scale the target to fit within the parent view
        ImageSize preScaleTargetSize = scaleTargetSizeBuilder.build();
        mScaledTargetSize = scaleTargetSizeBuilder.scaleToFit(parentSize, true).build();
        // Scale the preview size by the same amount that target size was scaled
        scalePreviewSizeBuilder.width = scalePreviewSizeBuilder.width
                * mScaledTargetSize.width / preScaleTargetSize.width;
        scalePreviewSizeBuilder.height = scalePreviewSizeBuilder.height
                * mScaledTargetSize.height / preScaleTargetSize.height;
        mScaledPreviewSize = scalePreviewSizeBuilder.build();
        Log.v(LOG_TAG, String.format(
                "Parent=%s Target=%s Preview=%s", parentSize, targetSize, mPreviewSize));
        Log.v(LOG_TAG, String.format(
                "ScaledTarget=%s ScaledPreview=%s", mScaledTargetSize, mScaledPreviewSize));

        setMeasuredDimension(mScaledPreviewSize.width, mScaledPreviewSize.height);

        if (mCameraController.canFocusOnRect()) {
            // Calculate matrix for transforming preview x:y coordinates into the camera -1000:1000
            // coordinate space, taking into account camera rotation and flipping
            // Note: Use float so that division is float division
            float focusSize = CAMERA_FOCUS_MAX - CAMERA_FOCUS_MIN;
            int orientationDegrees =
                    VideoUtil.determineCameraSurfaceOrientation(
                            VideoUtil.getContextOrientationDegrees(getContext()),
                            mCameraController.getCameraOrientationDegrees(),
                            mCameraController.getCameraFacing());
            mFocusMatrix = new Matrix();
            mFocusMatrix.postScale(
                    focusSize / mScaledPreviewSize.width, focusSize / mScaledPreviewSize.height);
            mFocusMatrix.postTranslate(-0.5f * focusSize, -0.5f * focusSize);
            mFocusMatrix.postRotate(-orientationDegrees);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mScaledTargetSize != null) {
            // Clip the drawable bounds to the target size
            float targetMarginX = Math.max(0, 0.5f * (canvas.getWidth() - mScaledTargetSize.width));
            float targetMarginY =
                    Math.max(0, 0.5f * (canvas.getHeight() - mScaledTargetSize.height));
            canvas.clipRect(
                    targetMarginX,
                    targetMarginY,
                    targetMarginX + mScaledTargetSize.width,
                    targetMarginY + mScaledTargetSize.height);
            Log.v(LOG_TAG, String.format("ClipRect left=%f top=%f right=%f bottom=%f",
                    targetMarginX,
                    targetMarginY,
                    targetMarginX + mScaledTargetSize.width,
                    targetMarginY + mScaledTargetSize.height));
        }

        canvas.drawColor(mBlackColor);

        if (mPreviewSize != null && mIsPreviewing) {
            super.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && mFocusMatrix != null
                && mCameraController.canFocusOnRect()
                && mFocusSize > 0) {
            final RectF uiFocusRect = calculateTapArea(event.getX(), event.getY());
            RectF transformedFocusRect = new RectF();
            mFocusMatrix.mapRect(transformedFocusRect, uiFocusRect);
            Rect cameraFocusRect = new Rect(
                    Math.round(transformedFocusRect.left), Math.round(transformedFocusRect.top),
                    Math.round(transformedFocusRect.right), Math.round(transformedFocusRect.bottom));
            Log.v(LOG_TAG, String.format("Tap to focus on %s", cameraFocusRect));

            try {
                mCameraController.focusOnRect(cameraFocusRect, mFocusWeight);
            } catch (Exception e) {
                Log.w(LOG_TAG, "Unable to focus", e);
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     */
    protected RectF calculateTapArea(float x, float y) {
        // Convert UI coordinates to the top and left bound
        ImageSize size = new ImageSize(getWidth(), getHeight());
        float left = x - 0.5f * mFocusSize;
        float top = y - 0.5f * mFocusSize;

        // Convert UI coordinates into scaled target size coordinates
        left += 0.5f * (mScaledTargetSize.width - size.width);
        top += 0.5f * (mScaledTargetSize.height - size.height);

        // Clamp coordinates by scaled target size
        left = Math.max(0, Math.min(mScaledTargetSize.width - mFocusSize, left));
        top = Math.max(0, Math.min(mScaledTargetSize.height - mFocusSize, top));

        // Translate the scaled target size coordinates into the scaled preview size coordinates
        left += 0.5f * (mScaledPreviewSize.width - mScaledTargetSize.width);
        top += 0.5f * (mScaledPreviewSize.height - mScaledTargetSize.height);

        return new RectF(left, top, left + mFocusSize, top + mFocusSize);
    }

    public interface UIInteractionListener {
        void onResize(ImageSize size);
        void onFocusOnRect(RectF uiRect, Rect cameraRect);
    }
}