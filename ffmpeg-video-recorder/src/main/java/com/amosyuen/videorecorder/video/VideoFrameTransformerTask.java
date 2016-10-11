package com.amosyuen.videorecorder.video;


import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amosyuen.videorecorder.util.TaskListener;

import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.FrameRecorder.Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static android.R.attr.x;
import static org.bytedeco.javacv.Frame.DEPTH_UBYTE;

/**
 * Transforms video frames to the desired size and orientation and records it using a
 * FFmpegFrameRecorder. Also creates thumbnail if a path is specified.
 */
public class VideoFrameTransformerTask implements Runnable {

    protected static final String LOG_TAG = "VideoFrameTransformer";

    private static final int IMAGE_DEPTH = DEPTH_UBYTE;
    private static final int IMAGE_CHANNELS = 2;

    protected FFmpegFrameRecorder mFrameRecorder;
    protected File mThumbnailFile;
    protected final VideoFrameTransformerParams mParams;
    protected final boolean mIsLandscape;
    protected final int mRecordedWidth;
    protected final int mRecordedHeight;
    protected Collection<VideoFrameData> mFrameDatas;
    protected TaskListener mProgressListener;

    protected FFmpegFrameFilter filterRotateAndCropFrontCam = null;
    protected FFmpegFrameFilter filterRotateAndCropBackCam = null;
    protected FFmpegFrameFilter filterCropBackCam = null;
    protected FFmpegFrameFilter filterCropFrontCam = null;

    protected boolean mIsFirstImage = true;

    public VideoFrameTransformerTask(
            FFmpegFrameRecorder frameRecorder,
            @Nullable File thumbnailFile,
            VideoFrameTransformerParams params,
            boolean isLandscape,
            int recordedWidth,
            int recordedHeight,
            Collection<VideoFrameData> frameDatas) {
        mFrameRecorder = frameRecorder;
        mThumbnailFile = thumbnailFile;
        mParams = params;
        mIsLandscape = isLandscape;
        mRecordedWidth = recordedWidth;
        mRecordedHeight = recordedHeight;
        mFrameDatas = frameDatas;

        initializeFilters();
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
            mProgressListener.onStart(this);
        }
        try {
            int i = 0;
            int total = mFrameDatas.size();
            Frame yuvFrame =
                    new Frame(mRecordedWidth, mRecordedHeight, IMAGE_DEPTH, IMAGE_CHANNELS);
            Log.v(LOG_TAG, String.format("Start recording %d frames", total));

            for (VideoFrameData frameData : mFrameDatas) {
                byte[] byteData = frameData.getFrameBytesData();
                ((ByteBuffer) yuvFrame.image[0].position(0)).put(byteData);

                mFrameRecorder.setFrameNumber(frameData.getFrameNumber());
                if (frameData.isPortrait()) {
                    if (frameData.isFrontCamera()) {
                        filterImage(filterRotateAndCropFrontCam, yuvFrame);
                    } else {
                        filterImage(filterRotateAndCropBackCam, yuvFrame);
                    }
                } else {
                    if (frameData.isFrontCamera()) {
                        filterImage(filterCropFrontCam, yuvFrame);
                    } else {
                        filterImage(filterCropBackCam, yuvFrame);
                    }
                }
                if (mProgressListener != null) {
                    mProgressListener.onProgress(this, i, total);
                }
                i++;
            }
            Log.v(LOG_TAG, "Finished recording.");
        } finally {
            release();
        }
        if (mProgressListener != null) {
            mProgressListener.onDone(this);
        }
    }

    protected void initializeFilters() {
        String baseTransform = calculateBaseTransform();
        Log.v(LOG_TAG, "Base transform " + baseTransform + "");

        // No additional transforms
        if (baseTransform != null) {
            filterCropBackCam =
                    new FFmpegFrameFilter(baseTransform, mRecordedWidth, mRecordedHeight);
            filterCropBackCam.setPixelFormat(avutil.AV_PIX_FMT_NV21);
        }

        // Rotate 90 degrees clockwise for landscape
        filterRotateAndCropBackCam = new FFmpegFrameFilter(
                "transpose=" + combineTransforms("clock", baseTransform),
                mRecordedWidth, mRecordedHeight);
        filterRotateAndCropBackCam.setPixelFormat(avutil.AV_PIX_FMT_NV21);

        // Flip horizontally for front camera
        filterCropFrontCam = new FFmpegFrameFilter(
                combineTransforms("hflip", baseTransform), mRecordedWidth, mRecordedHeight);
        filterCropFrontCam.setPixelFormat(avutil.AV_PIX_FMT_NV21);

        // Flip horizontally for front camera and rotate 90 degrees counter clockwise for landscape
        filterRotateAndCropFrontCam = new FFmpegFrameFilter(
                "transpose=" + combineTransforms("cclock,hflip", baseTransform),
                mRecordedWidth, mRecordedHeight);
        filterRotateAndCropFrontCam.setPixelFormat(avutil.AV_PIX_FMT_NV21);

        try {
            if (filterCropBackCam != null) {
                filterCropBackCam.start();
            }
            filterRotateAndCropBackCam.start();
            filterCropFrontCam.start();
            filterRotateAndCropFrontCam.start();
        } catch (FrameFilter.Exception e) {
            Log.e(LOG_TAG, "Error starting filters", e);
            throw new RuntimeException(e);
        }
    }

    protected String calculateBaseTransform() {
        ArrayList<String> transforms = new ArrayList<String>();
        ImageSize targetSize =
                new ImageSize(mParams.getVideoWidth(), mParams.getVideoHeight());
        if (targetSize.isAtLeastOneDimensionDefined()) {
            //noinspection SuspiciousNameCombination
            ImageSize imageSize = mIsLandscape
                    ? new ImageSize(mRecordedWidth, mRecordedHeight)
                    : new ImageSize(mRecordedHeight, mRecordedWidth);
            targetSize.calculateUndefinedDimensions(imageSize);
            if (imageSize.scale(
                    targetSize, mParams.getVideoScaleType(), mParams.canUpscaleVideo())) {
                transforms.add("scale=w=" + imageSize.width + ":h=" + imageSize.height);
            }
            if (imageSize.cropTo(targetSize)) {
                transforms.add("crop=w=" + imageSize.width + ":h=" + imageSize.height);
            }
            if (imageSize.padTo(targetSize)) {
                transforms.add("pad=w=" + imageSize.width + ":h=" + imageSize.height);
            }
            mFrameRecorder.setImageWidth(imageSize.width);
            mFrameRecorder.setImageHeight(imageSize.height);
        } else {
            // These are flipped because we are recording in portrait
            if (mIsLandscape) {
                mFrameRecorder.setImageWidth(mRecordedWidth);
                mFrameRecorder.setImageHeight(mRecordedHeight);
            } else {
                //noinspection SuspiciousNameCombination
                mFrameRecorder.setImageWidth(mRecordedHeight);
                //noinspection SuspiciousNameCombination
                mFrameRecorder.setImageHeight(mRecordedWidth);
            }
        }

        return combineTransforms(transforms.toArray(new String[transforms.size()]));
    }

    protected String combineTransforms(String... transforms) {
        String combinedTransform = null;
        for (int i = 0; i < transforms.length; i++) {
            String transform = transforms[i];
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

    protected void filterImage(@Nullable FFmpegFrameFilter filter, Frame yuvFrame) {
        if (filter == null) {
            recordFrame(yuvFrame);
            return;
        }

        try {
            filter.push(yuvFrame);
            while ((yuvFrame = filter.pull()) != null) {
                recordFrame(yuvFrame);
            }
        } catch (FrameFilter.Exception e) {
            Log.e(LOG_TAG, "Error filtering frame", e);
            throw new RuntimeException(e);
        }
    }

    protected void recordFrame(Frame yuvFrame) {
        try {
            mFrameRecorder.record(yuvFrame);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error recording frame", e);
            throw new RuntimeException(e);
        }

        if (mIsFirstImage) {
            mIsFirstImage = false;
            if (mThumbnailFile != null) {
                saveThumbnail(yuvFrame);
            }
        }
    }

    protected void saveThumbnail(Frame yuvFrame) {
        YuvImage localYuvImage = new YuvImage(
                ((ByteBuffer) yuvFrame.image[0]).array(),
                ImageFormat.NV21,
                yuvFrame.imageWidth,
                yuvFrame.imageHeight, null);

        try {
            // Convert image to JPEG
            FileOutputStream outStream = new FileOutputStream(mThumbnailFile);
            localYuvImage.compressToJpeg(
                    new Rect(0, 0, yuvFrame.imageWidth, yuvFrame.imageHeight), 100, outStream);
            outStream.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error saving thumbnail", e);
            throw new RuntimeException(e);
        }
    }

    protected void release() {
        mFrameRecorder = null;
        mFrameDatas = null;
        try {
            if (filterRotateAndCropFrontCam != null) {
                filterRotateAndCropFrontCam.stop();
                filterRotateAndCropFrontCam.release();
                filterRotateAndCropFrontCam = null;
            }
            if (filterRotateAndCropBackCam != null) {
                filterRotateAndCropBackCam.stop();
                filterRotateAndCropBackCam.release();
                filterRotateAndCropBackCam = null;
            }
            if (filterCropBackCam != null) {
                filterCropBackCam.stop();
                filterCropBackCam.release();
                filterCropBackCam = null;
            }
            if (filterCropFrontCam != null) {
                filterCropFrontCam.stop();
                filterCropFrontCam.release();
                filterCropFrontCam = null;
            }
        } catch (FrameFilter.Exception e) {
            Log.e(LOG_TAG, "Error releasing resources", e);
        }
    }
}
