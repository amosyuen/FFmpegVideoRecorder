package com.amosyuen.videorecorder.video;


import java.util.LinkedList;
import java.util.List;

/**
 * Video frame recorder that stores the raw frames in a list.
 */
public class ListVideoFrameRecorder implements VideoFrameRecorderInterface {

    protected List<VideoFrameData> mRecordedFrames;
    protected byte[] mFrameBuffer;
    protected int mFrameSizeBytes;

    public ListVideoFrameRecorder() {
        mRecordedFrames = new LinkedList<>();
    }

    public synchronized List<VideoFrameData> getRecordedFrames() {
        return mRecordedFrames;
    }

    @Override
    public void initializeFrameBuffer(int frameSizeBytes) {
        mFrameSizeBytes = frameSizeBytes;
        mFrameBuffer = new byte[mFrameSizeBytes];
    }

    @Override
    public byte[] getFrameBuffer() {
        return mFrameBuffer;
    }

    @Override
    public synchronized void recordFrame(VideoFrameData frameData) {
        mRecordedFrames.add(frameData);
        mFrameBuffer = new byte[mFrameSizeBytes];
    }

    @Override
    public void clear() {
        mRecordedFrames.clear();
    }
}