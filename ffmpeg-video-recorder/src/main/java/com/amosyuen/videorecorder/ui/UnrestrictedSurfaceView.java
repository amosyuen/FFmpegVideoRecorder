package com.amosyuen.videorecorder.ui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceView;

import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.common.base.Preconditions;

/**
 * Surface view that overrides measure spec constraints when setting fixed size.
 */
public class UnrestrictedSurfaceView extends SurfaceView {

    protected ImageSize mImageSize;

    public UnrestrictedSurfaceView(Context context) {
        super(context);
    }

    public UnrestrictedSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnrestrictedSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UnrestrictedSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean setUnrestrictedFixedSize(ImageSize imageSize) {
        if (mImageSize == imageSize || mImageSize != null && mImageSize.equals(imageSize)) {
            return false;
        }
        Preconditions.checkArgument(mImageSize == null || imageSize.areBothDimensionsDefined());
        mImageSize = imageSize;
        forceLayout();
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mImageSize != null) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    mImageSize.getWidthUnchecked(), MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    mImageSize.getHeightUnchecked(), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
