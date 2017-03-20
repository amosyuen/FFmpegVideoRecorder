package com.amosyuen.videorecorder.demo;


import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.io.Serializable;

/**
 * Wrapper for a video file and its thumbnail file
 */
@AutoValue
public abstract class VideoFile implements Comparable<VideoFile>, Serializable {

    public abstract File getVideoFile();
    public abstract File getThumbnailFile();

    public void delete() {
        getVideoFile().delete();
        getThumbnailFile().delete();
    }

    @Override
    public int compareTo(@NonNull VideoFile file) {
        return (int)(file.getVideoFile().lastModified() - getVideoFile().lastModified());
    }

    public static VideoFile create(File videoFile, File thumbnailFile) {
        return new AutoValue_VideoFile(videoFile, thumbnailFile);
    }
}
