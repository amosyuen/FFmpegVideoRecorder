package com.amosyuen.videorecorder.recorder.params;


import android.support.annotation.CallSuper;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Params for the camera.
 */
public interface CameraParams extends VideoFrameRateParams, VideoSizeParams, Serializable {

    /**
     * Get the video camera facing.
     */
    CameraControllerI.Facing getVideoCameraFacing();

    interface BuilderI<T extends BuilderI<T>>
            extends VideoFrameRateParams.BuilderI<T>, VideoSizeParams.BuilderI<T> {

        /**
         * Set the video camera facing.
         */
        T videoCameraFacing(CameraControllerI.Facing val);

        CameraParams build();
    }

    class Builder implements BuilderI<Builder> {
        protected ImageSize.Builder videoSize = ImageSize.UNDEFINED.toBuilder();
        protected CameraControllerI.Facing videoCameraFacing = CameraControllerI.Facing.BACK;
        protected int videoFrameRate = 30;

        public Builder merge(CameraParams copy) {
            videoSize = copy.getVideoSize().toBuilder();
            videoCameraFacing = copy.getVideoCameraFacing();
            videoFrameRate = copy.getVideoFrameRate();
            return this;
        }

        @Override
        public Builder videoWidth(int val) {
            videoSize.width(val);
            return this;
        }

        @Override
        public Builder videoHeight(int val) {
            videoSize.height(val);
            return this;
        }

        @Override
        public Builder videoSize(int width, int height) {
            videoSize.setSize(width, height);
            return this;
        }

        @Override
        public Builder videoSize(ImageSize imageSize) {
            videoSize.setSize(imageSize);
            return this;
        }

        @Override
        public Builder videoCameraFacing(CameraControllerI.Facing val) {
            videoCameraFacing = val;
            return this;
        }

        @Override
        public Builder videoFrameRate(int val) {
            videoFrameRate = val;
            return this;
        }

        public CameraParams build() {
            return new CameraParamsImpl(this);
        }

        public static class CameraParamsImpl implements CameraParams {
            private final ImageSize videoSize;
            private final CameraControllerI.Facing videoCameraFacing;
            private final int videoFrameRate;

            public CameraParamsImpl(Builder builder) {
                videoSize = builder.videoSize.build();
                videoCameraFacing = builder.videoCameraFacing;
                videoFrameRate = builder.videoFrameRate;
            }

            @Override
            public ImageSize getVideoSize() {
                return videoSize;
            }

            @Override
            public CameraControllerI.Facing getVideoCameraFacing() {
                return videoCameraFacing;
            }

            @Override
            public int getVideoFrameRate() {
                return videoFrameRate;
            }

            @Override
            @CallSuper
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                CameraParamsImpl that = (CameraParamsImpl) o;

                if (videoFrameRate != that.videoFrameRate) return false;
                if (videoSize != null ? !videoSize.equals(that.videoSize) : that.videoSize != null)
                    return false;
                return videoCameraFacing == that.videoCameraFacing;

            }

            @Override
            @CallSuper
            public int hashCode() {
                int result = videoSize != null ? videoSize.hashCode() : 0;
                result = 31 * result + (videoCameraFacing != null ? videoCameraFacing.hashCode() : 0);
                result = 31 * result + videoFrameRate;
                return result;
            }

            @CallSuper
            protected MoreObjects.ToStringHelper toStringHelper() {
                return MoreObjects.toStringHelper(this)
                        .add("videoSize", videoSize)
                        .add("videoCameraFacing", videoCameraFacing)
                        .add("videoFrameRate", videoFrameRate);
            }

            @Override
            public final String toString() {
                return toStringHelper().toString();
            }
        }
    }
}