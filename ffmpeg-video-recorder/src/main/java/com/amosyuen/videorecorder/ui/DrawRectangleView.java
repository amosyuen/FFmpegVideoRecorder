package com.amosyuen.videorecorder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * View to draw an indicator for a rectangle.
 */
public class DrawRectangleView extends View {

    private Paint mPaint;
    private RectF mRect;

    public DrawRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
}
