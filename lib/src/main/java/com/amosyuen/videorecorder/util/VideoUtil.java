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
     *  If both dimensions are defined, pick the size that is equal to or bigger than the desired
     *  size and has the closest aspect ratio. If no sizes are big enough, just pick the largest
     *  size.
     *
     *  If one dimension is specified, just pick the first size that is equal to or bigger than the
     *  desired dimension. If no sizes are big enough, just pick the largest size.
     *
     *  If zero dimensions are specified, just pick the first size.
     */
    public static Camera.Size getBestResolution(Camera camera, ImageSize imageSize) {
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        Collections.sort(sizes, new ResolutionComparator());
        float aspectRatio =
                imageSize.areDimensionsDefined() ? (float) imageSize.width / imageSize.height : 0f;
        float bestSizeAspectRatioDiff = Float.POSITIVE_INFINITY;
        Camera.Size bestSize = null;
        for (Camera.Size size : sizes) {
            if (size.width >= imageSize.width && size.height >= imageSize.height) {
                if (aspectRatio > 0f) {
                    float aspectRatioDiff =
                            Math.abs((float) size.width / size.height - aspectRatio);
                    if (aspectRatioDiff < bestSizeAspectRatioDiff) {
                        bestSizeAspectRatioDiff = aspectRatioDiff;
                        bestSize = size;
                    }
                } else if (bestSize == null) {
                    bestSize = size;
                }
            }
        }
        return bestSize == null ? sizes.get(sizes.size() - 1) : bestSize;
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

    public static class ResolutionComparator implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            if (size1.height != size2.height)
                return size1.height - size2.height;
            else
                return size1.width - size2.width;
        }
    }
}
