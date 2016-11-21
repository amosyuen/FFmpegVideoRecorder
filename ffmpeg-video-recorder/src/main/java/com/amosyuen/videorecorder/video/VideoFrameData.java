package com.amosyuen.videorecorder.video;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class encapsulating data about a video frame captured.
 */
public class VideoFrameData implements Parcelable {

    private final byte[] mFrameBytesData;
    private final int mFrameNumber;
    private final boolean mIsPortrait;
    private final boolean mIsFrontCamera;

    public VideoFrameData(byte[] frameBytesData, int frameNumber,
            boolean isLandscape, boolean isFrontCamera) {
        mFrameBytesData = frameBytesData;
        mFrameNumber = frameNumber;
        mIsPortrait = isLandscape;
        mIsFrontCamera = isFrontCamera;
    }

    protected VideoFrameData(Parcel in) {
        mFrameBytesData = in.createByteArray();
        mFrameNumber = in.readInt();
        boolean bools[] = new boolean[2];
        in.readBooleanArray(bools);
        mIsPortrait = bools[0];
        mIsFrontCamera = bools[1];
    }

    public byte[] getFrameBytesData() {
        return mFrameBytesData;
    }

    public int getFrameNumber() {
        return mFrameNumber;
    }

    public boolean isPortrait() {
        return mIsPortrait;
    }

    public boolean isFrontCamera() {
        return mIsFrontCamera;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(mFrameBytesData);
        dest.writeInt(mFrameNumber);
        dest.writeBooleanArray(new boolean[] { mIsPortrait, mIsFrontCamera });
    }

    public static final Creator<VideoFrameData> CREATOR = new Creator<VideoFrameData>() {
        @Override
        public VideoFrameData createFromParcel(Parcel in) {
            return new VideoFrameData(in);
        }

        @Override
        public VideoFrameData[] newArray(int size) {
            return new VideoFrameData[size];
        }
    };
}
