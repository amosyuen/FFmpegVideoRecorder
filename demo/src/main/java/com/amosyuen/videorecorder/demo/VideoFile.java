package com.amosyuen.videorecorder.demo;


import java.io.File;
import java.io.Serializable;

/**
 * Wrapper for a video file and its thumbnail file
 */
public class VideoFile implements Comparable<VideoFile>, Serializable {
    public File videoFile;
    public File thumbnailFile;

    public VideoFile(File videoFile, File thumbnailFile) {
        this.videoFile = videoFile;
        this.thumbnailFile = thumbnailFile;
    }

    @Override
    public int compareTo(VideoFile file) {
        return (int)(videoFile.lastModified() - file.videoFile.lastModified());
    }
}
