package com.amosyuen.videorecorder.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.amosyuen.videorecorder.R;
import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.VideoTransformerParamsI;
import com.google.common.base.Preconditions;

/**
 * View that previews the camera. Scales and limits the view according to the desired dimensions.
 */
public class CameraPreviewView extends RelativeLayout {

    protected static final String LOG_TAG = "CameraPreviewView";

    private static final int CAMERA_FOCUS_MIN = -1000;
    private static final int CAMERA_FOCUS_MAX = 1000;

    // User specified params
    protected VideoTransformerParamsI mParams;
    protected CameraControllerI mCameraController;
    protected UIInteractionListener mUIInteractionListener;
    @Nullable protected ImageSize mPreviewSize = ImageSize.UNDEFINED;
    protected boolean mIsPreviewing;
    protected float mFocusSize;
    protected int mFocusWeight;

    // Params
    protected SurfaceView mSurfaceView;
    protected ImageSize mScaledPreviewSize = ImageSize.UNDEFINED;
    protected ImageSize mScaledTargetSize = ImageSize.UNDEFINED;
    protected Matrix mFocusMatrix;

    public CameraPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    public CameraPreviewView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    protected void init(Context context) {
        inflate(context, R.layout.view_camera_preview, this);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        // Needed in order to clip the bounds
        mSurfaceView.setWillNotDraw(false);
    }

    @Nullable
    public VideoTransformerParamsI getParams() {
        return mParams;
    }

    public void setParams(@NonNull VideoTransformerParamsI params) {
        mParams = Preconditions.checkNotNull(params);
    }

    public UIInteractionListener getUIInteractionListener() {
        return mUIInteractionListener;
    }

    public void setUIInteractionListener(UIInteractionListener UIInteractionListener) {
        mUIInteractionListener = UIInteractionListener;
    }

    public SurfaceHolder getHolder() {
        return mSurfaceView.getHolder();
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

    public void setPreviewSize(@NonNull ImageSize previewSize) {
        Preconditions.checkArgument(
                !Preconditions.checkNotNull(previewSize).isOneDimensionDefined());
        if (!mPreviewSize.equals(previewSize)) {
            mPreviewSize = previewSize;
            if (mPreviewSize.areBothDimensionsDefined()) {
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

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get the parent size
        ImageSize parentSize;
        if (getParent() instanceof View) {
            View parent = (View) getParent();
            parentSize = new ImageSize(parent.getWidth(), parent.getHeight());
        } else {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            parentSize = new ImageSize(metrics.widthPixels, metrics.heightPixels);
        }
        if (!parentSize.areBothDimensionsDefined()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        ImageSize targetSize = mParams == null ? ImageSize.UNDEFINED : mParams.getVideoSize();

        // Get the preview size and rotate it into the correct orientation for the view.
        ImageSize.Builder scalePreviewSizeBuilder;
        if (mPreviewSize.areBothDimensionsDefined()) {
            // Rotate the preview orientation if view orientation is portrait
            boolean isLandscape = ViewUtil.isContextLandscape(getContext());
            scalePreviewSizeBuilder = mPreviewSize.toBuilder();
            if (!isLandscape) {
                scalePreviewSizeBuilder.invert();
            }
        } else {
            // If there is no preview size calculate it by scaling the targetSize to fir the parent
            // or use the parent if the target size doesn't have both dimensions defined.
            mPreviewSize = targetSize.areBothDimensionsDefined()
                    ? targetSize.toBuilder()
                            .scaleToFit(parentSize, ImageScale.ANY).build()
                    : parentSize;
            scalePreviewSizeBuilder = mPreviewSize.toBuilder();
        }
        Preconditions.checkState(scalePreviewSizeBuilder.areBothDimensionsDefined());

        // Calculate any undefined dimensions of the target size and scale the preview size to the
        // target size according to the requested params
        ImageSize.Builder scaleTargetSizeBuilder;
        if (targetSize.isAtLeastOneDimensionDefined()) {
            scaleTargetSizeBuilder = targetSize.toBuilder()
                    .calculateUndefinedDimensions(scalePreviewSizeBuilder.build())
                    .roundWidthUpToEvenAndMaintainAspectRatio();
            scalePreviewSizeBuilder.scale(
                    scaleTargetSizeBuilder.build(),
                    mParams.getVideoImageFit(),
                    mParams.getVideoImageScale());
        } else {
            // No target size was specified, so just use the preview size.
            scaleTargetSizeBuilder = ImageSize.builder().size(scalePreviewSizeBuilder)
                    .roundWidthUpToEvenAndMaintainAspectRatio();
        }
        Preconditions.checkState(scaleTargetSizeBuilder.areBothDimensionsDefined());

        ImageSize newScaledTargetSize;
        ImageSize newScaledPreviewSize;
        if (mParams.getVideoImageFit() == ImageFit.FILL && !mParams.getShouldCropVideo()
                || mParams.getVideoImageFit() == ImageFit.FIT && !mParams.getShouldPadVideo()) {
            // If we can't modify the aspect ratio using cropping or padding, then the target view
            // will be the same as the preview for viewing
            newScaledTargetSize = scalePreviewSizeBuilder
                    .scaleToFit(parentSize, ImageScale.ANY)
                    .build();
            newScaledPreviewSize = newScaledTargetSize;
        } else {
            // Scale the target to fit within the parent view
            ImageSize preScaleTargetSize = scaleTargetSizeBuilder.build();
            newScaledTargetSize = scaleTargetSizeBuilder
                    .scaleToFit(parentSize, ImageScale.ANY).build();
            // Scale the preview size by the same amount that target size was scaled
            scalePreviewSizeBuilder.width(scalePreviewSizeBuilder.getWidthUnchecked()
                    * newScaledTargetSize.getWidthUnchecked()
                    / preScaleTargetSize.getWidthUnchecked());
            scalePreviewSizeBuilder.height(scalePreviewSizeBuilder.getHeightUnchecked()
                    * newScaledTargetSize.getHeightUnchecked()
                    / preScaleTargetSize.getHeightUnchecked());
            newScaledPreviewSize = scalePreviewSizeBuilder.build();
        }

        boolean hasChanged = !mScaledTargetSize.equals(newScaledTargetSize)
                || !mScaledPreviewSize.equals(newScaledPreviewSize);
        mScaledTargetSize = newScaledTargetSize;
        mScaledPreviewSize = newScaledPreviewSize;
        setMeasuredDimension(
                mScaledTargetSize.getWidthUnchecked(), mScaledTargetSize.getHeightUnchecked());
        mSurfaceView.getHolder().setFixedSize(
                mScaledPreviewSize.getWidthUnchecked(), mScaledPreviewSize.getHeightUnchecked());
        if (!hasChanged) {
            return;
        }

        Log.v(LOG_TAG, String.format(
                "Parent=%s Target=%s Preview=%s", parentSize, targetSize, mPreviewSize));
        Log.v(LOG_TAG, String.format(
                "ScaledTarget=%s ScaledPreview=%s", mScaledTargetSize, mScaledPreviewSize));
        if (mCameraController.canFocusOnRect()) {
            // Calculate matrix for transforming preview x:y coordinates into the camera
            // -1000:1000
            // coordinate space, taking into account camera rotation and flipping
            // Note: Use float so that division is float division
            float focusSize = CAMERA_FOCUS_MAX - CAMERA_FOCUS_MIN;
            mFocusMatrix = new Matrix();
            mFocusMatrix.postScale(focusSize / mScaledPreviewSize.getWidthUnchecked(),
                    focusSize / mScaledPreviewSize.getHeightUnchecked());
            mFocusMatrix.postTranslate(-0.5f * focusSize, -0.5f * focusSize);
            mFocusMatrix.postRotate(-mCameraController.getPreviewDisplayOrientationDegrees());
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mScaledPreviewSize.areBothDimensionsDefined()) {
            float marginX = Math.max(0,
                    0.5f * (canvas.getWidth() - mScaledPreviewSize.getWidthUnchecked()));
            float marginY = Math.max(0,
                    0.5f * (canvas.getHeight() - mScaledPreviewSize.getHeightUnchecked()));
            canvas.clipRect(
                    marginX,
                    marginY,
                    marginX + mScaledPreviewSize.getWidthUnchecked(),
                    marginY + mScaledPreviewSize.getHeightUnchecked());
        }

        if (mPreviewSize != null && mIsPreviewing) {
            super.dispatchDraw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN
                && mFocusMatrix != null
                && mCameraController.canFocusOnRect()
                && mFocusSize > 0) {
            final RectF uiFocusRect = calculateTapArea(event.getX(), event.getY());
            RectF cameraFocusRectF = new RectF();
            mFocusMatrix.mapRect(cameraFocusRectF, uiFocusRect);
            Rect cameraFocusRect = new Rect(
                    Math.round(cameraFocusRectF.left), Math.round(cameraFocusRectF.top),
                    Math.round(cameraFocusRectF.right), Math.round(cameraFocusRectF.bottom));
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
        left += 0.5f * (mScaledTargetSize.getWidthUnchecked() - size.getWidthUnchecked());
        top += 0.5f * (mScaledTargetSize.getHeightUnchecked() - size.getHeightUnchecked());

        // Clamp coordinates by scaled target size
        left = Math.max(0, Math.min(mScaledTargetSize.getWidthUnchecked() - mFocusSize, left));
        top = Math.max(0, Math.min(mScaledTargetSize.getHeightUnchecked() - mFocusSize, top));

        // Translate the scaled target size coordinates into the scaled preview size coordinates
        left += 0.5f
                * (mScaledPreviewSize.getWidthUnchecked() - mScaledTargetSize.getWidthUnchecked());
        top += 0.5f * (mScaledPreviewSize.getHeightUnchecked()
                - mScaledTargetSize.getHeightUnchecked());

        return new RectF(left, top, left + mFocusSize, top + mFocusSize);
    }

    public interface UIInteractionListener {
        void onResize(ImageSize size);
        void onFocusOnRect(RectF uiRect, Rect cameraRect);
    }
}