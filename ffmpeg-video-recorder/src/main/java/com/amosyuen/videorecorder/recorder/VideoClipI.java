package com.amosyuen.videorecorder.recorder;

import com.amosyuen.videorecorder.camera.CameraControllerI;

import java.io.File;

/**
 * Video clip with some additional metadata.
 */
interface VideoClipI {
    File getFile();
    CameraControllerI.Facing getFacing();
    int getOrientationDegrees();
}
