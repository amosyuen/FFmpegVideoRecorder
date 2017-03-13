package com.amosyuen.videorecorder.demo;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.amosyuen.videorecorder.activity.FFmpegRecorderActivity;
import com.amosyuen.videorecorder.activity.RecorderActivityParams;
import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ScaleType;
import com.amosyuen.videorecorder.recorder.params.EncoderParams.AudioCodec;
import com.amosyuen.videorecorder.recorder.params.EncoderParams.VideoCodec;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;


/**
 * Fragment for starting a video recorder activity with specific parameters.
 */
public class VideoRecorderRequestFragment extends Fragment {

    static final String VIDEO_FILE_EXTENSION = ".mp4";
    static final String VIDEO_FILE_POSTFIX = ".video" + VIDEO_FILE_EXTENSION;
    static final String THUMBNAIL_FILE_EXTENSION = ".jpg";
    static final String THUMBNAIL_FILE_POSTFIX = ".thumbnail" + THUMBNAIL_FILE_EXTENSION;

    private static final String[] VIDEO_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private static final int REQUEST_VIDEO_PERMISSIONS_REQUEST = 1000;
    private static final int RECORD_VIDEO_REQUEST = 2000;

    // Ui components
    private EditText mVideoWidthEditText;
    private EditText mVideoHeightEditText;
    private Spinner mVideoCodecSpinner;
    private EditText mVideoBitrateEditText;
    private EditText mVideoFrameRateEditText;
    private Spinner mVideoCameraFacingSpinner;
    private Spinner mVideoScaleTypeSpinner;
    private SwitchCompat mVideoCanUpscaleSwitchCompat;
    private SwitchCompat mVideoCanPadSwitchCompat;

    private Spinner mAudioCodecSpinner;
    private EditText mAudioSampleRateEditText;
    private EditText mAudioBitrateEditText;
    private Spinner mAudioChannelCount;

    private EditText mMinRecordingEditText;
    private EditText mMaxRecordingEditText;
    private EditText mMaxFileSizeEditText;

    // State vars
    private OnVideoRecorderListener mListener;
    private File mVideoFile;
    private File mThumbnailFile;
    private View mErrorView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnVideoRecorderListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_recorder_request, container, false);

        mVideoWidthEditText = (EditText) view.findViewById(R.id.video_width);
        mVideoWidthEditText.setText("360");

        mVideoHeightEditText = (EditText) view.findViewById(R.id.video_height);
        mVideoHeightEditText.setText("360");

        ArrayAdapter<VideoCodec> videoCodecAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, VideoCodec.values());
        mVideoCodecSpinner = (Spinner) view.findViewById(R.id.video_codec);
        mVideoCodecSpinner.setAdapter(videoCodecAdapter);
        mVideoCodecSpinner.setSelection(videoCodecAdapter.getPosition(VideoCodec.H264));

        mVideoBitrateEditText = (EditText) view.findViewById(R.id.video_bitrate);
        mVideoBitrateEditText.setText("800000");

        mVideoFrameRateEditText = (EditText) view.findViewById(R.id.video_frame_rate);
        mVideoFrameRateEditText.setText("30");

        ArrayAdapter<CameraControllerI.Facing> facingAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, CameraControllerI.Facing.values());
        mVideoCameraFacingSpinner = (Spinner) view.findViewById(R.id.video_camera_facing);
        mVideoCameraFacingSpinner.setAdapter(facingAdapter);
        mVideoCameraFacingSpinner.setSelection(
                facingAdapter.getPosition(CameraControllerI.Facing.BACK));

        ArrayAdapter<ScaleType> scaleTypeAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, ScaleType.values());
        mVideoScaleTypeSpinner = (Spinner) view.findViewById(R.id.video_scale_type);
        mVideoScaleTypeSpinner.setAdapter(scaleTypeAdapter);
        mVideoScaleTypeSpinner.setSelection(scaleTypeAdapter.getPosition(ScaleType.FILL));

        mVideoCanUpscaleSwitchCompat = (SwitchCompat) view.findViewById(R.id.video_can_upscale);

        mVideoCanPadSwitchCompat = (SwitchCompat) view.findViewById(R.id.video_can_pad);
        mVideoCanPadSwitchCompat.setChecked(true);

        ArrayAdapter<AudioCodec> audioCodecAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, AudioCodec.values());
        mAudioCodecSpinner = (Spinner) view.findViewById(R.id.audio_codec);
        mAudioCodecSpinner.setAdapter(audioCodecAdapter);
        mAudioCodecSpinner.setSelection(audioCodecAdapter.getPosition(AudioCodec.AAC));

        mAudioSampleRateEditText = (EditText) view.findViewById(R.id.audio_sample_rate);
        mAudioSampleRateEditText.setText("44100");

        mAudioBitrateEditText = (EditText) view.findViewById(R.id.audio_bitrate);
        mAudioBitrateEditText.setText("64000");

        mAudioChannelCount = (Spinner) view.findViewById(R.id.audio_channel_count);
        mAudioChannelCount.setAdapter(new ArrayAdapter<Integer>(
                getContext(), android.R.layout.simple_spinner_item, new Integer[] { 1, 2 }));

        mMinRecordingEditText = (EditText) view.findViewById(R.id.min_recording_time);
        mMinRecordingEditText.setText("1");

        mMaxRecordingEditText = (EditText) view.findViewById(R.id.max_recording_time);
        mMaxRecordingEditText.setText("10");

        mMaxFileSizeEditText = (EditText) view.findViewById(R.id.max_file_size);
        mMaxFileSizeEditText.setText("0");

        Button recordButton = (Button) view.findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_VIDEO_PERMISSIONS_REQUEST:
                handleRequestPermissionsResult();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_VIDEO_PERMISSIONS_REQUEST:
                handleRequestPermissionsResult();
                break;
            case RECORD_VIDEO_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:
                        Uri videoUri = data.getData();
                        Uri thumbnailUri =
                                data.getParcelableExtra(FFmpegRecorderActivity.THUMBNAIL_URI_KEY);
                        mListener.onVideoRecorded(new VideoFile(
                                new File(videoUri.getPath()), new File(thumbnailUri.getPath())));
                        mVideoFile = null;
                        mThumbnailFile = null;
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    case FFmpegRecorderActivity.RESULT_ERROR:
                        Exception error = (Exception)
                                data.getSerializableExtra(FFmpegRecorderActivity.ERROR_PATH_KEY);
                        new AlertDialog.Builder(getContext())
                                .setCancelable(false)
                                .setTitle(R.string.error)
                                .setMessage(error.getLocalizedMessage())
                                .setPositiveButton(R.string.ok, null)
                                .show();
                        break;
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void checkPermissions() {
        if (hasAllPermissions()) {
            launchVideoRecorder();
            return;
        }

        requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS_REQUEST);
    }

    private void handleRequestPermissionsResult() {
        if (hasAllPermissions()) {
            launchVideoRecorder();
        } else if (shouldShowRequestPermissionRationale()) {
            requestPermissions(VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS_REQUEST);
        } else {
            new AlertDialog.Builder(getContext())
                    .setTitle("Need permissions")
                    .setMessage("You have set permissions to never show. You must enable" +
                            " them in the app details screen to record video.")
                    .setPositiveButton("App Details",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialogInterface, int i) {
                                    startAppDetailsIntent();
                                }
                            })
                    .show();
        }
    }

    public boolean hasAllPermissions() {
        for (String permission : VIDEO_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldShowRequestPermissionRationale() {
        for (String permission : VIDEO_PERMISSIONS) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

    private void startAppDetailsIntent() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, REQUEST_VIDEO_PERMISSIONS_REQUEST);
    }

    private int validatePositiveIntegerEditText(EditText editText) {
        return validateIntegerEditText(editText, false, false);
    }

    private int validateEmptyPositiveIntegerEditText(EditText editText) {
        return validateIntegerEditText(editText, true, false);
    }

    private int validateEmptyNonNegativeIntegerEditText(EditText editText) {
        return validateIntegerEditText(editText, true, true);
    }

    private int validateIntegerEditText(
            EditText editText, boolean allowEmpty, boolean allowZero) {
        editText.setError(null);
        if (editText.getText().length() > 0 || !allowEmpty) {
            try {
                int value = Integer.parseInt(editText.getText().toString());
                if (value < 0) {
                    editText.setError(getString(R.string.error_value_is_negative));
                    if (mErrorView == null) {
                        mErrorView = editText;
                    }
                } else if (value == 0 && !allowZero) {
                    editText.setError(getString(R.string.error_value_is_zero));
                    if (mErrorView == null) {
                        mErrorView = editText;
                    }
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                editText.setError(getString(R.string.error_value_is_not_a_number));
                if (mErrorView == null) {
                    mErrorView = editText;
                }
            }
        }
        return allowZero ? -1 : 0;
    }

    private void launchVideoRecorder() {
        mErrorView = null;
        int videoWidth = validateEmptyPositiveIntegerEditText(mVideoWidthEditText);
        int videoHeight = validateEmptyPositiveIntegerEditText(mVideoHeightEditText);
        int videoBitrate = validateEmptyPositiveIntegerEditText(mVideoBitrateEditText);
        int videoFrameRate = validatePositiveIntegerEditText(mVideoFrameRateEditText);

        int audioSampleRate = validatePositiveIntegerEditText(mAudioSampleRateEditText);
        int audioBitrate = validateEmptyPositiveIntegerEditText(mAudioBitrateEditText);

        int minRecordingMillis = validateEmptyNonNegativeIntegerEditText(mMinRecordingEditText);
        int maxRecordingMillis = validateEmptyNonNegativeIntegerEditText(mMaxRecordingEditText);
        int maxFileSizeBytes = validateEmptyNonNegativeIntegerEditText(mMaxFileSizeEditText);

        if (mErrorView != null) {
            mErrorView.requestFocus();
            return;
        }

        createTempFiles();

        RecorderActivityParams params = new RecorderActivityParams.Builder(mVideoFile)
                .videoThumbnailOutput(mThumbnailFile)

                .videoWidth(videoWidth)
                .videoHeight(videoHeight)
                .videoCodec((VideoCodec) mVideoCodecSpinner.getSelectedItem())
                .videoBitrate(videoBitrate)
                .videoFrameRate(videoFrameRate)

                .videoCameraFacing((CameraControllerI.Facing) mVideoCameraFacingSpinner.getSelectedItem())
                .videoScaleType((ScaleType) mVideoScaleTypeSpinner.getSelectedItem())
                .canUpscaleVideo(mVideoCanUpscaleSwitchCompat.isChecked())
                .canPadVideo(mVideoCanPadSwitchCompat.isChecked())

                .audioCodec((AudioCodec) mAudioCodecSpinner.getSelectedItem())
                .audioSamplingRateHz(audioSampleRate)
                .audioBitrate(audioBitrate)
                .audioChannelCount((Integer) mAudioChannelCount.getSelectedItem())

                .minRecordingMillis(TimeUnit.SECONDS.toMillis(minRecordingMillis))
                .maxRecordingMillis(TimeUnit.SECONDS.toMillis(maxRecordingMillis))
                .maxFileSizeBytes(maxFileSizeBytes)
                .build();

        // Start the intent for the activity so that the activity can handle the requestCode
        Intent intent = new Intent(getActivity(), FFmpegRecorderActivity.class);
        intent.putExtra(FFmpegRecorderActivity.RECORDER_ACTIVITY_PARAMS_KEY, params);
        startActivityForResult(intent, RECORD_VIDEO_REQUEST);
    }

    private void createTempFiles() {
        if (mVideoFile != null && mThumbnailFile != null) {
            return;
        }

        File dir = getContext().getExternalCacheDir();
        try {
            while (true) {
                int n = (int) (Math.random() * Integer.MAX_VALUE);
                String videoFileName = Integer.toString(n) + VIDEO_FILE_POSTFIX;
                mVideoFile = new File(dir, videoFileName);
                    if (!mVideoFile.exists() && mVideoFile.createNewFile()) {
                        String thumbnailFileName = Integer.toString(n) + THUMBNAIL_FILE_POSTFIX;
                        mThumbnailFile = new File(dir, thumbnailFileName);
                        if (!mThumbnailFile.exists() && mThumbnailFile.createNewFile()) {
                            return;
                        }
                        mVideoFile.delete();
                    }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface OnVideoRecorderListener {
        void onVideoRecorded(VideoFile videoFile);
    }
}
