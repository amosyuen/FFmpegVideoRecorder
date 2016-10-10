package com.amosyuen.videorecorder.util;


import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.amosyuen.videorecorder.video.ImageSize;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Video utilities
 */
public class VideoUtil {

    // Orientation hysteresis amount used in rounding, in degrees
    public static final int ORIENTATION_HYSTERESIS = 5;

    /**
     *  Finds the best supported camera preview size for the target size.
     */
    public static Camera.Size getBestResolution(Camera camera, ImageSize targetSize) {
        if (targetSize.areDimensionsDefined()) {
            // Find the size closest in aspect ratio that is bigger than the target size
            float aspectRatio = (float) targetSize.width / targetSize.height;
            float bestSizeAspectRatioDiff = Float.POSITIVE_INFINITY;
            Camera.Size bestSize = null;
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            for (Camera.Size size : sizes) {
                if (size.width >= targetSize.width && size.height >= targetSize.height) {
                    float aspectRatioDiff =
                            Math.abs((float) size.width / size.height - aspectRatio);
                    if (aspectRatioDiff < bestSizeAspectRatioDiff) {
                        bestSizeAspectRatioDiff = aspectRatioDiff;
                        bestSize = size;
                    }
                }
            }
            // If no size was found, find the size with the most pixels that fit in the target
            // size.
            if (bestSize == null) {
                int bestPixelCount = 0;
                for (Camera.Size size : sizes) {
                    int pixelCount = Math.min(size.width, targetSize.width)
                            * Math.min(size.height, targetSize.height);
                    if (pixelCount >= bestPixelCount) {
                        bestPixelCount = pixelCount;
                        bestSize = size;
                    }
                }
            }
            return bestSize;
        } else if (targetSize.isAtLeastOneDimensionDefined()) {
            // Choose the closest camera size whose dimension is greater than the one defined
            // dimension in the target size, or the closest size if none is greater.
            int bestSizeDiff = Integer.MIN_VALUE;
            Camera.Size bestSize = null;
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            for (Camera.Size size : sizes) {
                int sizeDiff = targetSize.width == ImageSize.SIZE_UNDEFINED
                        ? size.height - targetSize.height
                        : size.width - targetSize.width;
                if (sizeDiff == 0) {
                    bestSize = size;
                    break;
                } else if (bestSizeDiff < 0) {
                    if (sizeDiff > bestSizeDiff) {
                        bestSizeDiff = sizeDiff;
                        bestSize = size;
                    }
                } else if (sizeDiff > 0 && sizeDiff < bestSizeDiff) {
                    bestSizeDiff = sizeDiff;
                    bestSize = size;
                }
            }
            return bestSize;
        } else {
            // No sizes are defined, just return the default one
            return camera.getParameters().getPreviewSize();
        }
    }

    /**
     *  Choose the best fps range for the target fps.
     */
    public static int[] getBestFpsRange(Camera camera, float targetFps) {
        int targetFpsInt = (int)(1000f * targetFps);
        List<int[]> fpsRanges = camera.getParameters().getSupportedPreviewFpsRange();
        int bestFpsRangeAvgDist = Integer.MAX_VALUE;
        int[] bestFpsRange = null;
        // Choose the closest range whose minimum fps is greater than or equal to the target fps
        for (int[] fpsRange : fpsRanges) {
            if (fpsRange[0] >= targetFpsInt) {
                int avgFpsDist = Math.abs(targetFpsInt - (fpsRange[0] + fpsRange[1]) / 2);
                if (avgFpsDist < bestFpsRangeAvgDist) {
                    bestFpsRange = fpsRange;
                    bestFpsRangeAvgDist = avgFpsDist;
                }
            }
        }
        // If no range was found above choose the closest range whose maximum fps is greater than or
        // equal to the target fps
        if (bestFpsRange == null) {
            for (int[] fpsRange : fpsRanges) {
                if (fpsRange[1] >= targetFpsInt) {
                    int avgFpsDist = Math.abs(targetFpsInt - (fpsRange[0] + fpsRange[1]) / 2);
                    if (avgFpsDist < bestFpsRangeAvgDist) {
                        bestFpsRange = fpsRange;
                        bestFpsRangeAvgDist = avgFpsDist;
                    }
                }
            }
        }
        // If no range was found, just choose the closest range with the largest fps
        if (bestFpsRange == null) {
            for (int[] fpsRange : fpsRanges) {
                if (bestFpsRange == null || fpsRange[1] >= bestFpsRange[1]) {
                    int avgFpsDist = Math.abs(targetFpsInt - (fpsRange[0] + fpsRange[1]) / 2);
                    if (avgFpsDist < bestFpsRangeAvgDist) {
                        bestFpsRange = fpsRange;
                        bestFpsRangeAvgDist = avgFpsDist;
                    }
                }
            }
        }
        return bestFpsRange;
    }

    public static int determineDisplayOrientation(Context context, int defaultCameraId) {
        int displayOrientation = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(defaultCameraId, cameraInfo);

        int degrees = getRotationAngle(context);
        Log.v("Util", "Orientation " + degrees + " camera " + cameraInfo.orientation);

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return displayOrientation;
    }

    public static int getRotationAngle(Context context) {
        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_0:
            default:
                return 0;
        }
    }

    public static int roundOrientation(int orientation, int lastOrientation) {
        boolean changeOrientation;
        if (lastOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - lastOrientation);
            dist = Math.min(dist, 360 - dist);
            changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return lastOrientation;
    }
}
