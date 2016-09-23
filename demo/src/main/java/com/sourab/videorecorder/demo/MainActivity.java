package com.sourab.videorecorder.demo;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.sourab.videorecorder.FFmpegRecorderActivity;

import java.security.Permissions;

import static java.security.AccessController.getContext;

/**
 * Simple demo activity that request the required permissions if not granted, then starts the video
 * recording activity.
 */
public class MainActivity extends Activity {

    private static final String[] VIDEO_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private static final int REQUEST_VIDEO_PERMISSIONS = 1000;

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_VIDEO_PERMISSIONS:
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
            case REQUEST_VIDEO_PERMISSIONS:
                handleRequestPermissionsResult();
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
    }

    private void checkPermissions() {
        if (hasAllPermissions()) {
            launchVideoRecorder();
            return;
        }

        ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
    }

    private void handleRequestPermissionsResult() {
        if (hasAllPermissions()) {
            launchVideoRecorder();
        } else if (shouldShowRequestPermissionRationale()) {
            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
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
        startActivity(intent);
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
        startActivityForResult(intent, REQUEST_VIDEO_PERMISSIONS);
    }
}
