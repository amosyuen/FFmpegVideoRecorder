package com.amosyuen.videorecorder.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.amosyuen.videorecorder.BuildConfig;
import com.amosyuen.videorecorder.R;
import com.amosyuen.videorecorder.camera.CameraController;
import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.MediaClipsRecorder;
import com.amosyuen.videorecorder.recorder.VideoTransformerTask;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.CameraParams;
import com.amosyuen.videorecorder.ui.CameraPreviewView;
import com.amosyuen.videorecorder.ui.ProgressSectionsView;
import com.amosyuen.videorecorder.ui.TapToFocusView;
import com.amosyuen.videorecorder.util.Util;
import com.amosyuen.videorecorder.util.VideoUtil;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for recording audio and video
 */
public class FFmpegRecorderActivity extends AbstractDynamicStyledActivity implements
        OnClickListener,
        OnTouchListener,
        CameraControllerI.CameraListener,
        MediaClipsRecorder.MediaClipsRecorderListener,
        MediaClipsRecorder.MediaRecorderConfigurer,
        ProgressSectionsView.ProgressSectionsProvider,
        SurfaceHolder.Callback {

    public static final int RESULT_ERROR = RESULT_FIRST_USER;
    public static final String ERROR_PATH_KEY = "error";
    public static final String THUMBNAIL_URI_KEY = "thumbnail";

    public static final String RECORDER_ACTIVITY_PARAMS_KEY =
            BuildConfig.APPLICATION_ID + ".RecorderActivityParams";

    private static final int PREVIEW_ACTIVITY_RESULT = 10000;

    protected static final int FOCUS_WEIGHT = 1000;

    protected static final String LOG_TAG = "FFmpegRecorderActivity";

    // Ui variables
    protected ProgressSectionsView mProgressView;
    protected CameraPreviewView mCameraPreviewView;
    protected TapToFocusView mFocusRectView;
    protected AppCompatImageButton mRecordButton;
    protected AppCompatImageButton mFlashButton;
    protected AppCompatImageButton mSwitchCameraButton;
    protected CircularProgressView mProgressBar;
    protected TextView mProgressText;
    protected AppCompatImageButton mNextButton;

    // State variables
    protected ActivityOrientationEventListener mOrientationEventListener;
    protected int mOpenCameraOrientationDegrees;
    protected MediaClipsRecorder mMediaClipsRecorder;
    protected CameraControllerI mCameraController;
    protected int mOriginalRequestedOrientation;
    protected long mResumeAutoFocusTaskStartMillis;
    protected OpenCameraTask mOpenCameraTask;
    protected SaveVideoTask mSaveVideoTask;
    protected File mVideoOutputFile;
    protected File mVideoThumbnailOutputFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mThemeParams == null) {
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RecorderActivityParams params = getThemeParams();

        int progressColor = getProgressColor();
        mProgressView = (ProgressSectionsView) findViewById(R.id.recorder_progress);
        mProgressView.setMinProgress((int) params.getMinRecordingMillis());
        mProgressView.setMaxProgress((int) params.getMaxRecordingMillis());
        mProgressView.setProgressColor(progressColor);
        mProgressView.setCursorColor(getProgressCursorColor());
        mProgressView.setMinProgressColor(getProgressMinProgressColor());
        mProgressView.setProvider(this);

        // TODO: Support Camera 2
        mCameraController = new CameraController();
        mCameraController.addListener(this);
        mCameraPreviewView = (CameraPreviewView) findViewById(R.id.recorder_view);
        mCameraPreviewView.getHolder().addCallback(this);
        mCameraPreviewView.setParams(params);
        mCameraPreviewView.setCameraController(mCameraController);
        mCameraPreviewView.setFocusSize(getResources().getDimensionPixelSize(R.dimen.focus_size));
        mCameraPreviewView.setFocusWeight(FOCUS_WEIGHT);

        mFocusRectView = (TapToFocusView) findViewById(R.id.focus_rectangle_view);
        Paint focusPaint = new Paint();
        focusPaint.setColor(Color.WHITE);
        focusPaint.setStyle(Paint.Style.STROKE);
        focusPaint.setStrokeWidth(2);
        focusPaint.setShadowLayer(1f, 1f, 1f, Color.BLACK);
        mFocusRectView.setPaint(focusPaint);

        mRecordButton = (AppCompatImageButton) findViewById(R.id.record_button);
        mRecordButton.setOnTouchListener(FFmpegRecorderActivity.this);

        @ColorInt int widgetcolor = getWidgetColor();
        mSwitchCameraButton = (AppCompatImageButton) findViewById(R.id.switch_camera_button);
        mSwitchCameraButton.setOnClickListener(FFmpegRecorderActivity.this);
        mSwitchCameraButton.setColorFilter(widgetcolor);

        mFlashButton = (AppCompatImageButton) findViewById(R.id.flash_button);
        mFlashButton.setOnClickListener(this);
        mFlashButton.setColorFilter(widgetcolor);

        mProgressBar = (CircularProgressView) findViewById(R.id.progress_bar) ;
        mProgressBar.setColor(progressColor);
        mProgressText = (TextView) findViewById(R.id.progress_text) ;
    
        mNextButton = (AppCompatImageButton) findViewById(R.id.next_button);
        mNextButton.setColorFilter(getToolbarWidgetColor());
        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaClipsRecorder.getRecordedMillis() > 0) {
                    saveRecording();
                }
            }
        });

        mOrientationEventListener = new ActivityOrientationEventListener();

        mOriginalRequestedOrientation = getRequestedOrientation();
    }

    @Override
    protected boolean extractIntentParams() {
        RecorderActivityParams params = (RecorderActivityParams)
                getIntent().getSerializableExtra(RECORDER_ACTIVITY_PARAMS_KEY);
        if (params == null) {
            onError(new InvalidParameterException(
                    "Intent did not have RECORDER_ACTIVITY_PARAMS_KEY params set."));
            return false;
        }
        if (params.getVideoOutputUri() == null || params.getVideoOutputUri().isEmpty()) {
            onError(new InvalidParameterException(
                    "Intent did not have a valid video output uri set."));
            return false;
        }
        mThemeParams = params;
        return true;
    }

    @Override
    protected RecorderActivityParams getThemeParams() {
        return (RecorderActivityParams) mThemeParams;
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    protected void initRecorders() {
        mProgressView.setVisibility(View.GONE);
        mNextButton.setVisibility(View.INVISIBLE);

        mMediaClipsRecorder = new MediaClipsRecorder(this, getCacheDir());
        mMediaClipsRecorder.setMediaCLipstRecorderListener(this);

        setRequestedOrientation(mOriginalRequestedOrientation);

        openCamera(getThemeParams().getVideoCameraFacing());
    }

    @Override
    public void configureMediaRecorder(MediaRecorder recorder) {
        Log.v(LOG_TAG, String.format("Remaining millis %d", getThemeParams().getMaxRecordingMillis()
                - mMediaClipsRecorder.getRecordedMillis()));
        // Camera must be set first
        mCameraController.setMediaRecorder(recorder);
        // Then other config
        Util.setMediaRecorderParams(recorder, getThemeParams(),
                mMediaClipsRecorder.getRecordedMillis(), mMediaClipsRecorder.getRecordedBytes());

        // Set the same params as the camera
        ImageSize pictureSize = mCameraController.getPictureSize();
        recorder.setVideoSize(pictureSize.width, pictureSize.height);
        Log.v(LOG_TAG, String.format("Orientation: %d", mCameraController.getPreviewDisplayOrientationDegrees()));
        recorder.setOrientationHint(mCameraController.getPreviewDisplayOrientationDegrees());

        int[] framteRateRange = mCameraController.getFrameRateRange();
        int fps = framteRateRange[0] / 1000;
        try {
            recorder.setVideoFrameRate(fps);
        } catch (Exception e) {
            Log.e(LOG_TAG, String.format("Error setting recorder frame rate to %d", fps), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOrientationEventListener.enable();
        initRecorders();
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
                        supportInvalidateOptionsMenu();
                        break;
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mMediaClipsRecorder.getRecordedMillis() > 0) {
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
    protected void onPause() {
        super.onPause();
        stopRecording();
        mMediaClipsRecorder.deleteClips();
        mCameraController.closeCamera();
        releaseResources();
        mOrientationEventListener.disable();
    }

    protected void openCamera(CameraControllerI.Facing facing) {
        if (mOpenCameraTask != null) {
            return;
        }

        Log.d(LOG_TAG, String.format("Opening camera facing %s", facing));
        showProgress(R.string.initializing);
        mCameraPreviewView.setPreviewSize(null);
        mOpenCameraOrientationDegrees = mOrientationEventListener.mOrientationDegrees;
        mOpenCameraTask = new OpenCameraTask();
        mOpenCameraTask.execute(Preconditions.checkNotNull(facing));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setCameraPreviewDisplayIfReady();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public void onCameraOpen() {
        mCameraController.startPreview();
        mCameraPreviewView.setPreviewSize(mCameraController.getPreviewSize());
        setCameraPreviewDisplayIfReady();
    }

    protected void setCameraPreviewDisplayIfReady() {
        if (mCameraController.isCameraOpen()) {
            try {
                mCameraController.setPreviewDisplay(mCameraPreviewView.getHolder());
            } catch (IOException exception) {
                Log.e(LOG_TAG, "Error setting camera preview display", exception);
            }
        }
    }

    @Override
    public void onCameraClose() {
        mCameraPreviewView.setPreviewSize(null);
    }

    @Override
    public void onCameraStartPreview() {
        mCameraPreviewView.setIsPreviewing(true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgress();
            }
        });
        Log.d(LOG_TAG, "Ready to record");
    }

    @Override
    public void onCameraStopPreview() {
        mCameraPreviewView.setIsPreviewing(false);
    }

    @Override
    public void onFlashModeChanged(CameraControllerI.FlashMode flashMode) {}

    @Override
    public void onCameraFocusOnRect(Rect rect) {
        //mFocusRectView.setRect(rect);
        runTaskToAutoFocusAfterInactivity();
    }

    @Override
    public void onCameraAutoFocus() {
        mFocusRectView.clearRect();
    }

    protected void showProgress(@StringRes int progressTextRes) {
        if (!mProgressBar.isIndeterminate()) {
            mProgressBar.setIndeterminate(true);
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
        mProgressText.setText(progressTextRes);
        hideControls();
    }

    protected void hideUI() {
        mProgressView.setVisibility(View.GONE);
        hideControls();
    }

    protected void hideControls() {
        mRecordButton.setVisibility(View.INVISIBLE);
        mSwitchCameraButton.setVisibility(View.INVISIBLE);
        mFlashButton.setVisibility(View.INVISIBLE);
    }

    protected void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
        mProgressText.setVisibility(View.GONE);

        mProgressView.setVisibility(View.VISIBLE);
        mRecordButton.setVisibility(View.VISIBLE);
        if (mCameraController.getCameraCount() > 1) {
            mSwitchCameraButton.setVisibility(View.VISIBLE);
        }
        if (mCameraController.supportsFlashMode(CameraControllerI.FlashMode.ON)) {
            mFlashButton.setVisibility(View.VISIBLE);
        } else {
            mFlashButton.setVisibility(View.INVISIBLE);
        }
    }

    protected void startRecording() {
        Log.v(LOG_TAG, "Start recording");
        mCameraController.unlock();
        mMediaClipsRecorder.start();
        mSwitchCameraButton.setVisibility(View.INVISIBLE);
        // Lock the orientation the first time we start recording if there is no request orientation
        if (mMediaClipsRecorder.getClips().isEmpty() && mOriginalRequestedOrientation == -1) {
            setRequestedOrientation(getResources().getConfiguration().orientation);
        }
    }

    @Override
    public boolean hasCurrentProgress() {
        return mMediaClipsRecorder.isRecording();
    }

    @Override
    public List<Integer> getProgressSections() {
        List<MediaClipsRecorder.Clip> clips = mMediaClipsRecorder.getClips();
        ArrayList<Integer> progressList = Lists.newArrayListWithCapacity(
                clips.size() + (mMediaClipsRecorder.isRecording() ? 1 : 0));
        long totalRecordedMillis = 0;
        for (MediaClipsRecorder.Clip clip : clips) {
            totalRecordedMillis += clip.durationMillis;
            progressList.add((int) clip.durationMillis);
        }
        if (mMediaClipsRecorder.isRecording()) {
            progressList.add((int) mMediaClipsRecorder.getCurrentRecordedTimeMillis());
        }
        if (mNextButton.getVisibility() == View.INVISIBLE) {
            long minRecordingMillis = getThemeParams().getMinRecordingMillis();
            if (totalRecordedMillis > minRecordingMillis) {
                Log.v(LOG_TAG, "Reached min recoding time");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mNextButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
        return progressList;
    }

    @Override
    public void onMediaRecorderMaxDurationReached() {
        Log.d(LOG_TAG, "Max recording time reached");
        saveRecording();
    }

    @Override
    public void onMediaRecorderMaxFileSizeReached() {
        Log.d(LOG_TAG, "Max file size reached");
        saveRecording();
    }

    protected void stopRecordingAndPrepareForNext() {
        if (!mMediaClipsRecorder.isRecording()) {
            return;
        }
        stopRecording();
        runTaskToAutoFocusAfterInactivity();
    }

    protected void stopRecording() {
        if (mThemeParams == null || !mMediaClipsRecorder.isRecording()) {
            return;
        }
        Log.v(LOG_TAG, "Stop recording");
        mMediaClipsRecorder.stop();
        mCameraController.lock();
        if (mSaveVideoTask == null && mCameraController.getCameraCount() > 1) {
            mSwitchCameraButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mSaveVideoTask == null && mCameraController.isCameraOpen()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    break;
                case MotionEvent.ACTION_UP:
                    stopRecordingAndPrepareForNext();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getX();
                    float y = event.getY();
                    if (x < 0 || x > v.getWidth() || y < 0 || y > v.getHeight()) {
                        stopRecordingAndPrepareForNext();
                    }
                    break;
            }
        }
        return false;
    }

    protected void runTaskToAutoFocusAfterInactivity() {
        final long tapToFocusHoldTimeMillis = getThemeParams().getTapToFocusHoldTimeMillis();
        if (tapToFocusHoldTimeMillis <= 0) {
            return;
        }
        mResumeAutoFocusTaskStartMillis = System.currentTimeMillis();
        if (mCameraController.canAutoFocus()) {
            mFocusRectView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (System.currentTimeMillis()
                            >= mResumeAutoFocusTaskStartMillis + tapToFocusHoldTimeMillis
                            && mCameraController.canAutoFocus()
                            && !mMediaClipsRecorder.isRecording()) {
                        Log.v(LOG_TAG, "Reverting to auto focus");
                        mCameraController.autoFocus();
                    }
                }
            }, tapToFocusHoldTimeMillis);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.flash_button) {
            if (!mCameraController.isCameraOpen()) {
                return;
            }
            CameraControllerI.FlashMode flashMode;
            switch (mCameraController.getFlashMode()) {
                case ON:
                    flashMode = CameraControllerI.FlashMode.OFF;
                    break;
                case OFF:
                default:
                    flashMode = CameraControllerI.FlashMode.ON;
                    break;
            }
            updateCameraFlash(flashMode);
        } else if (v.getId() == R.id.switch_camera_button) {
            if (!mCameraController.isCameraOpen()) {
                return;
            }
            CameraControllerI.Facing cameraFacing =
                    mCameraController.getCameraFacing() == CameraControllerI.Facing.BACK
                            ? CameraControllerI.Facing.FRONT
                            : CameraControllerI.Facing.BACK;
            openCamera(cameraFacing);
        }
    }

    protected void updateCameraFlash(CameraControllerI.FlashMode flashMode) {
        CameraControllerI cameraManager = mCameraController;
        if (cameraManager.setFlashMode(flashMode)) {
            @DrawableRes int resId = flashMode == CameraControllerI.FlashMode.ON
                    ? R.drawable.ic_flash_on_white_36dp
                    : R.drawable.ic_flash_off_white_36dp;
            mFlashButton.setImageDrawable(ActivityCompat.getDrawable(this, resId));
        }
    }

    public void onMediaRecorderError(final Exception e) {
        Log.e(LOG_TAG, "Media recorder error");
        mCameraController.lock();
        onError(e);
    }

    public void onError(final Exception e) {
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
        Log.i(LOG_TAG, "Discard recording");
        if (mThemeParams == null) {
            return;
        }
        if (mSaveVideoTask != null) {
            mSaveVideoTask.cancel(true);
            mSaveVideoTask = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideUI();
            }
        });
        releaseResources();
        mMediaClipsRecorder.deleteClips();
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
        if (mSaveVideoTask == null) {
            Log.i(LOG_TAG, "Saving recording");
            showProgress(R.string.preparing);

            // Start the task slightly delayed so that the UI can update first
            mSaveVideoTask = new SaveVideoTask();
            mSaveVideoTask.execute();
        }
    }

    protected void startPreviewActivity() {
        Log.i(LOG_TAG, "Saved recording. Starting preview");
        Intent previewIntent = new Intent(this, FFmpegPreviewActivity.class);
        previewIntent.setData(Uri.fromFile(mVideoOutputFile));
        previewIntent.putExtra(ACTIVITY_THEME_PARAMS_KEY,
                new ActivityThemeParams.Builder().merge(mThemeParams).build());
        previewIntent.putExtra(FFmpegPreviewActivity.CAN_CANCEL_KEY, true);
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
        if (mThemeParams == null) {
            return;
        }
        mMediaClipsRecorder.release();
        mCameraController.closeCamera();
    }

    protected class ActivityOrientationEventListener extends OrientationEventListener {
        protected int mOrientationDegrees;

        public ActivityOrientationEventListener() {
            super(FFmpegRecorderActivity.this);
        }

        @Override
        public void onOrientationChanged(int orientationDegrees)
        {
            // If the view orientation when we opened the camera doesn't match the current
            // orientation, reopen the camera.
            mOrientationDegrees = VideoUtil.getContextRotation(FFmpegRecorderActivity.this);
            if (mCameraController.isCameraOpen()
                    && mOpenCameraOrientationDegrees != mOrientationDegrees) {
                openCamera(mCameraController.getCameraFacing());
            }
        }
    }

    protected class OpenCameraTask extends AsyncTask<CameraControllerI.Facing, Void, Exception> {

        @Override
        protected Exception doInBackground(CameraControllerI.Facing[] params) {
            try {
                CameraControllerI.Facing facing = Preconditions.checkNotNull(params[0]);
                CameraParams cameraParams;
                if (getThemeParams().getVideoCameraFacing() == facing) {
                    cameraParams = getThemeParams();
                } else {
                    cameraParams = new CameraParams.Builder()
                            .merge(getThemeParams())
                            .videoCameraFacing(facing)
                            .build();
                }
                mCameraController.openCamera(cameraParams, mOpenCameraOrientationDegrees);
            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            mOpenCameraTask = null;
            if (e != null) {
                Log.e(LOG_TAG, "Error opening camera", e);
                onError(e);
            }
        }
    }

    protected class SaveVideoTask extends AsyncTask<Object, Object, Exception>
            implements VideoTransformerTask.TaskListener {

        private VideoTransformerTask mVideoTransformerTask;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            releaseResources();

            Log.d(LOG_TAG, "VideoRecorder length " + mMediaClipsRecorder.getRecordedMillis());
        }

        @Override
        protected Exception doInBackground(Object[] params) {
            mVideoOutputFile = new File(Uri.parse(getThemeParams().getVideoOutputUri()).getPath());

            String videoThumbnailOutputUri = getThemeParams().getVideoThumbnailOutputUri();
            mVideoThumbnailOutputFile = videoThumbnailOutputUri == null
                    ? null
                    : new File(Uri.parse(videoThumbnailOutputUri).getPath());

            List<MediaClipsRecorder.Clip> clips = mMediaClipsRecorder.getClips();
            List<File> files = Lists.newArrayListWithCapacity(clips.size());
            for (MediaClipsRecorder.Clip clip : clips) {
                files.add(clip.file);
            }
            mVideoTransformerTask = new VideoTransformerTask(mVideoOutputFile, getThemeParams(), files);
            mVideoTransformerTask.setProgressListener(this);

            try {
                mVideoTransformerTask.run();
                mMediaClipsRecorder.deleteClips();
                publishProgress();
                // Create video thumbnail
                if (mVideoThumbnailOutputFile != null) {
                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(mVideoOutputFile.getAbsolutePath());
                    Bitmap frame = metadataRetriever.getFrameAtTime(
                            0, MediaMetadataRetriever.OPTION_NEXT_SYNC);
                    FileOutputStream outputStream = new FileOutputStream(mVideoThumbnailOutputFile);
                    frame.compress(CompressFormat.JPEG, 100, outputStream);
                }
                return null;
            } catch (Throwable e) {
                return (e instanceof Exception) ? (Exception) e : new RuntimeException(e);
            }
        }

        @Override
        public void onStart() {
            publishProgress(0f);
        }

        @Override
        public void onProgress(int progress, int total) {
            publishProgress(progress, total);
        }

        @Override
        public void onDone() {
            publishProgress(1f);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);

            if (values.length == 0) {
                if (!mProgressBar.isIndeterminate()) {
                    mProgressBar.setIndeterminate(true);
                    mProgressBar.startAnimation();
                    mProgressText.setText(R.string.saving_video);
                }
            } else {
                if (mProgressBar.isIndeterminate()) {
                    mProgressBar.setIndeterminate(false);
                }
                float progress = getFloatValue(values[0]);
                if (values.length > 1) {
                    progress /= getFloatValue(values[1]);
                }
                mProgressBar.setProgress(progress * mProgressBar.getMaxProgress());
                mProgressText.setText(
                        String.format(getString(R.string.encoding_percent), (int)(100 * progress)));
            }
        }

        protected float getFloatValue(Object o) {
            if (o instanceof Number) {
                return ((Number) o).floatValue();
            }
            throw new InvalidParameterException(String.format("Invalid progress value %s", o));
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            mVideoTransformerTask = null;

            if (e == null) {
                startPreviewActivity();
            } else {
                Log.e(LOG_TAG, "Error saving video", e);
                onError(e);
            }
        }
    }
}