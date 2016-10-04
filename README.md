# FFmpegVideoRecorder - Video Recording Library for Android

This library is based off of these sources:
- https://github.com/sourab-sharma/TouchToRecord
- https://github.com/bytedeco/sample-projects/tree/master/JavaCV-android-example

It uses https://github.com/bytedeco/javacv for combining and encoding videos.

On a high level, the library provides a way to record audio and video separately and then merge them together using FFmpeg Recorder from JavaCV. It allows customization of the video encoding and recording, but also exposes some basic components that can be used to customize your own activity.

# Usage

*For a working implementation, please have a look at the demo project*

1. Include the library as local library project.

    ``` compile 'com.amosyuen:ffmpegvideorecorder:1.0.0' ```
    
2. For Android M and above, make sure you request the following permissions before starting the activity:

    - Manifest.permission.CAMERA
    - Manifest.permission.RECORD_AUDIO
    - Manifest.permission.WAKE_LOCK
    - Manifest.permission.WRITE_EXTERNAL_STORAGE

3. Send an intent to start the activity with the desired parameters. The parameters are required:

	```java
	private static final int RECORD_VIDEO_REQUEST = 1000;

	Intent intent = new Intent(this, FFmpegRecorderActivity.class);
	RecorderActivityParams params = new RecorderActivityParams.Builder(videoFile)
        .videoThumbnailOutput(thumbnailFile)
        .videoWidth(640)
        .videoHeight(480)
        .build();
    intent.putExtra(FFmpegRecorderActivity.RECORDER_ACTIVITY_PARAMS_KEY, params);
    startActivityForResult(intent, RECORD_VIDEO_REQUEST);
    ```

4. Override `onActivityResult` method and handle request code sent in step 3.

    ```java
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_VIDEO_REQUEST) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri videoUri = data.getData();
                    Uri thumbnailUri =
                            data.getParcelableExtra(FFmpegRecorderActivity.THUMBNAIL_URI_KEY);
                    break;
                case Activity.RESULT_CANCELED:
                    break;
                case FFmpegRecorderActivity.RESULT_ERROR:
                    Exception error = (Exception)
                            data.getSerializableExtra(FFmpegRecorderActivity.ERROR_PATH_KEY);
                    break;
            }
        }
    }
    ```

# Features

- Enable / disable flash
- Switch between front and back cameras
- Instagram / Snapchat like recording by holding down a button
- Able to record multiple clips and combine them into one video
- Can set the minimum and maximum recording time
- Can set the desired width and height dimensions
- Can set how the camera capture images are scaled to the desired width and height
- Camera image is scaled for the desired aspect ratio
- Can set attributes such as bitrate, codec, and quality
- Generates a thumbnail image for the video
- After recording the video, the user is able to preview the video before using it
