package com.amosyuen.videorecorder.audio;

import java.nio.ShortBuffer;

/**
 * IRecords raw audio data.
 */
public interface AudioRecorderInterface {

    /** This will be called before any other methods are called.
     */
    void initializeAudioBuffer(int bufferShortSize);

    /** This will be called before each recordAudioSamples(). */
    ShortBuffer getAudioBuffer();

    /** Called with the buffer last returned from getAudioBuffer(). */
    void recordAudioSamples(long audioTimestampNanos, ShortBuffer buffer);

    /** Clear all recorded data. */
    void clear();
}
