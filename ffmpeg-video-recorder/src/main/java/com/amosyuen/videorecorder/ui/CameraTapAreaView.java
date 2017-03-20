package com.amosyuen.videorecorder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * View to show taps on a {@link CameraPreviewView}. This view should be added as a child of
 * {@link CameraPreviewView}.
 */
public class CameraTapAreaView extends View {

    public static final int DEFAULT_FOCUS_SIZE_DP = 100;

    private static final String LOG_TAG = "CameraTapAreaView";

    protected List<TapAreaListener> mTapAreaListeners;
    protected float mTapSizePercent;
    protected Paint mPaint;
    protected RectF mTapAreaRect;

    public CameraTapAreaView(Context context) {
        super(context);
        init();
    }

    public CameraTapAreaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraTapAreaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraTapAreaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    protected void init() {
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTapAreaListeners = new ArrayList<>();
        mTapSizePercent = 0.1f;
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setShadowLayer(1f, 1f, 1f, Color.BLACK);
    }

    public CameraPreviewView getCameraPreviewView() {
        return Preconditions.checkNotNull((CameraPreviewView) getParent());
    }

    public boolean addTapAreaListener(TapAreaListener tapAreaListener) {
        return mTapAreaListeners.add(tapAreaListener);
    }

    public boolean removeTapAreaListener(TapAreaListener tapAreaListener) {
        return mTapAreaListeners.remove(tapAreaListener);
    }

    public float getTapSizePercent() {
        return mTapSizePercent;
    }

    /**
     * Set the focus size in pixels. A focus size of 0 disables tap to focus.
     */
    public void setTapSizePercent(float tapSizePercent) {
        Preconditions.checkArgument(tapSizePercent > 0 && tapSizePercent < 1f);
        mTapSizePercent = tapSizePercent;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    @Nullable
    public RectF getTapAreaRect() {
        return mTapAreaRect;
    }

    public void setTapAreaRect(@NonNull RectF tapAreaRect) {
        if (mTapAreaRect != null && mTapAreaRect.equals(tapAreaRect)) {
            return;
        }
        mTapAreaRect = tapAreaRect;
        Log.v(LOG_TAG, String.format("Tap area %s", mTapAreaRect));
        postInvalidate();
        for (TapAreaListener listener : mTapAreaListeners) {
            listener.onTapArea(mTapAreaRect);
        }
    }

    public void clearTapAreaRect() {
        if (mTapAreaRect == null) {
            return;
        }
        mTapAreaRect = null;
        Log.v(LOG_TAG, "Clear tap area");
        postInvalidate();
        for (TapAreaListener listener : mTapAreaListeners) {
            listener.onClearTapArea();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint != null && mTapAreaRect != null) {
            canvas.drawRect(mTapAreaRect, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setTapAreaRect(calculateTapArea(event.getX(), event.getY()));
        }
        return super.onTouchEvent(event);
    }

    /**
     * Convert touch position x:y to camera position -1000:-1000 to 1000:1000.
     */
    protected RectF calculateTapArea(float x, float y) {
        CameraPreviewView cameraPreviewView = getCameraPreviewView();
        ImageSize scaledTargetSize = cameraPreviewView.getScaledTargetSize();
        ImageSize scaledPreviewSize = cameraPreviewView.getScaledPreviewSize();
        // UI coordinates should be in scaled target coordinates because the size of
        // CameraPreviewView is set to the scaled target size.

        // Calculate the actual size in pixels
        float focusSize = 0.5f
                * (getMeasuredWidth() * mTapSizePercent + getMeasuredHeight() * mTapSizePercent);
        // Get the left and top bound
        float left = x - 0.5f * focusSize;
        float top = y - 0.5f * focusSize;

        // Calculate the margins of the scaled preview size in scaled target size coordinates
        float marginX = Math.max(0, 0.5f
                * (scaledTargetSize.getWidthUnchecked() - scaledPreviewSize.getWidthUnchecked()));
        float marginY = Math.max(0, 0.5f
                * (scaledTargetSize.getHeightUnchecked() - scaledPreviewSize.getHeightUnchecked()));

        // Clamp coordinates by margins
        left = Math.max(marginX,
                Math.min(scaledTargetSize.getWidthUnchecked() - marginX - focusSize, left));
        top = Math.max(marginY,
                Math.min(scaledTargetSize.getHeightUnchecked() - marginY - focusSize, top));

        return new RectF(left, top, left + focusSize, top + focusSize);
    }

    public interface TapAreaListener {
        void onTapArea(RectF tapArea);
        void onClearTapArea();
    }
}
