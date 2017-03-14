package com.amosyuen.videorecorder.recorder;


import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;

import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.EncoderParams;
import com.amosyuen.videorecorder.util.Util;
import com.amosyuen.videorecorder.util.VideoUtil;

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
 * Combined multiple video files and transforms video to the desired size.
 */
public class VideoTransformerTask implements Runnable {

    protected static final String LOG_TAG = "VideoTransformerTask";

    protected File mVideoOutputFile;
    protected final EncoderParams mParams;
    protected Collection<File> mFilesToTransform;
    protected TaskListener mProgressListener;

    public VideoTransformerTask(
            File videoOutputFile,
            EncoderParams params,
            Collection<File> filesToTransform) {
        mVideoOutputFile = videoOutputFile;
        mParams = params;
        mFilesToTransform = filesToTransform;
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
        FFmpegFrameRecorder recorder = null;
        try {
            long processedMillis = 0;
            long totalMillis = 0;
            LinkedList<FrameGrabberWrapper> frameGrabbers = new LinkedList<>();
            ImageSize.Builder maxRecordedSizeBuilder = ImageSize.UNDEFINED.toBuilder();
            for (File file : mFilesToTransform) {
                int rotationDegrees = getRotationDegrees(file);

                FrameGrabber frameGrabber = new FFmpegFrameGrabber(file);
                frameGrabber.start();
                totalMillis += frameGrabber.getLengthInTime();
                ImageSize recordedSize =
                        new ImageSize(frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
                FilterParams filterParams = new FilterParams(
                        recordedSize, frameGrabber.getPixelFormat(), rotationDegrees);
                frameGrabbers.add(new FrameGrabberWrapper(frameGrabber, filterParams));

                ImageSize rotatedRecordedSize = (rotationDegrees % 180 == 90)
                        ? recordedSize.toBuilder().invert().build()
                        : recordedSize;
                maxRecordedSizeBuilder.max(rotatedRecordedSize);
            }
            ImageSize maxRecordedSize = maxRecordedSizeBuilder.build();
            ImageSize targetSize = mParams.getVideoSize();
            if (!targetSize.areDimensionsDefined()) {
                targetSize = targetSize.toBuilder()
                        .calculateUndefinedDimensions(maxRecordedSize)
                        .build();
            }
            Log.d(LOG_TAG, String.format(
                    "Start transforming %d files of length %.3fs to target size %s",
                    mFilesToTransform.size(),
                    (float) totalMillis / TimeUnit.SECONDS.toMillis(1),
                    targetSize));

            recorder = Util.createFrameRecorder(mVideoOutputFile, mParams);
            recorder.setImageWidth(targetSize.width);
            recorder.setImageHeight(targetSize.height);
            recorder.start();

            while (!frameGrabbers.isEmpty()) {
                FrameGrabberWrapper wrapper = frameGrabbers.poll();
                FrameGrabber frameGrabber = wrapper.frameGrabber;
                // Create a filter to transform image size into desired size if needed
                FFmpegFrameFilter filter =
                        getFilter(filterMap, wrapper.filterParams, maxRecordedSize, targetSize);
                long lastMillis = 0;
                Frame frame;
                while ((frame = frameGrabber.grabFrame()) != null) {
                    if (filter == null) {
                        recorder.record(frame);
                    } else {
                        filter.push(frame);
                        while ((frame = filter.pull()) != null) {
                            recorder.record(frame);
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
            recorder.stop();
            Log.v(LOG_TAG, "Finished transforming");
        } catch (FrameGrabber.Exception | FrameFilter.Exception | FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (recorder != null) {
                try {
                    recorder.release();
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
        return rotation == null ? 0 : VideoUtil.roundOrientation(Integer.parseInt(rotation));
    }

    // Create a filter to transform the frame to the desired size. Uses an existing filter if the
    // params match.
    protected FFmpegFrameFilter getFilter(
            HashMap<FilterParams, FFmpegFrameFilter> filterMap,
            FilterParams params, ImageSize maxRecordedSize, ImageSize targetSize)
                    throws FrameFilter.Exception {
        if (params.imageSize.equals(targetSize) && params.rotationDegrees == 0) {
            return null;
        }

        FFmpegFrameFilter filter = filterMap.get(params);
        if (filter != null) {
            return filter;
        }

        ArrayList<String> transforms = new ArrayList<String>();
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
        transforms.add("scale=" + imageSizeBuilder.width + ":" + imageSizeBuilder.height);
        imageSizeBuilder.cropTo(targetSize);
        transforms.add("crop=" + imageSizeBuilder.width + ":" + imageSizeBuilder.height);
        ImageSize padSize = mParams.canPadVideo()
                ? imageSizeBuilder.padTo(targetSize).build()
                : maxRecordedSize;
        transforms.add("pad=" + padSize.width + ":" + padSize.height + ":(ow-iw)/2:(oh-ih)/2");

        filter = new FFmpegFrameFilter(
                TextUtils.join(",", transforms),
                params.imageSize.width,
                params.imageSize.height);
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

    /**
     * Progress listener
     */
    public interface TaskListener {
        void onStart();
        void onProgress(int progress, int total);
        void onDone();
    }
}
