package com.amosyuen.videorecorder.video;

/**
 * Records raw video frame data.
 */
public interface VideoFrameRecorderInterface {

    /** This will be called before any other methods are called. */
    void initializeFrameBuffer(int frameSizeBytes);

    /** This will be called before each recordFrame(). */
    byte[] getFrameBuffer();

    /** Called with the buffer last returned from getFrameBuffer()  */
    void recordFrame(VideoFrameData frameData);

    /** Clear all recorded data. */
    void clear();
}
