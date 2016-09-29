package com.sourab.videorecorder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Sourab Sharma (sourab.sharma@live.in)  on 1/19/2016.
 */
public class ProgressSectionsView extends View
{
	private int mMinProgress = 1000;
	private int mMaxProgress = 8000;
	private int mCursorFlashIntervalMillis = 500;
	private int mCursorWidthPixels;
	private int mSeparatorWidthPixels;
	private Paint mCursorPaint;
	private Paint mMinProgressPaint;
	private Paint mSeparatorPaint;
	private Paint mProgressPaint;

	private int mCurrentProgress = 0;
	private int mTotalSectionProgress = 0;
	private ArrayList<Integer> mSections = new ArrayList<>();
	private boolean mIsCursorVisible = true;
	private long mCursorLastChangeMillis = 0;

	public ProgressSectionsView(Context context) {
		this(context, null, 0);
	}

	public ProgressSectionsView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProgressSectionsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
		mCursorWidthPixels = Math.round(4 * displayMetrics.density);
        mSeparatorWidthPixels = Math.round(2 * displayMetrics.density);

		Resources.Theme theme = getContext().getTheme();
		int colorAccent = Util.getThemeColorAttribute(theme, R.attr.colorAccent);
		int colorPrimary = Util.getThemeColorAttribute(theme, R.attr.colorPrimary);
		int colorPrimaryDark = Util.getThemeColorAttribute(theme, R.attr.colorPrimaryDark);

		mProgressPaint = new Paint();
		mProgressPaint.setStyle(Paint.Style.FILL);
		mProgressPaint.setColor(colorAccent);

		mCursorPaint = new Paint();
		mCursorPaint.setStyle(Paint.Style.FILL);
		mCursorPaint.setColor(colorPrimaryDark);

		mMinProgressPaint = new Paint();
		mMinProgressPaint.setStyle(Paint.Style.FILL);
		mMinProgressPaint.setColor(colorPrimary);

		mSeparatorPaint = new Paint();
		mSeparatorPaint.setStyle(Paint.Style.FILL);
		mSeparatorPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
	}

	public int getMinProgress() {
		return mMinProgress;
	}

	public void setMinProgress(int minProgress) {
		this.mMinProgress = minProgress;
	}

	public int getMaxProgress() { return mMaxProgress; }

	public void setMaxProgress(int time){
		mMaxProgress = time;
	}

	public int getCursorFlashInterval() {
		return mCursorFlashIntervalMillis;
	}

	public void setCursorFlashInterval(int cursorFlashIntervalMillis) {
		this.mCursorFlashIntervalMillis = cursorFlashIntervalMillis;
	}

	public int getCursorWidth() {
		return mCursorWidthPixels;
	}

	public void setCursorWidth(int cursorWidthPixels) {
		this.mCursorWidthPixels = cursorWidthPixels;
	}

	public int getSeparatorWidth() {
		return mSeparatorWidthPixels;
	}

	public void setSeparatorWidth(int separatorWidthPixels) {
		this.mSeparatorWidthPixels = separatorWidthPixels;
	}

	public Paint getCursorPaint() {
		return mCursorPaint;
	}

	public void setCursorPaint(Paint cursorPaint) {
		this.mCursorPaint = cursorPaint;
	}

	public void setCursorColor(@ColorInt int color) {
		mCursorPaint.setColor(color);
	}

	public Paint getMinProgressPaint() {
		return mMinProgressPaint;
	}

	public void setMinProgressPaint(Paint minProgressPaint) {
		this.mMinProgressPaint = minProgressPaint;
	}

	public void setMinProgressColor(@ColorInt int color) {
		mMinProgressPaint.setColor(color);
	}

	public Paint getSeparatorPaint() {
		return mSeparatorPaint;
	}

	public void setSeparatorPaint(Paint separatorPaint) {
		this.mSeparatorPaint = separatorPaint;
	}

	public void setSeparatorColor(@ColorInt int color) {
		mSeparatorPaint.setColor(color);
	}

	public Paint getProgressPaint() {
		return mProgressPaint;
	}

	public void setProgressPaint(Paint progressPaint) {
		this.mProgressPaint = progressPaint;
	}

	public void setProgressColor(@ColorInt int color) {
		mProgressPaint.setColor(color);
	}

	public int getCurrentProgress() {
		return mCurrentProgress;
	}

	public void setCurrentProgress(int currentProgress) {
		this.mCurrentProgress = currentProgress;
	}

	public int getTotalSectionProgress() {
		return mTotalSectionProgress;
	}

	public void startNewProgressSection() {
		if (mCurrentProgress > 0) {
			mSections.add(mCurrentProgress);
			mTotalSectionProgress += mCurrentProgress;
			mCurrentProgress = 0;
			mIsCursorVisible = true;
			mCursorLastChangeMillis = System.currentTimeMillis();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int height = getMeasuredHeight();
		int width = getMeasuredWidth();
		float pixelsPerProgress =
				(float) (width - mSections.size() * mSeparatorWidthPixels) / mMaxProgress;

		// Draw the current sections
		float drawnWidth = 0;
		for (int progress : mSections) {
			float sectionWidth = progress * pixelsPerProgress;
			canvas.drawRect(drawnWidth, 0, drawnWidth + sectionWidth, height, mProgressPaint);
            drawnWidth += sectionWidth;
			canvas.drawRect(drawnWidth, 0,
					drawnWidth + mSeparatorWidthPixels, height, mSeparatorPaint);
			drawnWidth += mSeparatorWidthPixels;
		}

		// Draw the minimum time indicator
		if (mTotalSectionProgress <= mMinProgress) {
			float minProgressX = pixelsPerProgress * mMinProgress;
			canvas.drawRect(minProgressX, 0,
					minProgressX + mSeparatorWidthPixels, height, mMinProgressPaint);
		}

		if (mCurrentProgress > 0) {
            float currentProgressWidth = pixelsPerProgress * mCurrentProgress;
			float endX = Math.min(drawnWidth + currentProgressWidth, width);
			canvas.drawRect(drawnWidth, 0, endX, height, mProgressPaint);
		} else {
			// Show and hide the cursor every mCursorFlashIntervalMillis
			long currMillis = System.currentTimeMillis();
			if (currMillis - mCursorLastChangeMillis >= mCursorFlashIntervalMillis) {
				mIsCursorVisible = !mIsCursorVisible;
				mCursorLastChangeMillis = currMillis;
			}
			if (mIsCursorVisible){
				canvas.drawRect(drawnWidth, 0,
						drawnWidth + mCursorWidthPixels, height, mCursorPaint);
			}
		}
		invalidate();
	}
}