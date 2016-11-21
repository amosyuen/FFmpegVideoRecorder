package com.amosyuen.videorecorder.audio;

import android.util.Log;

import com.amosyuen.videorecorder.util.FFmpegFrameRecorder;
import com.amosyuen.videorecorder.util.TaskListener;

import org.bytedeco.javacv.FrameRecorder;

import java.nio.ShortBuffer;
import java.util.Collection;

/**
 * Encodes a list of audio samples using a FFmpegFrameRecorder.
 */
public class AudioTransformerTask implements Runnable {

    protected static final String LOG_TAG = "AudioTransformerTask";

    protected FFmpegFrameRecorder mFrameRecorder;
    protected Collection<ShortBuffer> mSamples;
    protected TaskListener mProgressListener;

    public AudioTransformerTask(
            FFmpegFrameRecorder frameRecorder, Collection<ShortBuffer> samples) {
        mFrameRecorder = frameRecorder;
        mSamples = samples;
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
        int i = 0;
        int total = mSamples.size();
        Log.v(LOG_TAG, String.format("Start recording %d samples", total));
        for (ShortBuffer sample : mSamples) {
            try {
                mFrameRecorder.recordSamples(sample);
            } catch (FrameRecorder.Exception e) {
                Log.e(LOG_TAG, "Error recording samples", e);
                throw new RuntimeException(e);
            }
            if (mProgressListener != null) {
                mProgressListener.onProgress(this, ++i, total);
            }
        }
        Log.v(LOG_TAG, "Finished recording");
        if (mProgressListener != null) {
            mProgressListener.onDone(this);
        }
    }
}
