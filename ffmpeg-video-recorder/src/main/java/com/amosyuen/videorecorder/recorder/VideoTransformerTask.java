package com.amosyuen.videorecorder.recorder;


import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;

import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.VideoTransformerParamsI;
import com.amosyuen.videorecorder.util.Util;
import com.google.common.base.Preconditions;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
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
    protected Collection<File> mFilesToTransform;
    protected TaskListener mProgressListener;

    public VideoTransformerTask(
            FFmpegFrameRecorder recorder,
            VideoTransformerParamsI params,
            Collection<File> filesToTransform) {
        mRecorder = Preconditions.checkNotNull(recorder);
        mParams = Preconditions.checkNotNull(params);
        mFilesToTransform = Preconditions.checkNotNull(filesToTransform);
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
            long processedMillis = 0;
            long totalMillis = 0;
            LinkedList<FrameGrabberWrapper> frameGrabbers = new LinkedList<>();
            ImageSize targetSize = mParams.getVideoSize();
            ImageSize.Builder outputSizeBuilder = ImageSize.UNDEFINED.toBuilder();
            for (File file : mFilesToTransform) {
                int rotationDegrees = getRotationDegrees(file);
                Log.e(LOG_TAG, String.format("File with rotation %d", rotationDegrees));

                FrameGrabber frameGrabber = new FFmpegFrameGrabber(file);
                frameGrabber.start();
                totalMillis += frameGrabber.getLengthInTime();
                ImageSize recordedSize =
                        new ImageSize(frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
                FilterParams filterParams = new FilterParams(
                        recordedSize, frameGrabber.getPixelFormat(), rotationDegrees);
                frameGrabbers.add(new FrameGrabberWrapper(frameGrabber, filterParams));

                // Calculate the transformed size and take the max of them to determine the actual
                // output size as different files may have different transformed sizes.
                outputSizeBuilder.max(calculateTransformedSize(
                        getRotatedRecordedSize(recordedSize, rotationDegrees), targetSize));
            }
            ImageSize outputSize = outputSizeBuilder.build();
            Log.d(LOG_TAG, String.format(
                    "Start transforming %d files of length %.3fs to output size %s",
                    mFilesToTransform.size(),
                    (float) totalMillis / TimeUnit.SECONDS.toMillis(1),
                    outputSize));

            // Initialize recorder
            if (mRecorder.getAudioChannels() < 0) {
                mRecorder.setAudioChannels(frameGrabbers.peek().frameGrabber.getAudioChannels());
            }
            mRecorder.setImageWidth(outputSize.getWidthUnchecked());
            mRecorder.setImageHeight(outputSize.getHeightUnchecked());
            mRecorder.start();

            while (!frameGrabbers.isEmpty()) {
                FrameGrabberWrapper wrapper = frameGrabbers.poll();
                FrameGrabber frameGrabber = wrapper.frameGrabber;
                // Create a filter to transform image size into desired size if needed
                FFmpegFrameFilter filter = getFilter(filterMap, wrapper.filterParams, outputSize);
                long lastMillis = 0;
                Frame frame;
                while ((frame = frameGrabber.grabFrame()) != null) {
                    if (filter == null) {
                        mRecorder.record(frame);
                    } else {
                        filter.push(frame);
                        while ((frame = filter.pull()) != null) {
                            mRecorder.record(frame);
                        }
                    }
                    processedMillis += frameGrabber.getTimestamp() - lastMillis;
                    lastMillis = frameGrabber.getTimestamp();
                    if (mProgressListener != null) {
                        mProgressListener.onProgress((int) processedMillis, (int) totalMillis);
                    }
                }
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

    protected static int getRotationDegrees(File file) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(file.getAbsolutePath());
        String rotation = metadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        metadataRetriever.release();
        return rotation == null ? 0 : Util.roundOrientation(Integer.parseInt(rotation));
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
        if (targetSize.isOneDimensionDefined()) {
            targetSize = targetSize.toBuilder()
                    .calculateUndefinedDimensions(
                            getRotatedRecordedSize(params.imageSize, params.rotationDegrees))
                    .build();
        }
        if (params.imageSize.equals(targetSize) && params.rotationDegrees == 0) {
            return null;
        }

        FFmpegFrameFilter filter = filterMap.get(params);
        if (filter != null) {
            return filter;
        }

        ArrayList<String> transforms = new ArrayList<>();
        switch (params.rotationDegrees) {
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
                        Locale.US, "Unsupported rotation %d", params.rotationDegrees));
        }
        ImageSize.Builder imageSizeBuilder = params.imageSize.toBuilder()
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

        filter = new FFmpegFrameFilter(
                TextUtils.join(",", transforms),
                params.imageSize.getWidthUnchecked(),
                params.imageSize.getHeightUnchecked());
        filter.setPixelFormat(params.pixelFormat);
        filter.start();
        filterMap.put(params, filter);
        return filter;
    }

    protected static class FilterParams {
        public ImageSize imageSize;
        public int pixelFormat;
        public int rotationDegrees;

        public FilterParams(ImageSize imageSize, int pixelFormat, int rotationDegrees) {
            this.imageSize = imageSize;
            this.pixelFormat = pixelFormat;
            this.rotationDegrees = rotationDegrees;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilterParams that = (FilterParams) o;

            if (pixelFormat != that.pixelFormat) return false;
            if (rotationDegrees != that.rotationDegrees) return false;
            return imageSize != null
                    ? imageSize.equals(that.imageSize)
                    : that.imageSize == null;

        }

        @Override
        public int hashCode() {
            int result = imageSize != null ? imageSize.hashCode() : 0;
            result = 31 * result + pixelFormat;
            result = 31 * result + rotationDegrees;
            return result;
        }
    }

    protected static class FrameGrabberWrapper {
        public final FrameGrabber frameGrabber;
        public final FilterParams filterParams;

        public FrameGrabberWrapper(
                FrameGrabber frameGrabber,
                FilterParams filterParams) {
            this.frameGrabber = frameGrabber;
            this.filterParams = filterParams;
        }
    }

    public interface FFmpegFrameRecorderCustomizer {
        FFmpegFrameRecorder create(FFmpegFrameRecorder recorder);
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
