package com.amosyuen.videorecorder.demo;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.amosyuen.videorecorder.FFmpegRecorderActivity;
import com.amosyuen.videorecorder.util.RecorderActivityParams;

import java.io.File;
import java.io.IOException;

/**
 * Simple demo activity that request the required permissions if not granted, then starts the video
 * recording activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final String[] VIDEO_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private static final int REQUEST_VIDEO_PERMISSIONS_REQUEST = 1000;
    private static final int RECORD_VIDEO_REQUEST = 1001;

    private File mVideoFile;
    private File mThumbnailFile;

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
                        String msg = "Recorded video at " + videoUri;
                        Uri thumbnailUri =
                                data.getParcelableExtra(FFmpegRecorderActivity.THUMBNAIL_URI_KEY);
                        if (thumbnailUri != null) {
                            msg += "\n\nRecorded thumbnail at " + thumbnailUri;
                        }
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle(R.string.success)
                                .setMessage(msg)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    case FFmpegRecorderActivity.RESULT_ERROR:
                        Exception error = (Exception)
                                data.getSerializableExtra(FFmpegRecorderActivity.ERROR_PATH_KEY);
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle(R.string.error)
                                .setMessage(error.getLocalizedMessage())
                                .setPositiveButton(R.string.ok, null)
                                .show();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button launchButton = (Button) findViewById(R.id.launch_button);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
            }
        });

        try {
            mVideoFile = createTempFile("video", ".mp4");
            mThumbnailFile = createTempFile("thumbnail", ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        if (hasAllPermissions()) {
            launchVideoRecorder();
            return;
        }

        ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS,
                REQUEST_VIDEO_PERMISSIONS_REQUEST);
    }

    private void handleRequestPermissionsResult() {
        if (hasAllPermissions()) {
            launchVideoRecorder();
        } else if (shouldShowRequestPermissionRationale()) {
            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS,
                    REQUEST_VIDEO_PERMISSIONS_REQUEST);
        } else {
            new AlertDialog.Builder(this)
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

    private void launchVideoRecorder() {
        Intent intent = new Intent(this, FFmpegRecorderActivity.class);
        RecorderActivityParams params = new RecorderActivityParams.Builder(mVideoFile)
                .videoThumbnailOutput(mThumbnailFile)
                .videoWidth(480)
                .videoHeight(640)
                .build();
        intent.putExtra(FFmpegRecorderActivity.RECORDER_ACTIVITY_PARAMS_KEY, params);
        startActivityForResult(intent, RECORD_VIDEO_REQUEST);
    }

    private File createTempFile(String prefix, String postfix) throws IOException {
        File file = File.createTempFile(prefix, postfix, getExternalCacheDir());
        file.deleteOnExit();
        return file;
    }

    public boolean hasAllPermissions() {
        for (String permission : VIDEO_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldShowRequestPermissionRationale() {
        for (String permission : VIDEO_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    private void startAppDetailsIntent() {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, REQUEST_VIDEO_PERMISSIONS_REQUEST);
    }
}
