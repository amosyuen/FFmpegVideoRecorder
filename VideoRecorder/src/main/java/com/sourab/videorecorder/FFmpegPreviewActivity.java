package com.sourab.videorecorder;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class FFmpegPreviewActivity extends AbstractDynamicStyledActivity implements TextureView.SurfaceTextureListener
        , OnClickListener, OnCompletionListener {

    private String path;
    private TextureView surfaceView;
    private MediaPlayer mediaPlayer;
    private ImageView imagePlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        surfaceView = (TextureView) findViewById(R.id.preview_video);

        RelativeLayout preview_video_parent = (RelativeLayout) findViewById(R.id.preview_video_parent);
        LayoutParams layoutParams = (LayoutParams) preview_video_parent
                .getLayoutParams();
        layoutParams.width = displaymetrics.widthPixels;
        layoutParams.height = displaymetrics.widthPixels;
        preview_video_parent.setLayoutParams(layoutParams);

        surfaceView.setSurfaceTextureListener(this);
        surfaceView.setOnClickListener(this);

        path = getIntent().getStringExtra(FFmpegRecorderActivity.VIDEO_PATH_KEY);

        imagePlay = (ImageView) findViewById(R.id.previre_play);
        imagePlay.setOnClickListener(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
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
            stop();
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            imagePlay.setVisibility(View.GONE);
        }
        super.onStop();
    }

    private void prepare(Surface surface) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(path);
            mediaPlayer.setSurface(surface);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
        } catch (Exception e) {
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1,
                                          int arg2) {
        prepare(new Surface(arg0));
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1,
                                            int arg2) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture arg0) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.previre_play) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
            imagePlay.setVisibility(View.GONE);
        } else if (id == R.id.preview_video) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                imagePlay.setVisibility(View.VISIBLE);
            }
        }
    }

    private void stop() {
        mediaPlayer.stop();
        Intent intent = new Intent(this, FFmpegRecorderActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        stop();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        imagePlay.setVisibility(View.VISIBLE);
    }
}
