package com.amosyuen.videorecorder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.amosyuen.videorecorder.util.RecorderListener;

import java.nio.ShortBuffer;
import java.security.InvalidParameterException;
import java.util.Locale;

/**
 * Thread that records audio.
 */
public class AudioRecorderThread extends Thread {

    protected static final String LOG_TAG = "AudioRecorderThread";

    // User params
    protected final AudioRecorderParams mParams;
    protected AudioRecorderInterface mAudioRecorder;
    protected RecorderListener mRecorderListener;

    // Recording state
    protected volatile boolean mIsRunning = true;
    protected volatile boolean mIsRecording;
    /** Recording length in nanoseconds */
    protected volatile long mRecordingLengthNanos;
    /** System time in nanoseconds when the recording was last updated */
    protected volatile long mLastUpdateTimeNanos;

    public AudioRecorderThread(AudioRecorderParams params, AudioRecorderInterface audioRecorder) {
        mParams = params;
        mAudioRecorder = audioRecorder;
    }

    public AudioRecorderParams getParams() {
        return mParams;
    }

    public AudioRecorderInterface getAudioRecorder() {
        return mAudioRecorder;
    }

    public RecorderListener getRecorderListener() {
        return mRecorderListener;
    }

    public void setRecorderListener(RecorderListener recorderListener) {
        mRecorderListener = recorderListener;
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public void stopRunning() {
        mIsRunning = false;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setRecording(boolean isRecording) {
        if (mIsRecording == isRecording) {
            return;
        }
        this.mIsRecording = isRecording;
        if (isRecording) {
            Log.v(LOG_TAG, "Start recording");
            if (mRecorderListener != null) {
                mRecorderListener.onRecorderStart(this);
            }
        } else {
            Log.v(LOG_TAG, "Stop recording");
            if (mRecorderListener != null) {
                mRecorderListener.onRecorderStop(this);
            }
        }
    }

    public long getRecordingLengthNanos() {
        return mRecordingLengthNanos;
    }

    public long getLastUpdateTimeNanos() {
        return mLastUpdateTimeNanos;
    }

    @Override
    @CallSuper
    public void run() {
        AudioRecord audioRecord = null;
        try {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            if (mRecorderListener != null) {
                mRecorderListener.onRecorderInitializing(this);
            }

            int channelConfig = getChannelConfigForChannelCount(mParams.getAudioChannelCount());
            int bufferBytes = AudioRecord.getMinBufferSize(mParams.getAudioSamplingRateHz(),
                    channelConfig, AudioFormat.ENCODING_PCM_16BIT);
            int bufferSizeShorts = bufferBytes / 2;
            mAudioRecorder.initializeAudioBuffer(bufferSizeShorts);
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, mParams.getAudioSamplingRateHz(), channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT, bufferBytes);

            Log.d(LOG_TAG, "Start AudioRecord");
            audioRecord.startRecording();
            if (mRecorderListener != null) {
                mRecorderListener.onRecorderReady(this);
            }

            ShortBuffer buffer = mAudioRecorder.getAudioBuffer();
            long samplesRecorded = 0;
            while (mIsRunning) {
                int readShorts = audioRecord.read(buffer.array(), 0, buffer.capacity());
                if (mIsRecording && readShorts > 0) {
                    // Update the timestamp based on samples recorded and sample rate
                    samplesRecorded += readShorts / mParams.getAudioChannelCount();
                    mRecordingLengthNanos = 1000000L * samplesRecorded / mParams.getAudioSamplingRateHz();
                    mLastUpdateTimeNanos = System.nanoTime();

                    buffer.position(0).limit(readShorts);
                    mAudioRecorder.recordAudioSamples(mRecordingLengthNanos, buffer);
                    buffer = mAudioRecorder.getAudioBuffer();

                    if (mRecorderListener != null) {
                        mRecorderListener.onRecorderFrameRecorded(this, mRecordingLengthNanos);
                    }
                }
            }

            audioRecord.stop();
        } catch (Exception e) {
            if (mRecorderListener != null) {
                mRecorderListener.onRecorderError(this, e);
            }
        } finally {
            if (audioRecord != null) {
                audioRecord.release();
            }
            mAudioRecorder = null;
            Log.d(LOG_TAG, "Stop AudioRecord");
        }
    }

    public static int getChannelConfigForChannelCount(int channelCount) {
        switch (channelCount) {
            case 1:
                return AudioFormat.CHANNEL_IN_MONO;
            case 2:
                return AudioFormat.CHANNEL_IN_STEREO;
            default:
                throw new InvalidParameterException(
                        String.format(Locale.US, "Invalid number of channels %d", channelCount));
        }
    }
}
