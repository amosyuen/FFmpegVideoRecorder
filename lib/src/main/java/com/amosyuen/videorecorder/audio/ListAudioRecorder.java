package com.amosyuen.videorecorder.audio;


import java.nio.ShortBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Video frame recorder that stores the raw audio in a list.
 */
public class ListAudioRecorder implements AudioRecorderInterface {

    protected final List<ShortBuffer> mRecordedAudio;
    protected int mBufferShortSize;
    protected ShortBuffer mAudioBuffer;

    public ListAudioRecorder() {
        mRecordedAudio = new LinkedList<>();
    }

    public synchronized List<ShortBuffer> getRecordedSamples() {
        return mRecordedAudio;
    }

    @Override
    public void initializeAudioBuffer(int bufferShortSize) {
        mBufferShortSize = bufferShortSize;
        mAudioBuffer = ShortBuffer.allocate(bufferShortSize);
    }

    @Override
    public ShortBuffer getAudioBuffer() {
        return mAudioBuffer;
    }

    @Override
    public synchronized void recordAudioSamples(long audioTimestampNanos, ShortBuffer buffer) {
        mRecordedAudio.add(buffer);
        mAudioBuffer = ShortBuffer.allocate(mBufferShortSize);
    }

    @Override
    public void clear() {
        mRecordedAudio.clear();
    }
}
