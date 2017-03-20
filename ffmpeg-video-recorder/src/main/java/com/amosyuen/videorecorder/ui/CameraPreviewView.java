package com.amosyuen.videorecorder.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;

import com.amosyuen.videorecorder.R;
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

    // User specified params
    protected VideoTransformerParamsI mParams;
    protected ImageSize mPreviewSize = ImageSize.UNDEFINED;

    // Params
    protected UnrestrictedSurfaceView mSurfaceView;
    protected ImageSize mScaledPreviewSize = ImageSize.UNDEFINED;
    protected ImageSize mScaledTargetSize = ImageSize.UNDEFINED;

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
        mSurfaceView = (UnrestrictedSurfaceView) findViewById(R.id.surface_view);
        // Needed in order to clip the bounds
        mSurfaceView.setWillNotDraw(false);
    }

    @Nullable
    public VideoTransformerParamsI getParams() {
        return mParams;
    }

    public void setParams(@Nullable VideoTransformerParamsI params) {
        mParams = params;
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

    public boolean isPreviewing() {
        return mPreviewSize != null;
    }

    @Nullable
    public ImageSize getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSize(@Nullable ImageSize previewSize) {
        Preconditions.checkArgument(previewSize == null || previewSize.areBothDimensionsDefined());
        if (mPreviewSize == previewSize
                || mPreviewSize != null &&  mPreviewSize.equals(previewSize)) {
            return;
        }
        mPreviewSize = previewSize;
        if (mPreviewSize == null) {
            postInvalidate();
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (calculateSize()) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    mScaledTargetSize.getWidthUnchecked(), MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    mScaledTargetSize.getHeightUnchecked(), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected boolean calculateSize() {
        ImageSize parentSize;
        if (getParent() instanceof View) {
            View parent = (View) getParent();
            parentSize = new ImageSize(parent.getWidth(), parent.getHeight());
        } else {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            parentSize = new ImageSize(metrics.widthPixels, metrics.heightPixels);
        }
        if (!parentSize.areBothDimensionsDefined() || mParams == null) {
            return false;
        }

        ImageSize targetSize = mParams.getVideoSize();

        // Get the preview size and rotate it into the correct orientation for the view.
        ImageSize.Builder scalePreviewSizeBuilder;
        if (mPreviewSize == null) {
            // If there is no preview size calculate it by scaling the targetSize to fit the parent
            // or use the parent if the target size doesn't have both dimensions defined.
            scalePreviewSizeBuilder = targetSize.areBothDimensionsDefined()
                    ? targetSize.toBuilder().scaleToFit(parentSize, ImageScale.ANY)
                    : parentSize.toBuilder();
        } else {
            scalePreviewSizeBuilder = mPreviewSize.toBuilder();
            if (!ViewUtil.isContextConfigurationLandscape(getContext())) {
                scalePreviewSizeBuilder.invert();
            }
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
        if (hasChanged) {
            mSurfaceView.setUnrestrictedFixedSize(mScaledPreviewSize);
            Log.v(LOG_TAG, String.format(
                    "Parent=%s Target=%s Preview=%s", parentSize, targetSize, mPreviewSize));
            Log.v(LOG_TAG, String.format(
                    "ScaledTarget=%s ScaledPreview=%s", mScaledTargetSize, mScaledPreviewSize));
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isPreviewing()) {
            super.onDraw(canvas);
        }
    }
}