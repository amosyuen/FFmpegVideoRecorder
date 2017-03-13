package com.amosyuen.videorecorder.util;


import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.EncoderParams;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Video utilities
 */
public class VideoUtil {

    /**
     *  Finds the best supported camera preview size for the target size.
     *  Target size must have at least one dimension specified.
     */
    public static ImageSize getBestResolution(
            List<ImageSize> previewSizes, ImageSize targetSize, boolean isRecordingLandscape) {
        Preconditions.checkState(targetSize.isAtLeastOneDimensionDefined());
        if (!isRecordingLandscape) {
            //noinspection SuspiciousNameCombination
            targetSize = new ImageSize(targetSize.height, targetSize.width);
        }
        if (targetSize.areDimensionsDefined()) {
            // Find the size closest in aspect ratio that is bigger than the target size
            float aspectRatio = (float) targetSize.width / targetSize.height;
            float bestSizeAspectRatioDiff = Float.POSITIVE_INFINITY;
            ImageSize bestSize = null;
            for (ImageSize size : previewSizes) {
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
                for (ImageSize size : previewSizes) {
                    int pixelCount = Math.min(size.width, targetSize.width)
                            * Math.min(size.height, targetSize.height);
                    if (pixelCount >= bestPixelCount) {
                        bestPixelCount = pixelCount;
                        bestSize = size;
                    }
                }
            }
            return bestSize;
        }

        // Choose the closest camera size whose dimension is greater than the one defined
        // dimension in the target size, or the closest size if none is greater.
        int bestSizeDiff = Integer.MIN_VALUE;
        ImageSize bestSize = null;
        for (ImageSize size : previewSizes) {
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
    }

    /**
     *  Choose the best fps range for the target fps.
     */
    public static int[] getBestFpsRange(List<int[]> fpsRanges, float targetFps) {
        int targetFpsInt = (int)(1000f * targetFps);
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

    /**
     * Determine the orientation to display the camera surface.
     */
    public static int determineCameraSurfaceOrientation(
            int surfaceOrientationDegrees,
            int cameraOrientationDegrees,
            CameraControllerI.Facing cameraFacing) {
        if (cameraFacing == CameraControllerI.Facing.FRONT) {
            int displayOrientation = (cameraOrientationDegrees + surfaceOrientationDegrees) % 360;
            return (360 - displayOrientation) % 360;
        } else {
            return (cameraOrientationDegrees - surfaceOrientationDegrees + 360) % 360;
        }
    }

    public static boolean isLandscapeAngle(int orientationDegrees) {
        return (orientationDegrees > 45 && orientationDegrees < 135)
                || (orientationDegrees > 225 && orientationDegrees < 315);
    }

    public static int getContextOrientationDegrees(Context context) {
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

    public static boolean isContextLandscape(Context context) {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }
}
