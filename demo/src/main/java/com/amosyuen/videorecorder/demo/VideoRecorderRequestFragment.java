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
import android.support.v4.app.ActivityCompat;
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

import com.amosyuen.videorecorder.FFmpegRecorderActivity;
import com.amosyuen.videorecorder.util.FFmpegRecorderParams.AudioCodec;
import com.amosyuen.videorecorder.util.FFmpegRecorderParams.VideoCodec;
import com.amosyuen.videorecorder.util.RecorderActivityParams;
import com.amosyuen.videorecorder.video.ImageSize.ScaleType;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static java.io.File.createTempFile;


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
    private EditText mVideoQualityEditText;
    private EditText mVideoBitrateEditText;
    private EditText mVideoFrameRateEditText;
    private Spinner mVideoCameraFacingSpinner;
    private Spinner mVideoScaleTypeSpinner;
    private SwitchCompat mVideoCanUpscaleSwitchCompat;

    private Spinner mAudioCodecSpinner;
    private EditText mAudioQualityEditText;
    private EditText mAudioSampleRateEditText;
    private EditText mAudioBitrateEditText;
    private Spinner mAudioChannelCount;

    private EditText mMinRecordingTimeEditText;
    private EditText mMaxRecordingTimeEditText;

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

        mVideoQualityEditText = (EditText) view.findViewById(R.id.video_quality);
        mVideoQualityEditText.setText("");

        mVideoFrameRateEditText = (EditText) view.findViewById(R.id.video_frame_rate);
        mVideoFrameRateEditText.setText("30");

        mVideoCameraFacingSpinner = (Spinner) view.findViewById(R.id.video_camera_facing);
        mVideoCameraFacingSpinner.setAdapter(new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item,
                new String[] { "Back", "Front" }));

        mVideoScaleTypeSpinner = (Spinner) view.findViewById(R.id.video_scale_type);
        mVideoScaleTypeSpinner.setAdapter(new ArrayAdapter<ScaleType>(
                        getContext(), android.R.layout.simple_spinner_item, ScaleType.values()));

        mVideoCanUpscaleSwitchCompat = (SwitchCompat) view.findViewById(R.id.video_can_upscale);

        ArrayAdapter<AudioCodec> audioCodecAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, AudioCodec.values());
        mAudioCodecSpinner = (Spinner) view.findViewById(R.id.audio_codec);
        mAudioCodecSpinner.setAdapter(audioCodecAdapter);
        mAudioCodecSpinner.setSelection(audioCodecAdapter.getPosition(AudioCodec.AAC));

        mAudioSampleRateEditText = (EditText) view.findViewById(R.id.audio_sample_rate);
        mAudioSampleRateEditText.setText("44100");

        mAudioBitrateEditText = (EditText) view.findViewById(R.id.audio_bitrate);
        mAudioBitrateEditText.setText("64000");

        mAudioQualityEditText = (EditText) view.findViewById(R.id.audio_quality);
        mAudioQualityEditText.setText("");

        mAudioChannelCount = (Spinner) view.findViewById(R.id.audio_channel_count);
        mAudioChannelCount.setAdapter(new ArrayAdapter<Integer>(
                getContext(), android.R.layout.simple_spinner_item, new Integer[] { 1, 2 }));

        mMinRecordingTimeEditText = (EditText) view.findViewById(R.id.min_recording_time);
        mMinRecordingTimeEditText.setText("1");

        mMaxRecordingTimeEditText = (EditText) view.findViewById(R.id.max_recording_time);
        mMaxRecordingTimeEditText.setText("10");

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
        
        ActivityCompat.requestPermissions(getActivity(), VIDEO_PERMISSIONS,
                REQUEST_VIDEO_PERMISSIONS_REQUEST);
    }
    
    private void handleRequestPermissionsResult() {
        if (hasAllPermissions()) {
            launchVideoRecorder();
        } else if (shouldShowRequestPermissionRationale()) {
            ActivityCompat.requestPermissions(getActivity(), VIDEO_PERMISSIONS,
                    REQUEST_VIDEO_PERMISSIONS_REQUEST);
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
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
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

    private void validateOneOfBitrateAndQualityAreSet(
            EditText bitrateEditText, EditText qualityEditText, int bitrate, int quality) {
        if (bitrateEditText.getError() == null && qualityEditText.getError() == null) {
            if ((quality == -1) == (bitrate == 0)) {
                bitrateEditText.setError(getString(R.string.error_one_defined));
                qualityEditText.setError(getString(R.string.error_one_defined));
                if (mErrorView == null) {
                    mErrorView = qualityEditText;
                }
            }
        }
    }

    private void launchVideoRecorder() {
        mErrorView = null;
        int videoWidth = validateEmptyPositiveIntegerEditText(mVideoWidthEditText);
        int videoHeight = validateEmptyPositiveIntegerEditText(mVideoHeightEditText);
        int videoQuality = validateEmptyNonNegativeIntegerEditText(mVideoQualityEditText);
        int videoBitrate = validateEmptyPositiveIntegerEditText(mVideoBitrateEditText);
        validateOneOfBitrateAndQualityAreSet(
                mVideoBitrateEditText, mVideoQualityEditText, videoBitrate, videoQuality);
        int videoFrameRate = validatePositiveIntegerEditText(mVideoFrameRateEditText);

        int audioSampleRate = validatePositiveIntegerEditText(mAudioSampleRateEditText);
        int audioQuality = validateEmptyNonNegativeIntegerEditText(mAudioQualityEditText);
        int audioBitrate = validateEmptyPositiveIntegerEditText(mAudioBitrateEditText);
        validateOneOfBitrateAndQualityAreSet(
                mAudioBitrateEditText, mAudioQualityEditText, audioBitrate, audioQuality);

        int minRecordingTime = validateEmptyNonNegativeIntegerEditText(mMinRecordingTimeEditText);
        int maxRecordingTime = validateEmptyNonNegativeIntegerEditText(mMaxRecordingTimeEditText);

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
                .videoQuality(videoQuality)
                .videoBitrate(videoBitrate)
                .videoFrameRate(videoFrameRate)

                .videoCameraFacing(mVideoCameraFacingSpinner.getSelectedItemPosition())
                .videoScaleType((ScaleType) mVideoScaleTypeSpinner.getSelectedItem())
                .canUpscaleVideo(mVideoCanUpscaleSwitchCompat.isChecked())

                .audioCodec((AudioCodec) mAudioCodecSpinner.getSelectedItem())
                .audioSamplingRateHz(audioSampleRate)
                .audioQuality(audioQuality)
                .audioBitrate(audioBitrate)
                .audioChannelCount((Integer) mAudioChannelCount.getSelectedItem())

                .minRecordingTimeNanos(TimeUnit.SECONDS.toNanos(minRecordingTime))
                .maxRecordingTimeNanos(TimeUnit.SECONDS.toNanos(maxRecordingTime))
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
