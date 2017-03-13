package com.amosyuen.videorecorder.recorder;

import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Media recorder for recording multiple clips
 */
public class MediaClipsRecorder implements
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {

    protected static final String LOG_TAG = "MediaClipsRecorder";

    // Params
    protected MediaRecorderConfigurer mMediaRecorderConfigurer;
    protected File mTempDirectory;
    protected MediaClipsRecorderListener mListener;

    // State
    protected MediaRecorder mMediaRecorder;
    protected List<Clip> mClips;
    protected File mCurrentFile;
    protected long mStartTimeMillis;

    public MediaClipsRecorder(
            @NonNull MediaRecorderConfigurer mediaRecorderConfigurer,
            @NonNull File tempDirectory) {
        mMediaRecorderConfigurer = Preconditions.checkNotNull(mediaRecorderConfigurer);
        mTempDirectory = Preconditions.checkNotNull(tempDirectory);
        mMediaRecorder = new MediaRecorder();
        mClips = new ArrayList<>();
        newTempFile();
    }

    public void setMediaCLipstRecorderListener(MediaClipsRecorderListener listener) {
        mListener = listener;
    }

    protected void newTempFile() {
        try {
            mCurrentFile = File.createTempFile("video", "mp4", mTempDirectory);
            mCurrentFile.deleteOnExit();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error creating temp file", e);
            if (mListener != null) {
                mListener.onMediaRecorderError(e);
            }
        }
    }

    public List<Clip> getClips() {
        return mClips;
    }

    public boolean isRecording() {
        return mStartTimeMillis > 0;
    }

    public long getCurrentRecordedTimeMillis() {
        return isRecording() ? System.currentTimeMillis() - mStartTimeMillis : 0;
    }

    public long getRecordedMillis() {
        long recordedTimeMillis = getCurrentRecordedTimeMillis();
        for (Clip clip : mClips) {
            recordedTimeMillis += clip.durationMillis;
        }
        return recordedTimeMillis;
    }

    public long getCurrentRecordedBytes() {
        return isRecording() ? mCurrentFile.getTotalSpace() : 0;
    }

    public long getRecordedBytes() {
        long recordedBytes = getCurrentRecordedBytes();
        for (Clip clip : mClips) {
            recordedBytes += clip.bytes;
        }
        return recordedBytes;
    }

    public void start() {
        if (mMediaRecorder == null) {
            return;
        }
        Log.v(LOG_TAG, "Preparing recorder");
        if (mCurrentFile == null) {
            newTempFile();
        }
        try {
            mMediaRecorderConfigurer.configureMediaRecorder(mMediaRecorder);
            mMediaRecorder.setOutputFile(mCurrentFile.getAbsolutePath());
            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            if (mMediaRecorder != null) {
                release();
            }
            Log.e(LOG_TAG, "Error preparing recorder", e);
            if (mListener != null) {
                mListener.onMediaRecorderError(e);
            }
            return;
        }

        try {
            mMediaRecorder.start();
            mStartTimeMillis = System.currentTimeMillis();
        } catch (Exception e) {
            if (mMediaRecorder != null) {
                release();
            }
            Log.e(LOG_TAG, "Error starting recorder", e);
            if (mListener != null) {
                mListener.onMediaRecorderError(e);
            }
        }
    }

    public void stop() {
        if (!isRecording()) {
            return;
        }
        try {
            mMediaRecorder.stop();
            long duration = System.currentTimeMillis() - mStartTimeMillis;
            mClips.add(new Clip(mCurrentFile, duration, mCurrentFile.getTotalSpace()));
        } catch (RuntimeException e) {
            // RuntimeException is thrown when stop() is called immediately after start().
            // In this case the output file is not properly constructed ans should be deleted.
            mCurrentFile.delete();
        }
        mMediaRecorder.reset();
        mCurrentFile = null;
        mStartTimeMillis = 0;
    }

    public void release() {
        if (mMediaRecorder == null) {
            return;
        }
        stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    public void deleteClips() {
        for (Clip clip : mClips) {
            clip.file.delete();
        }
        mClips.clear();
    }

    /**
     * Remove the last recorded clip. Only works when not recording.
     */
    public void removeLastClip() {
        if (!isRecording() || mClips.isEmpty()) {
            return;
        }
        mClips.remove(mClips.size() - 1);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        final Exception error;
        switch (what) {
            case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
                error = new RuntimeException("Media recorder server died.");
                break;
            case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
            default:
                error = new RuntimeException("Unknown media recorder error.");
                break;
        }
        if (mListener != null) {
            mListener.onMediaRecorderError(error);
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                if (mListener != null) {
                    mListener.onMediaRecorderMaxDurationReached();
                }
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                if (mListener != null) {
                    mListener.onMediaRecorderMaxFileSizeReached();
                }
                break;
        }
    }

    public static class Clip {
        public final File file;
        public final long durationMillis;
        public final long bytes;

        public Clip(File file, long durationMillis, long bytes) {
            this.file = file;
            this.durationMillis = durationMillis;
            this.bytes = bytes;
        }
    }

    public interface MediaRecorderConfigurer {
        void configureMediaRecorder(MediaRecorder recorder);
    }

    public interface MediaClipsRecorderListener {
        void onMediaRecorderMaxDurationReached();
        void onMediaRecorderMaxFileSizeReached();
        void onMediaRecorderError(Exception e);
    }
}
