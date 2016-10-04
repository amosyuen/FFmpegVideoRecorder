package com.amosyuen.videorecorder;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amosyuen.videorecorder.audio.AudioRecorderThread;
import com.amosyuen.videorecorder.audio.AudioTransformerTask;
import com.amosyuen.videorecorder.audio.ListAudioRecorder;
import com.amosyuen.videorecorder.util.ActivityThemeParams.Builder;
import com.amosyuen.videorecorder.util.ProgressSectionsView;
import com.amosyuen.videorecorder.util.RecorderActivityParams;
import com.amosyuen.videorecorder.util.RecorderListener;
import com.amosyuen.videorecorder.util.TaskListener;
import com.amosyuen.videorecorder.util.Util;
import com.amosyuen.videorecorder.video.ListVideoFrameRecorder;
import com.amosyuen.videorecorder.video.VideoFrameRecorderParams;
import com.amosyuen.videorecorder.video.VideoFrameRecorderView;
import com.amosyuen.videorecorder.video.VideoFrameTransformerTask;

import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

/**
 * Activity for recording audio and video
 */
public class FFmpegRecorderActivity extends AbstractDynamicStyledActivity
        implements OnClickListener, OnTouchListener, RecorderListener {

    public static final int RESULT_ERROR = RESULT_FIRST_USER;
    public static final String ERROR_PATH_KEY = "error";
    public static final String THUMBNAIL_URI_KEY = "thumbnail";

    public static final String RECORDER_ACTIVITY_PARAMS_KEY =
            BuildConfig.APPLICATION_ID + ".RecorderActivityParams";

    private static final int PREVIEW_ACTIVITY_RESULT = 10000;

    protected static final String LOG_TAG = "RecordActivity";

    // User parameters
    protected RecorderActivityParams mParams;

    // Ui variables
    protected ProgressSectionsView mProgressView;
    protected VideoFrameRecorderView mVideoFrameRecorderView;
    protected ImageButton mRecordButton;
    protected ImageButton mFlashButton;
    protected ImageButton mSwitchCameraButton;
    protected ProgressBar mProgressBar;
    protected TextView mProgressText;

    // Other vars
    protected MediaActionSound mMediaActionSound;

    // State variables
    protected ListVideoFrameRecorder mVideoFrameRecorder;
    protected ListAudioRecorder mAudioRecorder;
    protected AudioRecorderThread mAudioRecorderThread;
    protected File mVideoOutputFile;
    protected File mVideoThumbnailOutputFile;
    protected boolean mIsFlashOn;
    protected boolean mIsFrontCamera;
    protected boolean mIsNextEnabled;
    protected boolean mIsAudioRecorderReady;
    protected boolean mIsVideoFrameRecorderReady;
    protected boolean mIsFinishedRecording;
    protected long mLatestTimestampNanos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (!extractIntentParams()) {
            super.onCreate(savedInstanceState);
            return;
        }
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mProgressView = (ProgressSectionsView) findViewById(R.id.recorder_progress);
        mProgressView.setMinProgress(convertNanosToProgress(mParams.getMinRecordingTimeNanos()));
        mProgressView.setMaxProgress(convertNanosToProgress(mParams.getMaxRecordingTimeNanos()));
        mProgressView.setProgressColor(getProgressColor());
        mProgressView.setCursorColor(getProgressCursorColor());
        mProgressView.setMinProgressColor(getProgressMinProgressColor());

        mVideoFrameRecorderView = (VideoFrameRecorderView) findViewById(R.id.recorder_view);
        mVideoFrameRecorderView.setRecorderListener(this);

        mRecordButton = (ImageButton) findViewById(R.id.record_button);
        mRecordButton.setOnTouchListener(FFmpegRecorderActivity.this);

        @ColorInt int widgetcolor = getWidgetColor();
        mSwitchCameraButton = (ImageButton) findViewById(R.id.switch_camera_button);
        mSwitchCameraButton.setOnClickListener(FFmpegRecorderActivity.this);
        mSwitchCameraButton.setColorFilter(widgetcolor);

        mFlashButton = (ImageButton) findViewById(R.id.flash_button);
        mFlashButton.setOnClickListener(this);
        mFlashButton.setColorFilter(widgetcolor);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar) ;
        mProgressText = (TextView) findViewById(R.id.progress_text) ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mMediaActionSound = new MediaActionSound();
        }

        mVideoFrameRecorder = new ListVideoFrameRecorder();
        mAudioRecorder = new ListAudioRecorder();

        initRecorders();
    }

    protected boolean extractIntentParams() {
        mParams = (RecorderActivityParams)
                getIntent().getSerializableExtra(RECORDER_ACTIVITY_PARAMS_KEY);
        if (mParams == null) {
            onError(new InvalidParameterException(
                    "Intent did not have RECORDER_ACTIVITY_PARAMS_KEY params set."));
            return false;
        }
        if (mParams.getVideoOutputUri() == null || mParams.getVideoOutputUri().isEmpty()) {
            mParams = null;
            onError(new InvalidParameterException(
                    "Intent did not have a valid video output uri set."));
            return false;
        }
        return true;
    }

    @Override
    protected RecorderActivityParams getThemeParams() {
        return mParams;
    }

    @Override
    protected void layoutView() {
        setContentView(R.layout.activity_ffmpeg_recorder);
    }

    @ColorInt
    protected int getProgressCursorColor() {
        int color = getThemeParams().getProgressCursorColor();
        return color == 0
                ? Util.getThemeColorAttribute(getTheme(), R.attr.colorPrimaryDark) : color;
    }

    @ColorInt
    protected int getProgressMinProgressColor() {
        int color = getThemeParams().getProgressMinProgressColor();
        return color == 0
                ? Util.getThemeColorAttribute(getTheme(), R.attr.colorPrimary) : color;
    }

    @ColorInt
    protected int getWidgetColor() {
        int color = getThemeParams().getWidgetColor();
        return color == 0 ? ActivityCompat.getColor(this, android.R.color.white) : color;
    }

    @Override
    protected void setupToolbar(android.support.v7.widget.Toolbar toolbar) {
        Drawable navButtonDrawable =
                ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp);
        toolbar.setNavigationIcon(Util.tintDrawable(navButtonDrawable, getToolbarWidgetColor()));
        super.setupToolbar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_next, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_finish).setVisible(mIsNextEnabled);
        return super.onPrepareOptionsMenu(menu);
    }

    protected void initRecorders() {
        mIsNextEnabled = false;
        mIsFinishedRecording = false;
        mLatestTimestampNanos = 0;
        mProgressView.setVisibility(View.GONE);
        mProgressView.clearProgress();

        mVideoFrameRecorder.clear();
        mVideoFrameRecorderView.clearData();

        mAudioRecorder.clear();
        mAudioRecorderThread = new AudioRecorderThread(mParams, mAudioRecorder);
        mAudioRecorderThread.setRecorderListener(this);
        mAudioRecorderThread.start();

        openCamera();
    }

    protected void openCamera() {
        int cameraFacing =
                mIsFrontCamera ? CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK;
        VideoFrameRecorderParams params;
        if (mParams.getVideoCameraFacing() == cameraFacing) {
            params = mParams;
        } else {
            params = new VideoFrameRecorderParams.Builder()
                    .merge(mParams)
                    .videoCameraFacing(cameraFacing)
                    .build();
        }
        showProgress(R.string.initializing);
        mVideoFrameRecorderView.openCamera(params, mVideoFrameRecorder);
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_finish) {
            if (mVideoFrameRecorderView.getRecordingLengthNanos() > 0) {
                saveRecording();
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PREVIEW_ACTIVITY_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        previewAccepted();
                        break;
                    case RESULT_CANCELED:
                    default:
                        discardRecording();
                        initRecorders();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseResources();
    }

    @Override
    public void onRecorderInitializing(Object recorder) {
        if (recorder == mVideoFrameRecorderView) {
            mIsVideoFrameRecorderReady = false;
        } else if (recorder == mAudioRecorderThread) {
            mIsAudioRecorderReady = false;
        }
    }

    @Override
    public void onRecorderReady(Object recorder) {
        if (recorder == mVideoFrameRecorderView) {
            mIsVideoFrameRecorderReady = true;
        } else if (recorder == mAudioRecorderThread) {
            mIsAudioRecorderReady = true;
        }
        if (mIsVideoFrameRecorderReady && mIsAudioRecorderReady) {
            Log.d(LOG_TAG, "Both recorders ready!");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                }
            });
        }
    }

    @Override
    public void onRecorderError(Object recorder, final Exception error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onError(error);
            }
        });
    }

    @Override
    public void onRecorderStart(Object recorder) {}

    @Override
    public void onRecorderFrameRecorded(Object recorder, final long timestampNanos) {
        if (timestampNanos > mLatestTimestampNanos) {
            mLatestTimestampNanos = timestampNanos;
            mProgressView.setCurrentProgress(convertNanosToProgress(timestampNanos)
                    - mProgressView.getTotalSectionProgress());
            if (!mIsNextEnabled) {
                long minRecordingNanos = mParams.getMinRecordingTimeNanos();
                if (timestampNanos > minRecordingNanos) {
                    mIsNextEnabled = true;
                    supportInvalidateOptionsMenu();
                }
            }
            long maxRecordingNanos = mParams.getMaxRecordingTimeNanos();
            if (maxRecordingNanos > 0 && timestampNanos > maxRecordingNanos) {
                Log.d(LOG_TAG, "Max recording time reached");
                saveRecording();
            }
        }
    }

    protected static int convertNanosToProgress(long nanos) {
        return (int) TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    @Override
    public void onRecorderStop(Object recorder) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressView.startNewProgressSection();
            }
        });
    }

    protected void showProgress(@StringRes int progressTextRes) {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText(progressTextRes);
        hideControls();
    }

    protected void hideControls() {
        mRecordButton.setVisibility(View.GONE);
        mSwitchCameraButton.setVisibility(View.GONE);
        mFlashButton.setVisibility(View.GONE);
    }

    protected void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
        mProgressText.setVisibility(View.GONE);

        mProgressView.setVisibility(View.VISIBLE);
        mSwitchCameraButton.setVisibility(View.VISIBLE);
        mRecordButton.setVisibility(View.VISIBLE);
        updateCameraFlash();
    }

    @Override
    public void onBackPressed() {
        if (mIsFinishedRecording) {
            return;
        }
        if (mVideoFrameRecorderView.getRecordingLengthNanos() > 0) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.are_you_sure)
                    .setMessage(R.string.discard_video_msg)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            discardRecordingAndFinish();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            discardRecordingAndFinish();
            finish();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mIsFinishedRecording) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    break;
                case MotionEvent.ACTION_UP:
                    stopRecording();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();
                    if (x < 0 || x > v.getWidth() || y < 0 || y > v.getHeight()) {
                        stopRecording();
                    }
                    break;
            }
        }
        return false;
    }

    protected void startRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && !mVideoFrameRecorderView.isRecording()) {
            mMediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING);
            mMediaActionSound.load(MediaActionSound.STOP_VIDEO_RECORDING);
        }
        mVideoFrameRecorderView.startRecording();
        mAudioRecorderThread.setRecording(true);
    }

    protected void stopRecording() {
        if (mParams == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && mVideoFrameRecorderView.isRecording()) {
            mMediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING);
            mMediaActionSound.load(MediaActionSound.STOP_VIDEO_RECORDING);
        }
        mVideoFrameRecorderView.stopRecording();
        mAudioRecorderThread.setRecording(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.flash_button) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                return;
            }
            mIsFlashOn = !mIsFlashOn;
            updateCameraFlash();
        } else if (v.getId() == R.id.switch_camera_button) {
            mIsFrontCamera = !mIsFrontCamera;
            openCamera();
        }
    }

    protected void updateCameraFlash() {
        Camera camera = mVideoFrameRecorderView.getCamera();
        Camera.Parameters params = camera.getParameters();
        if (params.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            @DrawableRes int resId = mIsFlashOn
                    ? R.drawable.ic_flash_on_white_36dp : R.drawable.ic_flash_off_white_36dp;
            mFlashButton.setVisibility(View.VISIBLE);
            mFlashButton.setImageDrawable(ActivityCompat.getDrawable(this, resId));
            params.setFlashMode(mIsFlashOn
                    ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
        } else {
            mFlashButton.setVisibility(View.GONE);
        }
    }

    protected void onError(Exception e) {
        Log.e(LOG_TAG, "Unexpected Error", e);
        discardRecording();
        Intent intent = new Intent();
        intent.putExtra(ERROR_PATH_KEY, e);
        setResult(RESULT_ERROR, intent);
        finish();
    }

    public void discardRecordingAndFinish() {
        discardRecording();
        setResult(RESULT_CANCELED);
        finish();
    }

    public void discardRecording() {
        if (mParams == null) {
            return;
        }
        mProgressView.setVisibility(View.GONE);
        hideControls();
        releaseResources();
        if (mVideoOutputFile != null && mVideoOutputFile.exists()) {
            mVideoOutputFile.delete();
            mVideoOutputFile = null;
        }
        if (mVideoThumbnailOutputFile != null && mVideoThumbnailOutputFile.exists()) {
            mVideoThumbnailOutputFile.delete();
            mVideoThumbnailOutputFile = null;
        }
    }

    protected void saveRecording() {
        stopRecording();
        if (!mIsFinishedRecording) {
            Log.d(LOG_TAG, "Saving recording");
            mIsFinishedRecording = true;
            showProgress(R.string.processing);
            releaseResources();

            new SaveVideoTask().execute();
        }
    }

    protected void startPreview() {
        Log.d(LOG_TAG, "Saved recording. Starting preview");
        Intent previewIntent = new Intent(this, FFmpegPreviewActivity.class);
        previewIntent.setData(Uri.fromFile(mVideoOutputFile));
        previewIntent.putExtra(ACTIVITY_THEME_PARAMS_KEY,  new Builder().merge(mParams).build());
        startActivityForResult(previewIntent, PREVIEW_ACTIVITY_RESULT);
    }

    protected void previewAccepted() {
        Log.d(LOG_TAG, "Preview accepted");
        Intent resultIntent = new Intent();
        resultIntent.setData(Uri.fromFile(mVideoOutputFile));
        if (mVideoThumbnailOutputFile != null) {
            resultIntent.putExtra(THUMBNAIL_URI_KEY, Uri.fromFile(mVideoThumbnailOutputFile));
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    protected void releaseResources() {
        if (mParams == null) {
            return;
        }
        mVideoFrameRecorderView.closeCamera();
        mAudioRecorderThread.stopRunning();
    }

    private class SaveVideoTask extends AsyncTask<Object, Object, Exception>
            implements TaskListener {

        private static final double VIDEO_PROGRESS_PORTION = 0.75;
        private static final double AUDIO_PROGRESS_PORTION = 1.0 - VIDEO_PROGRESS_PORTION;

        private FFmpegFrameRecorder mRecorder;
        private VideoFrameTransformerTask mVideoTransformerTask;
        private AudioTransformerTask mAudioTransformerTask;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mVideoOutputFile = new File(Uri.parse(mParams.getVideoOutputUri()).getPath());

            String videoThumbnailOutputUri = mParams.getVideoThumbnailOutputUri();
            if (videoThumbnailOutputUri == null) {
                mVideoThumbnailOutputFile = null;
            } else {
                mVideoThumbnailOutputFile = new File(Uri.parse(videoThumbnailOutputUri).getPath());
            }

            mRecorder = Util.createFrameRecorder(mVideoOutputFile, mParams);

            Camera.Size previewSize = mVideoFrameRecorderView.getPreviewSize();
            mVideoTransformerTask = new VideoFrameTransformerTask(
                    mRecorder, mVideoThumbnailOutputFile, mParams,
                    previewSize.width, previewSize.height, mVideoFrameRecorder.getRecordedFrames());
            mVideoTransformerTask.setProgressListener(this);

            try {
                mAudioRecorderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAudioTransformerTask =
                    new AudioTransformerTask(mRecorder,  mAudioRecorder.getRecordedSamples());
            mAudioTransformerTask.setProgressListener(this);
        }

        @Override
        protected Exception doInBackground(Object[] params) {
            if (mVideoOutputFile == null) {
                return null;
            }
            try {
                mRecorder.start();
                mVideoTransformerTask.run();
                mAudioTransformerTask.run();
                mRecorder.stop();
                mRecorder.release();
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        public void onStart(Object task) {
            if (task == mVideoTransformerTask) {
                publishProgress(0.0);
            }
        }

        @Override
        public void onProgress(Object task, int progress, int total) {
            if (task == mVideoTransformerTask) {
                publishProgress(VIDEO_PROGRESS_PORTION * progress / total);
            } else if (task == mAudioTransformerTask) {
                publishProgress(VIDEO_PROGRESS_PORTION + AUDIO_PROGRESS_PORTION * progress / total);
            }
        }

        @Override
        public void onDone(Object task) {
            if (task == mAudioTransformerTask) {
                publishProgress(100.0);
            }
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);

            // TODO: update progress bar
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            mRecorder = null;
            mVideoTransformerTask = null;
            mAudioTransformerTask = null;

            if (e == null) {
                startPreview();
            } else {
                onError(e);
            }
        }
    }
}