package com.amosyuen.videorecorder.util;

/**
 * Progress listener
 */
public interface TaskListener {
    void onStart(Object task);
    void onProgress(Object task, int progress, int total);
    void onDone(Object task);
}
