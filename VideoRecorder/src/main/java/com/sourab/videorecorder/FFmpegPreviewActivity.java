package com.sourab.videorecorder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.sourab.videorecorder.util.CustomUtil;

public class FFmpegPreviewActivity extends AbstractDynamicStyledActivity implements OnClickListener,
        OnCompletionListener, SurfaceHolder.Callback {

    private static final int PROGRESS_UPDATE_INTERVAL_MILLIS = 50;

    private String path;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private ImageView playButton;
    private ProgressBar mProgressBar;

    private DecelerateInterpolator mInterpolator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        RelativeLayout preview_video_parent = (RelativeLayout) findViewById(R.id.preview_video_parent);
        LayoutParams layoutParams = (LayoutParams) preview_video_parent
                .getLayoutParams();
        layoutParams.width = displaymetrics.widthPixels;
        layoutParams.height = displaymetrics.widthPixels;
        preview_video_parent.setLayoutParams(layoutParams);

        surfaceView = (SurfaceView) findViewById(R.id.preview_video);
        surfaceView.getHolder().addCallback(this);
        surfaceView.setOnClickListener(this);

        path = getIntent().getStringExtra(FFmpegRecorderActivity.VIDEO_PATH_KEY);

        playButton = (ImageView) findViewById(R.id.play_button);
        playButton.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mInterpolator = new DecelerateInterpolator();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void layoutView() {
        setContentView(R.layout.activity_preview);
    }

    @Override
    protected void setupToolbar(android.support.v7.widget.Toolbar toolbar) {
        super.setupToolbar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Drawable navButtonDrawable = toolbar.getNavigationIcon().mutate();
        navButtonDrawable.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(navButtonDrawable);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_finish) {
            stop();
            Toast.makeText(this, getString(R.string.video_saved_path) + " " + path, Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playButton.setVisibility(View.VISIBLE);
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play_button) {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                play();
            }
            playButton.setVisibility(View.GONE);
        } else if (id == R.id.preview_video) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pause();
            }
        }
    }

    @Override
    public void onBackPressed() {
        stop();
        Intent intent = new Intent(this, FFmpegRecorderActivity.class);
        setNextIntentParams(intent);
        startActivity(intent);
        finish();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(path);
            mediaPlayer.setDisplay(holder);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
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
                        if (mediaPlayer != null) {
                            mediaPlayer.seekTo(0);
                            animateProgress(0);
                            playButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void play() {
        mediaPlayer.start();
        updateProgress();
    }

    private void pause() {
        mediaPlayer.pause();
        playButton.setVisibility(View.VISIBLE);
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void updateProgress() {
        if (mediaPlayer == null) {
            return;
        }
        animateProgress(mProgressBar.getMax()
                * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
        if (mediaPlayer.isPlaying()) {
            mProgressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateProgress();
                }
            }, PROGRESS_UPDATE_INTERVAL_MILLIS);
        }
    }

    private ObjectAnimator animateProgress(int progress) {
        ObjectAnimator animation = ObjectAnimator.ofInt(mProgressBar, "progress", progress);
        animation.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        animation.setInterpolator(mInterpolator);
        animation.start();
        return animation;
    }
}
