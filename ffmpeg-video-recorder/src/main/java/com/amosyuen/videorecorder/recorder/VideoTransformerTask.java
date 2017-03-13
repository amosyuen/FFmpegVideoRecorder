package com.amosyuen.videorecorder.recorder;


import android.util.Log;

import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.params.EncoderParams;
import com.amosyuen.videorecorder.util.Util;
import com.google.common.collect.Lists;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
            int i = 0;
            int totalFrames = 0;
            List<FrameGrabber> frameGrabbers =
                    Lists.newArrayListWithCapacity(mFilesToTransform.size());
            ImageSize.Builder targetSizeBuilder = mParams.getVideoSize().toBuilder();
            for (File file : mFilesToTransform) {
                FrameGrabber frameGrabber = new FFmpegFrameGrabber(file);
                frameGrabber.start();
                totalFrames += frameGrabber.getLengthInFrames();
                frameGrabbers.add(frameGrabber);

                if (!targetSizeBuilder.areDimensionsDefined()) {
                    ImageSize recordedSize =
                            new ImageSize(frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
                    ImageSize targetSize = mParams.getVideoSize().toBuilder()
                            .calculateUndefinedDimensions(recordedSize)
                            .build();
                    targetSizeBuilder.max(targetSize);
                }
            }
            ImageSize targetSize = targetSizeBuilder.build();
            Log.v(LOG_TAG, String.format(
                    "Start transforming %d files with %d frames to target size %s",
                    mFilesToTransform.size(), totalFrames, targetSize));

            recorder = Util.createFrameRecorder(mVideoOutputFile, mParams);
            recorder.setImageWidth(targetSize.width);
            recorder.setImageHeight(targetSize.height);
            recorder.start();

            for (FrameGrabber frameGrabber : frameGrabbers) {
                // Create a filter to transform image size into desired size if needed
                ImageSize recordedSize =
                        new ImageSize(frameGrabber.getImageWidth(), frameGrabber.getImageHeight());
                FFmpegFrameFilter filter = recordedSize.equals(targetSize)
                        ? null
                        : getFilter(filterMap, recordedSize, targetSize, frameGrabber);

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
                    i++;
                    if (mProgressListener != null) {
                        mProgressListener.onProgress(i, totalFrames);
                    }
                }
                frameGrabber.stop();
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

    // Create a filter to transform the frame to the desired size. Uses an existing filter if the
    // params match.
    protected FFmpegFrameFilter getFilter(
            HashMap<FilterParams, FFmpegFrameFilter> filterMap, ImageSize recordedSize,
            ImageSize calculatedTargetSize, FrameGrabber frameGrabber)
            throws FrameFilter.Exception {
        FilterParams params = new FilterParams(recordedSize, frameGrabber.getPixelFormat());
        FFmpegFrameFilter filter = filterMap.get(params);
        if (filter != null) {
            return filter;
        }

        ArrayList<String> transforms = new ArrayList<String>();
        ImageSize.Builder imageSizeBuilder = recordedSize.toBuilder().scale(
                calculatedTargetSize, mParams.getVideoScaleType(), mParams.canUpscaleVideo());
        transforms.add("scale=" + imageSizeBuilder.width + ":" + imageSizeBuilder.height);
        imageSizeBuilder.cropTo(calculatedTargetSize);
        transforms.add("crop=" + imageSizeBuilder.width + ":" + imageSizeBuilder.height);
        if (mParams.canPadVideo()) {
            imageSizeBuilder.padTo(calculatedTargetSize);
            transforms.add(
                    "pad=" + imageSizeBuilder.width + ":" + imageSizeBuilder.height + ":(ow-iw)/2:(oh-ih)/2");

        }

        filter = new FFmpegFrameFilter(
                combineTransforms(transforms),
                recordedSize.width,
                recordedSize.height);
        filter.setPixelFormat(frameGrabber.getPixelFormat());
        filter.start();
        filterMap.put(params, filter);
        return filter;
    }

    protected static String combineTransforms(ArrayList<String> transforms) {
        String combinedTransform = null;
        for (String transform : transforms) {
            if (transform != null && !transform.isEmpty()) {
                if (combinedTransform == null) {
                    combinedTransform = transform;
                } else {
                    combinedTransform += "," + transform;
                }
            }
        }
        return combinedTransform;
    }

    public class FilterParams {
        public ImageSize mImageSize;
        public int mPixelFormat;

        public FilterParams(ImageSize imageSize, int pixelFormat) {
            mImageSize = imageSize;
            mPixelFormat = pixelFormat;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilterParams that = (FilterParams) o;

            if (mPixelFormat != that.mPixelFormat) return false;
            return mImageSize != null
                    ? mImageSize.equals(that.mImageSize)
                    : that.mImageSize ==  null;

        }

        @Override
        public int hashCode() {
            int result = mImageSize != null ? mImageSize.hashCode() : 0;
            result = 31 * result + mPixelFormat;
            return result;
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
