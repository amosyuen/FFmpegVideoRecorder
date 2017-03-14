package com.amosyuen.videorecorder.util;


import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.WindowManager;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Video utilities
 */
public class VideoUtil {

    /**
     *  Finds the best supported camera preview size for the target size.
     *  Target size must have at least one dimension specified and should be rotated into the same
     *  orientation as the preview sizes.
     */
    public static ImageSize getBestResolution(List<ImageSize> previewSizes,
            ImageSize targetSize, ImageFit imageFit, ImageScale imageScale) {
        Preconditions.checkState(targetSize.isAtLeastOneDimensionDefined());
        ImageSize bestSize = null;
        float bestScore = 0;
        for (ImageSize size : previewSizes) {
            float score = calculateCameraSizeScore(size, targetSize, imageFit, imageScale);
            if (bestSize == null || score > bestScore) {
                bestSize = size;
                bestScore = score;
            }
        }
        return bestSize;
    }

    /**
     * Calculate a score for how good a camera size is for recording the desired target size based
     * on these goals:
     * <ul>
     * <li> Increase recorded image pixels (that aren't scaled up) </li>
     * <li> Decrease camera pixels recorded that are wasted </li>
     * </ul>
     */
    public static float calculateCameraSizeScore(
            ImageSize cameraSize, ImageSize targetSize,
            ImageFit imageFit, ImageScale imageScale) {
        if (!targetSize.areDimensionsDefined()) {
            targetSize = targetSize.toBuilder().calculateUndefinedDimensions(cameraSize).build();
        }
        // Find the intersection of these sizes to find the recorded non upscaled pixels:
        // 1) Camera size scaled to target size
        // 2) Target size
        // 3) Camera size
        long recordedNonUpscaledPixels = cameraSize.toBuilder()
                .scale(targetSize, imageFit, imageScale)
                .min(targetSize)
                .min(cameraSize)
                .getArea();
        long pixelsWasted = Math.max(0, cameraSize.getArea() - recordedNonUpscaledPixels);
        float score = 100f * recordedNonUpscaledPixels - pixelsWasted;
        return score;
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
    public static int determineCameraDisplayRotation(
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

    public static int getContextRotation(Context context) {
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

    public static int roundOrientation(int orientation) {
        return ((orientation + 45) / 90 * 90) % 360;
    }
}
