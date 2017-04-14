package com.amosyuen.videorecorder.camera;

import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Utils for camera controllers.
 */
public final class CameraUtil {
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
        if (!targetSize.areBothDimensionsDefined()) {
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
        return 100f * recordedNonUpscaledPixels - pixelsWasted;
    }

    /**
     *  Choose the best fps range for the target fps. Tries to choose an fps range with a max
     *  greater than the target FPS. Also tries to choose an FPS range with the largest range to
     *  allow the most flexibility with exposure.
     */
    public static int[] getBestFpsRange(List<int[]> fpsRanges, float targetFps) {
        int targetFpsInt = (int)(1000f * targetFps);
        int[] bestFpsRange = null;
        int bestFpsRangeSize = -1;
        // Choose the range whose max fps is greater than the target FPS and has the greatest range.
        for (int[] fpsRange : fpsRanges) {
            if (fpsRange[1] >= targetFpsInt) {
                int fpsRangeSize = fpsRange[1] - fpsRange[0];
                if (fpsRangeSize > bestFpsRangeSize) {
                    bestFpsRange = fpsRange;
                    bestFpsRangeSize = fpsRangeSize;
                }
            }
        }
        // If no range was found then choose the range with the max closest to the target FPS.
        if (bestFpsRange == null) {
            for (int[] fpsRange : fpsRanges) {
                int fpsRangeSize = fpsRange[1] - fpsRange[0];
                if (bestFpsRange == null || fpsRange[1] > bestFpsRange[1]
                        || fpsRangeSize > bestFpsRangeSize) {
                    bestFpsRange = fpsRange;
                    bestFpsRangeSize = fpsRangeSize;
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

    private CameraUtil() {}
}
