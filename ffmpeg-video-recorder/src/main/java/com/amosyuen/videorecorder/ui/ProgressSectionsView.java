package com.amosyuen.videorecorder.ui;

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

import com.amosyuen.videorecorder.R;

import java.util.List;

/**
 * View that shows different sections of progress and how they add up to the total progress.
 */
public class ProgressSectionsView extends View
{
	private long mMinProgress = 1000;
	private long mMaxProgress = 8000;
	private int mCursorFlashIntervalMillis = 500;
	private int mCursorWidthPixels;
	private int mSeparatorWidthPixels;
	private Paint mCursorPaint;
	private Paint mMinProgressPaint;
	private Paint mSeparatorPaint;
	private Paint mProgressPaint;

	private ProgressSectionsProvider mProvider;
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
		int colorAccent = ViewUtil.getThemeColorAttribute(theme, R.attr.colorAccent);
		int colorPrimary = ViewUtil.getThemeColorAttribute(theme, R.attr.colorPrimary);
		int colorPrimaryDark = ViewUtil.getThemeColorAttribute(theme, R.attr.colorPrimaryDark);

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
		mSeparatorPaint.setColor(ContextCompat.getColor(getContext(), R.color.dark_gray));
	}

	public ProgressSectionsProvider getProvider() {
		return mProvider;
	}

	public void setProvider(ProgressSectionsProvider provider) {
		mProvider = provider;
	}

	public long getMinProgress() {
		return mMinProgress;
	}

	public void setMinProgress(long minProgress) {
		this.mMinProgress = minProgress;
	}

	public long getMaxProgress() { return mMaxProgress; }

	public void setMaxProgress(long time){
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

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		boolean hasCurrentProgress = mProvider.hasCurrentProgress();
		List<Integer> sections = mProvider.getProgressSections();
		int totalProgress = 0;
		for (Integer progress : sections) {
			totalProgress += progress;
		}

		int height = getMeasuredHeight();
		int width = getMeasuredWidth();
		long maxProgress;
		if (mMaxProgress > 0) {
			maxProgress = mMaxProgress;
		} else {
			maxProgress = (totalProgress / 4000 + 1) * 4000 + 1000;
		}
		float pixelsPerProgress =
				(float) (width - sections.size() * mSeparatorWidthPixels) / maxProgress;

		// Draw the current sections
		float drawnWidth = 0;
		for (int progress : sections) {
			float sectionWidth = progress * pixelsPerProgress;
			canvas.drawRect(drawnWidth, 0, drawnWidth + sectionWidth, height, mProgressPaint);
            drawnWidth += sectionWidth;
			canvas.drawRect(drawnWidth, 0,
					drawnWidth + mSeparatorWidthPixels, height, mSeparatorPaint);
			drawnWidth += mSeparatorWidthPixels;
		}

		// Draw the minimum time indicator
		if (totalProgress <= mMinProgress) {
			float minProgressX = pixelsPerProgress * mMinProgress;
			canvas.drawRect(minProgressX, 0,
					minProgressX + mSeparatorWidthPixels, height, mMinProgressPaint);
		}

		if (!hasCurrentProgress) {
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

	public interface ProgressSectionsProvider {
		boolean hasCurrentProgress();
		List<Integer> getProgressSections();
	}
}