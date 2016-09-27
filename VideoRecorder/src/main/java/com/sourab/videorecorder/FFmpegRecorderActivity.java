package com.sourab.videorecorder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.provider.MediaStore.Video;
import android.support.annotation.CallSuper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sourab.videorecorder.interfaces.Interfaces;
import com.sourab.videorecorder.util.CustomUtil;

import org.bytedeco.javacv.FrameRecorder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.List;

import static android.media.AudioFormat.ENCODING_PCM_16BIT;

/**
 * Created by Sourab Sharma (sourab.sharma@live.in)  on 1/19/2016.
 */
public class FFmpegRecorderActivity extends AbstractDynamicStyledActivity
        implements OnClickListener, OnTouchListener, Interfaces.AddBitmapOverlayListener {

    public static final String VIDEO_PATH_KEY = "video";
    public static final String THUMBNAIL_PATH_KEY = "thumbnail";

    public static final String EXTRA_PROGRESS_COLOR = EXTRA_PREFIX + ".ProgressColor";
    public static final String EXTRA_PROGRESS_CURSOR_COLOR = EXTRA_PREFIX + ".ProgressCursorColor";
    public static final String EXTRA_PROGRESS_MIN_PROGRESS_COLOR =
            EXTRA_PREFIX + ".ProgressMinProgressColor";
    public static final String EXTRA_WIDGET_COLOR = EXTRA_PREFIX + ".WidgetColor";

    private final static int MSG_START_RECORDING = 1;
    private final static int MSG_STOP_RECORDING = 2;
    private final static int MSG_SAVE_RECORDING = 3;

    private int mProgressColor;
    private int mProgressCursorColor;
    private int mProgressMinProgressColor;
    private int mWidgetColor;

    private final static String CLASS_LABEL = "RecordActivity";
    private PowerManager.WakeLock mWakeLock;
    private String strVideoPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "rec_video.mp4";
    private File fileVideoPath = null;
    private Uri uriVideoPath = null;
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    private Camera cameraDevice;
    private CameraView cameraView;
    private Parameters cameraParameters = null;
    private volatile FFmpegFrameRecorder videoRecorder;
    private RecorderThread recorderThread;

    private ImageButton recordButton;
    private ImageButton flashButton, switchCameraButton;
    private SavedFrames lastSavedframe = new SavedFrames(null, 0L, false, false);

    private boolean initSuccess = false;
    private boolean recording = false;
    private boolean isRecordingStarted = false;
    private boolean isFlashOn = false;
    private boolean isRotateVideo = false;
    private boolean isFrontCam = false;
    private boolean isPreviewOn = false;
    private boolean nextEnabled = false;
    private boolean recordFinish = false;
    private volatile boolean runAudioThread = true;
    private boolean isRecordingSaved = false;
    private boolean isFinalizing = false;

    private int previewWidth = 480;
    private int previewHeight = 480;
    private RecorderParameters recorderParameters = new RecorderParameters();
    private int defaultCameraId = -1;
    private int defaultScreenResolution = -1;
    private int cameraSelection = 0;

    private int totalRecordingTime = 6000;
    private int minRecordingTime = 1000;

    private long firstTime = 0;
    private long startPauseTime = 0;
    private long pausedTime = 0;
    private long stopPauseTime = 0;

    private volatile long mAudioTimestamp = 0L;
    private long mLastAudioTimestamp = 0L;
    private long frameTime = 0L;
    private long mVideoTimestamp = 0L;
    private volatile long mAudioTimeRecorded;

    private RelativeLayout surfaceParent;
    private ProgressSectionsView progressView;
    private String imagePath = null;

    private ProgressBar mProgressBar;
    private TextView mProgressText;

    private byte[] firstData = null;
    private byte[] bufferByte;

    private DeviceOrientationEventListener orientationListener;
    // The degrees of the device rotated clockwise from its natural orientation.
    private int deviceOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private Handler mHandler;

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void dispatchMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_RECORDING:
                        if (!isRecordingStarted) {
                            firstTime = System.currentTimeMillis();
                            isRecordingStarted = true;
                            pausedTime = 0;
                        } else {
                            stopPauseTime = System.currentTimeMillis();
                            long pauseInterval = stopPauseTime - startPauseTime - ((long) (1.0 /
                                    (double) recorderParameters.getVideoFrameRate()) * 1000);
                            pausedTime += pauseInterval;
                        }
                        recording = true;
                        break;
                    case MSG_STOP_RECORDING:
                        recording = false;
                        startPauseTime = System.currentTimeMillis();
                        progressView.startNewProgressSection();
                        break;
                    case MSG_SAVE_RECORDING:
                        recording = false;
                        saveRecording();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
        mWakeLock.acquire();

        orientationListener = new DeviceOrientationEventListener(FFmpegRecorderActivity.this);

        initHandler();

        initLayout();
    }

    @Override
    protected void layoutView() {
        setContentView(R.layout.activity_recorder);
    }

    @CallSuper
    protected void extractIntentParams(Intent intent) {
        super.extractIntentParams(intent);
        mProgressColor = intent.getIntExtra(EXTRA_PROGRESS_COLOR,
                CustomUtil.getThemeColorAttribute(getTheme(), R.attr.colorAccent));
        mProgressCursorColor = intent.getIntExtra(EXTRA_PROGRESS_CURSOR_COLOR,
                CustomUtil.getThemeColorAttribute(getTheme(), R.attr.colorPrimaryDark));
        mProgressMinProgressColor = intent.getIntExtra(EXTRA_PROGRESS_MIN_PROGRESS_COLOR,
                CustomUtil.getThemeColorAttribute(getTheme(), R.attr.colorPrimary));
        mWidgetColor = intent.getIntExtra(EXTRA_WIDGET_COLOR, android.R.color.white);
    }

    @Override
    protected void setupToolbar(android.support.v7.widget.Toolbar toolbar) {
        Drawable stateButtonDrawable =
                ContextCompat.getDrawable(this, R.drawable.ic_close_white_24dp);
        stateButtonDrawable = stateButtonDrawable.mutate();
        stateButtonDrawable.setColorFilter(mToolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(stateButtonDrawable);
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
        menu.findItem(R.id.menu_finish).setVisible(nextEnabled);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    @CallSuper
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_finish) {
            if (isRecordingStarted) {
                mHandler.sendEmptyMessage(MSG_SAVE_RECORDING);
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            if (isRecordingStarted) {
                showCancelConfirmationDialog();
            } else {
                cancel();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!initSuccess)
            return false;
        return super.dispatchTouchEvent(ev);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (orientationListener != null)
            orientationListener.enable();

        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, CLASS_LABEL);
            mWakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isFinalizing)
            finish();
        if (orientationListener != null)
            orientationListener.disable();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRecordingStarted = false;
        runAudioThread = false;

        releaseResources();

        if (cameraView != null) {
            stopPreview();
        }
        if (cameraDevice != null) {
            cameraDevice.setPreviewCallback(null);
            cameraDevice.release();
            cameraDevice = null;
        }
        firstData = null;
        cameraView = null;
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void initLayout() {
        surfaceParent = (RelativeLayout) findViewById(R.id.recorder_surface_parent);

        progressView = (ProgressSectionsView) findViewById(R.id.recorder_progress);
        progressView.setMinProgress(minRecordingTime);
        progressView.setMaxProgress(totalRecordingTime);
        progressView.setProgressColor(mProgressColor);
        progressView.setCursorColor(mProgressCursorColor);
        progressView.setMinProgressColor(mProgressMinProgressColor);

        recordButton = (ImageButton) findViewById(R.id.record_button);
        recordButton.setOnTouchListener(FFmpegRecorderActivity.this);

        switchCameraButton = (ImageButton) findViewById(R.id.switch_camera_button);
        switchCameraButton.setOnClickListener(FFmpegRecorderActivity.this);
        switchCameraButton.setColorFilter(mWidgetColor);

        flashButton = (ImageButton) findViewById(R.id.flash_button);
        flashButton.setOnClickListener(this);
        flashButton.setColorFilter(mWidgetColor);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar) ;
        mProgressText = (TextView) findViewById(R.id.progress_text) ;

        initCameraLayout();
    }

    private void initCameraLayout() {
        surfaceParent.removeAllViews();
        showProgress();
        mProgressText.setText(R.string.initializing);

        stopPreview();
        if (cameraDevice != null) {
            cameraDevice.release();
            cameraDevice = null;
        }

        new SetupCamera().execute();
    }

    private void showProgress() {
        hideControls();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressText.setVisibility(View.VISIBLE);
    }

    private void hideControls() {
        recordButton.setVisibility(View.GONE);
        switchCameraButton.setVisibility(View.GONE);
        flashButton.setVisibility(View.GONE);
    }

    private void initVideoRecorder() {
        strVideoPath = Util.createFinalPath(this);

        frameTime = (1000000L / recorderParameters.getVideoFrameRate());

        fileVideoPath = new File(strVideoPath);
        videoRecorder = new FFmpegFrameRecorder(strVideoPath,
                CONSTANTS.OUTPUT_WIDTH, CONSTANTS.OUTPUT_HEIGHT, recorderParameters
                .getAudioChannel());
        videoRecorder.setFormat(recorderParameters.getVideoOutputFormat());
        videoRecorder.setSampleRate(recorderParameters.getAudioSamplingRate());
        videoRecorder.setFrameRate(recorderParameters.getVideoFrameRate());
        videoRecorder.setVideoCodec(recorderParameters.getVideoCodec());
        videoRecorder.setVideoQuality(recorderParameters.getVideoQuality());
        videoRecorder.setAudioQuality(recorderParameters.getVideoQuality());
        videoRecorder.setAudioCodec(recorderParameters.getAudioCodec());
        videoRecorder.setVideoBitrate(recorderParameters.getVideoBitrate());
        videoRecorder.setAudioBitrate(recorderParameters.getAudioBitrate());
        //videoRecorder.setVideoOption(recorderParameters.getAudioBitrate());
        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);

        try {
            videoRecorder.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
            finish();
        }
        audioThread.start();
    }

    private class SetupCamera extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                int numberOfCameras = Camera.getNumberOfCameras();
                CameraInfo cameraInfo = new CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == cameraSelection) {
                        defaultCameraId = i;
                    }
                }

                if (defaultCameraId >= 0) {
                    cameraDevice = Camera.open(defaultCameraId);
                } else {
                    cameraDevice = Camera.open();
                }

            } catch (Exception e) {
                return false;
            }

            if (!initSuccess) {
                initVideoRecorder();
                initSuccess = true;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result || cameraDevice == null) {
                finish();
                return;
            }

            cameraParameters = cameraDevice.getParameters();
            handleSurfaceChanged();
            if (recorderThread == null) {
                recorderThread = new RecorderThread(videoRecorder, previewWidth, previewHeight);
                recorderThread.start();
            }

            mProgressBar.setVisibility(View.GONE);
            mProgressText.setVisibility(View.GONE);

            int screenWidth = surfaceParent.getMeasuredWidth();
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    screenWidth,
                    screenWidth * previewWidth / previewHeight);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            cameraView = new CameraView(FFmpegRecorderActivity.this);
            surfaceParent.addView(cameraView, layoutParams);

            progressView.setVisibility(View.VISIBLE);
            recordButton.setVisibility(View.VISIBLE);
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                switchCameraButton.setVisibility(View.VISIBLE);
            }

            if (cameraSelection == CameraInfo.CAMERA_FACING_FRONT) {
                flashButton.setVisibility(View.GONE);
                isFrontCam = true;
            } else {
                flashButton.setVisibility(View.VISIBLE);
                isFrontCam = false;
                if (isFlashOn) {
                    cameraParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    cameraDevice.setParameters(cameraParameters);
                }
            }
        }
    }

    private class AsyncStopRecording extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            isFinalizing = true;
            recordFinish = true;
            runAudioThread = false;

            surfaceParent.removeAllViews();
            showProgress();
            progressView.setVisibility(View.GONE);
            mProgressText.setText(R.string.processing);

            super.onPreExecute();
        }

        private void getFirstCapture(byte[] data) {
            String captureBitmapPath = Util.createImagePath(FFmpegRecorderActivity.this);
            YuvImage localYuvImage = new YuvImage(data, 17, previewWidth, previewHeight, null);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            FileOutputStream outStream;

            try {
                File file = new File(captureBitmapPath);
                if (!file.exists()) {
                    file.createNewFile();
                }

                // Convert image to JPEG
                localYuvImage.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight), 100, bos);
                byte[] bytes = bos.toByteArray();
                Bitmap localBitmap1 = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bos.close();

                // Rotate image and write to file
                Matrix localMatrix = new Matrix();
                if (cameraSelection == 0) {
                    localMatrix.setRotate(90.0F);
                } else {
                    localMatrix.setRotate(270.0F);
                }
                Bitmap localBitmap2 = Bitmap.createBitmap(localBitmap1, 0, 0,
                        localBitmap1.getWidth(), localBitmap1.getHeight(), localMatrix, true);
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                localBitmap2.compress(Bitmap.CompressFormat.JPEG, 100, bos2);

                outStream = new FileOutputStream(captureBitmapPath);
                outStream.write(bos2.toByteArray());
                outStream.close();

                localBitmap1.recycle();
                localBitmap2.recycle();

                isFirstFrame = false;
                imagePath = captureBitmapPath;
            } catch (IOException e) {
                isFirstFrame = true;
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (firstData != null) {
                getFirstCapture(firstData);
            }

            recorderThread.stopRecord();

            isFinalizing = false;
            if (videoRecorder != null && isRecordingStarted) {
                releaseResources();
            }
            publishProgress(100);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            registerVideo();

            mProgressBar.setVisibility(View.GONE);
            mProgressText.setVisibility(View.GONE);

            startPreview();
            videoRecorder = null;
        }

    }

    private void showCancelConfirmationDialog() {
         new AlertDialog.Builder(this)
                .setTitle(R.string.are_you_sure)
                .setMessage(R.string.discard_video_msg)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isRecordingStarted) {
            showCancelConfirmationDialog();
        } else {
            cancel();
        }
    }

    private class AudioRecordRunnable implements Runnable {

        private AudioRecordRunnable() {}

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            int channelConfig = recorderParameters.getAudioChannel() == 2
                    ? AudioFormat.CHANNEL_IN_STEREO
                    : AudioFormat.CHANNEL_IN_MONO;
            int sampleRate = recorderParameters.getAudioSamplingRate();
            int bufferByteSize =
                    AudioRecord.getMinBufferSize(sampleRate, channelConfig, ENCODING_PCM_16BIT);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate, channelConfig, ENCODING_PCM_16BIT, bufferByteSize);
            short[] audioBuffer = new short[bufferByteSize / 2];

            while (audioRecord.getState() == 0) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException localInterruptedException) {
                }
            }
            audioRecord.startRecording();

            long samplesRecorded = 0;
            // We need to record audio past the video to prevent sharp cutoffs
            while ((runAudioThread || mVideoTimestamp > mAudioTimestamp)
                    && videoRecorder != null && mAudioTimestamp < 1000 * totalRecordingTime) {
                int readShorts = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                if (readShorts > 0 && videoRecorder != null
                        && (recording || mVideoTimestamp > mAudioTimestamp)) {
                    try {
                        videoRecorder.recordSamples(sampleRate, audioRecord.getChannelCount(),
                                ShortBuffer.wrap(audioBuffer, 0, readShorts));

                        // Calculate the timestamp from the samples recorded and sample rate
                        samplesRecorded += readShorts / audioRecord.getChannelCount();
                        mAudioTimestamp = 1000000L * samplesRecorded / sampleRate;
                        mAudioTimeRecorded = System.nanoTime();
                    } catch (FrameRecorder.Exception localException) {
                    }
                }
            }
            audioRecord.stop();
            audioRecord.release();
        }
    }


    private boolean isFirstFrame = true;

    private class CameraView extends SurfaceView implements SurfaceHolder.Callback,
            PreviewCallback {

        private SurfaceHolder mHolder;

        public CameraView(Context context) {
            super(context);
            mHolder = getHolder();
            mHolder.addCallback(CameraView.this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            cameraDevice.setPreviewCallbackWithBuffer(CameraView.this);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                stopPreview();
                cameraDevice.setPreviewDisplay(holder);
            } catch (IOException exception) {
                cameraDevice.release();
                cameraDevice = null;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (isPreviewOn) {
                cameraDevice.stopPreview();
            }
            handleSurfaceChanged();
            startPreview();
            cameraDevice.autoFocus(null);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                mHolder.addCallback(null);
                cameraDevice.setPreviewCallback(null);
            } catch (RuntimeException e) {
            }
        }

        public void startPreview() {
            if (!isPreviewOn && cameraDevice != null) {
                isPreviewOn = true;
                cameraDevice.startPreview();
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            long frameTimeStamp = 0L;
            if (mAudioTimestamp == 0L && firstTime > 0L) {
                frameTimeStamp = 1000L * (System.currentTimeMillis() - firstTime);
            } else if (mLastAudioTimestamp == mAudioTimestamp) {
                frameTimeStamp = mAudioTimestamp + frameTime;
            } else {
                frameTimeStamp = mAudioTimestamp + (System.nanoTime() - mAudioTimeRecorded) / 1000L;
                mLastAudioTimestamp = mAudioTimestamp;
            }

            if (recording) {
                if (lastSavedframe != null && lastSavedframe.getFrameBytesData() != null) {
                    if (isFirstFrame) {
                        isFirstFrame = false;
                        firstData = data;
                    }

                    // We skipped the first frame
                    long totalTime = System.currentTimeMillis() - firstTime - pausedTime
                            - ((long) (1.0 / (double) recorderParameters.getVideoFrameRate()) *
                            1000);
                    progressView.setCurrentProgress(
                            (int)(totalTime - progressView.getTotalSectionProgress()));

                    if (!nextEnabled && totalTime >= minRecordingTime) {
                        nextEnabled = true;
                        supportInvalidateOptionsMenu();
                    }
                    if (nextEnabled && totalTime >= totalRecordingTime) {
                        mHandler.sendEmptyMessage(MSG_SAVE_RECORDING);
                        return;
                    }

                    mVideoTimestamp += frameTime;
                    if (lastSavedframe.getTimeStamp() > mVideoTimestamp) {
                        mVideoTimestamp = lastSavedframe.getTimeStamp();
                    }
                    recorderThread.putByteData(lastSavedframe);
                }
            }
            lastSavedframe = new SavedFrames(data, frameTimeStamp, isRotateVideo, isFrontCam);
            if (cameraDevice != null) {
                cameraDevice.addCallbackBuffer(bufferByte);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!recordFinish) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isRotateVideo = deviceOrientation == 0;
                    mHandler.removeMessages(MSG_START_RECORDING);
                    mHandler.sendEmptyMessage(MSG_START_RECORDING);
                    break;
                case MotionEvent.ACTION_UP:
                    mHandler.removeMessages(MSG_STOP_RECORDING);
                    mHandler.sendEmptyMessage(MSG_STOP_RECORDING);
                    break;
            }
        }
        return false;
    }

    public void stopPreview() {
        if (isPreviewOn && cameraDevice != null) {
            isPreviewOn = false;
            cameraDevice.stopPreview();
        }
    }

    private void handleSurfaceChanged() {
        if (cameraDevice == null) {
            finish();
            return;
        }
        List<Camera.Size> resolutionList = Util.getResolutionList(cameraDevice);
        if (resolutionList != null && resolutionList.size() > 0) {
            Collections.sort(resolutionList, new Util.ResolutionComparator());
            Camera.Size previewSize = null;
            if (defaultScreenResolution == -1) {
                boolean hasSize = false;
                for (int i = 0; i < resolutionList.size(); i++) {
                    Size size = resolutionList.get(i);
                    if (size != null && size.width == 640 && size.height == 480) {
                        previewSize = size;
                        hasSize = true;
                        break;
                    }
                }
                if (!hasSize) {
                    int mediumResolution = resolutionList.size() / 2;
                    if (mediumResolution >= resolutionList.size())
                        mediumResolution = resolutionList.size() - 1;
                    previewSize = resolutionList.get(mediumResolution);
                }
            } else {
                if (defaultScreenResolution >= resolutionList.size())
                    defaultScreenResolution = resolutionList.size() - 1;
                previewSize = resolutionList.get(defaultScreenResolution);
            }
            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
                cameraParameters.setPreviewSize(previewWidth, previewHeight);
                if (videoRecorder != null) {
                    videoRecorder.setImageWidth(previewWidth);
                    videoRecorder.setImageHeight(previewHeight);
                }
            }
        }

        bufferByte = new byte[previewWidth * previewHeight * 3 / 2];

        cameraDevice.addCallbackBuffer(bufferByte);

        cameraParameters.setPreviewFrameRate(recorderParameters.getVideoFrameRate());

        cameraDevice.setDisplayOrientation(Util.determineDisplayOrientation
                (FFmpegRecorderActivity.this, defaultCameraId));
        List<String> focusModes = cameraParameters.getSupportedFocusModes();
        if (focusModes != null) {
            Log.i("video", Build.MODEL);
            if (((Build.MODEL.startsWith("GT-I950"))
                    || (Build.MODEL.endsWith("SCH-I959"))
                    || (Build.MODEL.endsWith("MEIZU MX3"))) && focusModes.contains(Camera
                    .Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {

                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            }
        }
        cameraDevice.setParameters(cameraParameters);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.flash_button) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                return;
            }
            if (isFlashOn) {
                isFlashOn = false;
                flashButton.setImageDrawable(
                        ActivityCompat.getDrawable(this, R.drawable.ic_flash_off_white_36dp));
                cameraParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            } else {
                isFlashOn = true;
                flashButton.setImageDrawable(
                        ActivityCompat.getDrawable(this, R.drawable.ic_flash_on_white_36dp));
                cameraParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
            }
            cameraDevice.setParameters(cameraParameters);
        } else if (v.getId() == R.id.switch_camera_button) {
            if (cameraSelection == CameraInfo.CAMERA_FACING_BACK) {
                cameraSelection = CameraInfo.CAMERA_FACING_FRONT;
                isFrontCam = true;
            } else {
                cameraSelection = CameraInfo.CAMERA_FACING_BACK;
                isFrontCam = false;
            }

            initCameraLayout();
        }
    }

    public void cancel() {
        hideControls();
        surfaceParent.removeAllViews();
        releaseResources();
        if (fileVideoPath != null && fileVideoPath.exists()) {
            fileVideoPath.delete();
            fileVideoPath = null;
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    private void startPreview() {
        Intent resultIntent = new Intent();
        resultIntent.setData(uriVideoPath);
        resultIntent.putExtra(THUMBNAIL_PATH_KEY, imagePath);
        setResult(RESULT_OK, resultIntent);

        Intent previewIntent = new Intent(this, FFmpegPreviewActivity.class);
        previewIntent.putExtra(VIDEO_PATH_KEY, strVideoPath);
        setNextIntentParams(previewIntent);
        startActivity(previewIntent);
        finish();
    }

    private void registerVideo() {
        Uri videoTable = Uri.parse(CONSTANTS.VIDEO_CONTENT_URI);
        Util.videoContentValues.put(Video.Media.SIZE, new File(strVideoPath).length());
        try {
            uriVideoPath = getContentResolver().insert(videoTable, Util.videoContentValues);
        } catch (Throwable e) {
            uriVideoPath = null;
            strVideoPath = null;
            e.printStackTrace();
        } finally {
        }
        Util.videoContentValues = null;
    }

    private void saveRecording() {
        if (!isRecordingSaved) {
            isRecordingSaved = true;
            new AsyncStopRecording().execute();
        }
    }

    private void releaseResources() {
        isRecordingStarted = false;
        if (recorderThread != null) {
            recorderThread.finish();
        }
        isRecordingSaved = true;
        try {
            if (videoRecorder != null) {
                videoRecorder.stop();
                videoRecorder.release();
            }
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        }
        videoRecorder = null;
        lastSavedframe = null;
    }

    private class DeviceOrientationEventListener
            extends OrientationEventListener {
        public DeviceOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // We keep the last known orientation. So if the user first orient
            // the camera then point the camera to floor or sky, we still have
            // the correct orientation.
            if (orientation == ORIENTATION_UNKNOWN) return;
            deviceOrientation = Util.roundOrientation(orientation, deviceOrientation);
            if (deviceOrientation == 0) {
                isRotateVideo = true;
            } else {
                isRotateVideo = false;
            }
        }
    }

    @Override
    public void OnBitmapOverlayAdded(int position) {

    }
}