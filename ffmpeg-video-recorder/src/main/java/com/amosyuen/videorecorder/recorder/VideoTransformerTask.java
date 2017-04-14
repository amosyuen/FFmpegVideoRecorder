package com.amosyuen.videorecorder.recorder;


import android.text.TextUtils;
import android.util.Log;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.VideoTransformerParamsI;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Task that combines multiple video files and transforms video into the desired size.
 */
public class VideoTransformerTask implements Runnable {

    protected static final String LOG_TAG = "VideoTransformerTask";

    protected final FFmpegFrameRecorder mRecorder;
    protected final VideoTransformerParamsI mParams;
    protected Collection<? extends VideoClipI> mClips;
    protected TaskListener mProgressListener;

    public VideoTransformerTask(
            FFmpegFrameRecorder recorder,
            VideoTransformerParamsI params,
            Collection<? extends VideoClipI> filesToTransform) {
        mRecorder = Preconditions.checkNotNull(recorder);
        mParams = Preconditions.checkNotNull(params);
        mClips = Preconditions.checkNotNull(filesToTransform);
        Preconditions.checkArgument(!filesToTransform.isEmpty());
    }

    public TaskListener getProgressListener() {
        return mProgressListener;
    }

    public void setProgressListener(TaskListener progressListener) {
        mProgressListener = progressListener;
    }

    @Override
    public void run() {
        if (mProgressListener != null) {
            mProgressListener.onStart();
        }

        HashMap<FilterParams, FFmpegFrameFilter> filterMap = new HashMap<>();
        try {
            long totalMillis = 0;
            LinkedList<FrameGrabberWrapper> frameGrabbers = new LinkedList<>();
            ImageSize targetSize = mParams.getVideoSize();
            ImageSize.Builder outputSizeBuilder = ImageSize.UNDEFINED.toBuilder();
            for (VideoClipI clip : mClips) {
                Log.v(LOG_TAG, String.format("Transforming clip %s", clip));

                FrameGrabber frameGrabber = new FFmpegFrameGrabber(clip.getFile());
                frameGrabber.start();
                totalMillis += frameGrabber.getLengthInTime();
                ImageSize recordedSize =
                        new ImageSize(frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
                FilterParams filterParams = FilterParams.create(
                        recordedSize, (int) Math.round(frameGrabber.getFrameRate()),
                        clip.getFacing(), clip.getOrientationDegrees());
                frameGrabbers.add(FrameGrabberWrapper.create(frameGrabber, filterParams));

                // Calculate the transformed size and take the max of them to determine the actual
                // output size as different files may have different transformed sizes.
                outputSizeBuilder.max(calculateTransformedSize(getRotatedRecordedSize(
                        recordedSize, clip.getOrientationDegrees()), targetSize));
            }
            ImageSize outputSize = outputSizeBuilder.build();
            Log.d(LOG_TAG, String.format(
                    "Start transforming %d files of length %.3fs to output size %s",
                    mClips.size(),
                    (float) totalMillis / TimeUnit.SECONDS.toMillis(1),
                    outputSize));

            // Initialize recorder
            if (mRecorder.getAudioChannels() < 0) {
                mRecorder.setAudioChannels(
                        frameGrabbers.peek().getFrameGrabber().getAudioChannels());
            }
            mRecorder.setImageWidth(outputSize.getWidthUnchecked());
            mRecorder.setImageHeight(outputSize.getHeightUnchecked());
            mRecorder.start();

            long processedMillis = 0;
            while (!frameGrabbers.isEmpty()) {
                FrameGrabberWrapper wrapper = frameGrabbers.poll();
                FrameGrabber frameGrabber = wrapper.getFrameGrabber();
                // Create a filter to transform image size into desired size if needed
                FFmpegFrameFilter filter =
                        getFilter(filterMap, wrapper.getFilterParams(), outputSize);
                long currMillis = 0;
                Frame frame;
                while ((frame = frameGrabber.grabFrame()) != null) {
                    Preconditions.checkState((frame.image != null) ^ (frame.samples != null));
                    if (frame.image != null && filter != null) {
                        filter.push(frame);
                        frame = Preconditions.checkNotNull(filter.pull());
                    }
                    long timestampMillis = processedMillis + frameGrabber.getTimestamp();
                    if (timestampMillis > mRecorder.getTimestamp()) {
                        mRecorder.setTimestamp(timestampMillis);
                    }
                    mRecorder.record(frame);
                    currMillis = Math.max(currMillis, frameGrabber.getTimestamp());
                    if (mProgressListener != null) {
                        mProgressListener.onProgress(
                                (int) (processedMillis + currMillis), (int) totalMillis);
                    }
                }
                processedMillis += frameGrabber.getLengthInTime();
                frameGrabber.stop();
                frameGrabber.release();
            }
            mRecorder.stop();
            Log.v(LOG_TAG, "Finished transforming");
        } catch (FrameGrabber.Exception | FrameFilter.Exception | FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (mRecorder != null) {
                try {
                    mRecorder.release();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error releasing recorder", e);
                }
            }
            for (FFmpegFrameFilter filter : filterMap.values()) {
                try {
                    filter.stop();
                    filter.release();
                } catch (FrameFilter.Exception e) {
                    Log.e(LOG_TAG, "Error releasing filter", e);
                }
            }
        }
        if (mProgressListener != null) {
            mProgressListener.onDone();
        }
    }

    protected static ImageSize getRotatedRecordedSize(ImageSize recordedSize, int rotationDegrees) {
        return (rotationDegrees % 180 == 90)
                ? recordedSize.toBuilder().invert().build()
                : recordedSize;
    }

    /**
     * Returns the resulting transformed size for a specific recorded size.
     */
    protected ImageSize calculateTransformedSize(ImageSize recordedSize, ImageSize targetSize) {
        if (!targetSize.areBothDimensionsDefined()) {
            targetSize = targetSize.toBuilder()
                    .calculateUndefinedDimensions(recordedSize)
                    .build();
        }
        ImageSize.Builder builder = recordedSize.toBuilder()
                .scale(targetSize, mParams.getVideoImageFit(), mParams.getVideoImageScale());
        if (mParams.getShouldCropVideo()) {
            builder.min(targetSize);
        }
        if (mParams.getShouldPadVideo()) {
            builder.max(targetSize);
        }
        return builder.build();
    }

    // Create a filter to transform the frame to the desired size. Uses an existing filter if the
    // params match.
    protected FFmpegFrameFilter getFilter(
            HashMap<FilterParams, FFmpegFrameFilter> filterMap,
            FilterParams params, ImageSize outputSize)
                    throws FrameFilter.Exception {
        ImageSize targetSize = mParams.getVideoSize();
        int rotationDegrees = params.getRotationDegrees();
        ImageSize imageSize = params.getImageSize();
        ImageSize rotatedImageSize = getRotatedRecordedSize(imageSize, rotationDegrees);
        if (targetSize.isOneDimensionDefined()) {
            targetSize = targetSize.toBuilder()
                    .calculateUndefinedDimensions(rotatedImageSize)
                    .build();
        }
        if (rotationDegrees == 0 && params.getFacing() == CameraControllerI.Facing.BACK
                && imageSize.equals(targetSize)) {
            return null;
        }

        FFmpegFrameFilter filter = filterMap.get(params);
        if (filter != null) {
            return filter;
        }

        ArrayList<String> transforms = new ArrayList<>();
        if (params.getFacing() == CameraControllerI.Facing.FRONT) {
            transforms.add("hflip");
        }
        switch (rotationDegrees) {
            case 0:
                break;
            case 90:
                transforms.add("transpose=clock");
                break;
            case 180:
                transforms.add("vflip");
                transforms.add("hflip");
                break;
            case 270:
                transforms.add("transpose=cclock");
                break;
            default:
                throw new InvalidParameterException(String.format(
                        Locale.US, "Unsupported rotation %d", rotationDegrees));
        }
        ImageSize.Builder imageSizeBuilder = rotatedImageSize.toBuilder()
                .scale(targetSize, mParams.getVideoImageFit(), mParams.getVideoImageScale());
        transforms.add("scale=" + imageSizeBuilder.getWidthUnchecked()
                + ":" + imageSizeBuilder.getHeightUnchecked());
        if (mParams.getShouldCropVideo()) {
            imageSizeBuilder.min(targetSize);
            transforms.add("crop=" + imageSizeBuilder.getWidthUnchecked()
                        + ":" + imageSizeBuilder.getHeightUnchecked());
        }
        if (imageSizeBuilder.getWidthUnchecked() < outputSize.getWidthUnchecked()
                || imageSizeBuilder.getHeightUnchecked() < outputSize.getHeightUnchecked()) {
            transforms.add("pad=" + outputSize.getWidthUnchecked()
                    + ":" + outputSize.getHeightUnchecked() + ":(ow-iw)/2:(oh-ih)/2");
        }

        Log.v(LOG_TAG, String.format(
                "Creating filter with transforms %s for params %s", transforms, params));
        filter = new FFmpegFrameFilter(
                TextUtils.join(",", transforms),
                imageSize.getWidthUnchecked(),
                imageSize.getHeightUnchecked());
        filter.setFrameRate(params.getFrameRate());
        filter.start();
        filterMap.put(params, filter);
        return filter;
    }

    @AutoValue
    public abstract static class FilterParams {
        public abstract ImageSize getImageSize();
        public abstract int getFrameRate();
        public abstract CameraControllerI.Facing getFacing();
        public abstract int getRotationDegrees();

        public static FilterParams create(ImageSize imageSize, int frameRate,
                CameraControllerI.Facing facing, int rotationDegrees) {
            return new AutoValue_VideoTransformerTask_FilterParams(
                    imageSize, frameRate, facing, rotationDegrees);
        }
    }

    @AutoValue
    public abstract static class FrameGrabberWrapper {
        public abstract FrameGrabber getFrameGrabber();
        public abstract FilterParams getFilterParams();

        public static FrameGrabberWrapper create(FrameGrabber frameGrabber, FilterParams filterParams) {
            return new AutoValue_VideoTransformerTask_FrameGrabberWrapper(
                    frameGrabber, filterParams);
        }
    }

    /**
     * Progress listener
     */
    public interface TaskListener {
        void onStart();
        void onProgress(int progress, int total);
        void onDone();
    }
}
