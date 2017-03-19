package com.amosyuen.videorecorder.activity;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.amosyuen.videorecorder.R;
import com.amosyuen.videorecorder.activity.params.ActivityThemeParamsI;
import com.amosyuen.videorecorder.activity.params.FFmpegPreviewActivityParams;
import com.amosyuen.videorecorder.ui.ViewUtil;

import java.security.InvalidParameterException;

import static com.amosyuen.videorecorder.activity.FFmpegRecorderActivity.LOG_TAG;

public class FFmpegPreviewActivity
        extends AbstractDynamicStyledActivity
        implements OnClickListener, OnCompletionListener, Callback, OnVideoSizeChangedListener,
                OnLayoutChangeListener {

    public static final String REQUEST_PARAMS_KEY = "params";

    public static final int RESULT_ERROR = RESULT_FIRST_USER;
    public static final String RESULT_ERROR_PATH_KEY = "error";

    protected static final int PROGRESS_UPDATE_INTERVAL_MILLIS = 50;

    // User params
    protected FFmpegPreviewActivityParams mParams;

    // UI variables
    protected RelativeLayout mPreviewVideoParent;
    protected SurfaceView mSurfaceView;
    protected ImageView mPlayButton;
    protected ProgressBar mProgressBar;

    // State variables
    protected MediaPlayer mMediaPlayer;
    protected int mVideoWidth;
    protected int mVideoHeight;
    protected DecelerateInterpolator mInterpolator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!extractIntentParams()) {
            return;
        }
        layoutView();
        setupToolbar((Toolbar) findViewById(R.id.toolbar));

        mPreviewVideoParent = (RelativeLayout) findViewById(R.id.preview_video_parent);
        mPreviewVideoParent.addOnLayoutChangeListener(this);

        mSurfaceView = (SurfaceView) findViewById(R.id.preview_video);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.setOnClickListener(this);

        mPlayButton = (ImageView) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setProgressDrawable(ViewUtil.tintDrawable(
                mProgressBar.getProgressDrawable(), getThemeParams().getProgressColor()));

        mInterpolator = new DecelerateInterpolator();
    }

    @Override
    protected void layoutView() {
        setContentView(R.layout.activity_ffmpeg_preview);
    }

    @Override
    protected boolean extractIntentParams() {
        Intent intent = getIntent();
        mParams = (FFmpegPreviewActivityParams)
                        intent.getSerializableExtra(REQUEST_PARAMS_KEY);
        if (mParams == null) {
            onError(new InvalidParameterException(
                    "Intent did not have FFmpegPreviewActivity.REQUEST_PARAMS_KEY set."));
            return false;
        }
        return true;
    }

    @Override
    protected ActivityThemeParamsI getThemeParams() {
        return mParams.getThemeParams();
    }

    @Override
    protected void setupToolbar(android.support.v7.widget.Toolbar toolbar) {
        super.setupToolbar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(ViewUtil.tintDrawable(
                toolbar.getNavigationIcon(), getThemeParams().getToolbarWidgetColor()));
    }

    @Override
    @CallSuper
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (mParams.isConfirmation()) {
            getMenuInflater().inflate(R.menu.menu_done, menu);
            MenuItem menuItemFinish = menu.findItem(R.id.menu_finish);
            if (menuItemFinish != null) {
                Drawable menuItemFinishIcon = menuItemFinish.getIcon();
                if (menuItemFinishIcon != null) {
                    menuItemFinish.setIcon(ViewUtil.tintDrawable(
                            menuItemFinishIcon, getThemeParams().getToolbarWidgetColor()));
                }
            }
        }
        return true;
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_finish) {
            finishWithResult(RESULT_OK);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayButton.setVisibility(View.VISIBLE);
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_button) {
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                play();
            }
            mPlayButton.setVisibility(View.GONE);
        } else if (id == R.id.preview_video) {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                pause();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mParams.isConfirmation()) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.are_you_sure)
                    .setMessage(R.string.discard_video_msg)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishWithResult(RESULT_CANCELED);
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            finishWithResult(RESULT_CANCELED);
        }
    }

    protected void onError(Exception e) {
        Log.e(LOG_TAG, "Unexpected Error", e);
        Intent intent = new Intent();
        intent.putExtra(RESULT_ERROR_PATH_KEY, e);
        finishWithResult(RESULT_ERROR, intent);
    }

    protected void finishWithResult(int result) {
        finishWithResult(result, null);
    }

    protected void finishWithResult(int result, Intent intent) {
        stop();
        setResult(result, intent);
        finish();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(this, Uri.parse(mParams.getVideoFileUri()));
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(0);
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        animateProgress(mProgressBar.getMax())
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mMediaPlayer != null) {
                            mMediaPlayer.seekTo(0);
                            animateProgress(0);
                            mPlayButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        updateSurfaceSize();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        updateSurfaceSize();
    }

    // Scale the surface size to the largest size that fits both width and height within the parent.
    protected void updateSurfaceSize() {
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int width = mPreviewVideoParent.getMeasuredWidth();
            int height = mPreviewVideoParent.getMeasuredHeight();
            float videoAspectRatio = (float) mVideoWidth / mVideoHeight;
            float parentAspectRatio = (float) width / height;
            float ratioDiff = videoAspectRatio - parentAspectRatio;
            if (ratioDiff > 0) {
                height = (int)(width / videoAspectRatio);
            } else {
                width = (int)(height * videoAspectRatio);
            }
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
            if (layoutParams.width != width || layoutParams.height != height) {
                layoutParams.width = width;
                layoutParams.height = height;
                mSurfaceView.setLayoutParams(layoutParams);
            }
        }
    }

    protected void play() {
        mMediaPlayer.start();
        updateProgress();
    }

    protected void pause() {
        mMediaPlayer.pause();
        mPlayButton.setVisibility(View.VISIBLE);
    }

    protected void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    protected void updateProgress() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.getDuration() > 0) {
            animateProgress(mProgressBar.getMax()
                    * mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration());
        }
        if (mMediaPlayer.isPlaying()) {
            mProgressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateProgress();
                }
            }, PROGRESS_UPDATE_INTERVAL_MILLIS);
        }
    }

    protected ObjectAnimator animateProgress(int progress) {
        ObjectAnimator animation = ObjectAnimator.ofInt(mProgressBar, "progress", progress);
        animation.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        animation.setInterpolator(mInterpolator);
        animation.start();
        return animation;
    }
}
