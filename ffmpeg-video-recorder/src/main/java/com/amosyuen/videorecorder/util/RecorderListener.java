package com.amosyuen.videorecorder.util;


/**
 * Listener for recorders
 */
public interface RecorderListener {
    void onRecorderInitializing(Object recorder);
    void onRecorderReady(Object recorder);
    void onRecorderError(Object recorder, Exception error);
    void onRecorderStart(Object recorder);
    void onRecorderFrameRecorded(Object recorder, long timestampNanos);
    void onRecorderStop(Object recorder);
}
