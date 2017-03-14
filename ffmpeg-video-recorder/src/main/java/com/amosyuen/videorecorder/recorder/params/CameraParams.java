package com.amosyuen.videorecorder.recorder.params;


import android.support.annotation.CallSuper;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageFit;
import com.amosyuen.videorecorder.recorder.common.ImageScale;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Params for the camera.
 */
public interface CameraParams
        extends VideoFrameRateParams, VideoScaleParams, VideoSizeParams, Serializable {

    /**
     * Get the video camera facing.
     */
    CameraControllerI.Facing getVideoCameraFacing();

    interface BuilderI<T extends BuilderI<T>> extends
            VideoFrameRateParams.BuilderI<T>,
            VideoScaleParams.BuilderI<T>,
            VideoSizeParams.BuilderI<T> {

        /**
         * Set the video camera facing.
         */
        T videoCameraFacing(CameraControllerI.Facing val);

        CameraParams build();
    }

    class Builder implements BuilderI<Builder> {
        protected ImageSize.Builder videoSize = ImageSize.UNDEFINED.toBuilder();
        protected ImageFit mVideoImageFit = ImageFit.FILL;
        protected ImageScale mVideoImageScale = ImageScale.DOWNSCALE;
        protected CameraControllerI.Facing videoCameraFacing = CameraControllerI.Facing.BACK;
        protected int videoFrameRate = 30;

        public Builder merge(CameraParams copy) {
            videoSize = copy.getVideoSize().toBuilder();
            mVideoImageFit = copy.getVideoImageFit();
            mVideoImageScale = copy.getVideoImageScale();
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
        public Builder videoScaleFit(ImageFit val) {
            mVideoImageFit = val;
            return this;
        }

        @Override
        public Builder videoScaleDirection(ImageScale val) {
            mVideoImageScale = val;
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

        @Override
        @CallSuper
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Builder builder = (Builder) o;

            if (mVideoImageScale != builder.mVideoImageScale) return false;
            if (videoFrameRate != builder.videoFrameRate) return false;
            if (videoSize != null ? !videoSize.equals(builder.videoSize) : builder.videoSize !=
                    null)
                return false;
            if (mVideoImageFit != builder.mVideoImageFit) return false;
            return videoCameraFacing == builder.videoCameraFacing;

        }

        @Override
        @CallSuper
        public int hashCode() {
            int result = videoSize != null ? videoSize.hashCode() : 0;
            result = 31 * result + (mVideoImageFit != null ? mVideoImageFit.hashCode() : 0);
            result = 31 * result + (mVideoImageScale != null ? mVideoImageScale.hashCode() : 0);
            result = 31 * result + (videoCameraFacing != null ? videoCameraFacing.hashCode() : 0);
            result = 31 * result + videoFrameRate;
            return result;
        }

        @CallSuper
        protected MoreObjects.ToStringHelper toStringHelper() {
            return MoreObjects.toStringHelper(this)
                    .add("videoSize", videoSize)
                    .add("mVideoImageFit", mVideoImageFit)
                    .add("getVideoImageScale", mVideoImageScale)
                    .add("videoCameraFacing", videoCameraFacing)
                    .add("videoFrameRate", videoFrameRate);
        }

        @Override
        public final String toString() {
            return toStringHelper().toString();
        }

        public static class CameraParamsImpl implements CameraParams {
            private final ImageSize videoSize;
            private final ImageFit mVideoImageFit;
            private final ImageScale mVideoImageScale;
            private final CameraControllerI.Facing videoCameraFacing;
            private final int videoFrameRate;

            public CameraParamsImpl(Builder builder) {
                videoSize = builder.videoSize.build();
                mVideoImageFit = builder.mVideoImageFit;
                mVideoImageScale = builder.mVideoImageScale;
                videoCameraFacing = builder.videoCameraFacing;
                videoFrameRate = builder.videoFrameRate;
            }

            @Override
            public ImageSize getVideoSize() {
                return videoSize;
            }

            public ImageFit getVideoImageFit() {
                return mVideoImageFit;
            }

            public ImageScale getVideoImageScale() {
                return mVideoImageScale;
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

                Builder builder = (Builder) o;

                if (mVideoImageScale != builder.mVideoImageScale) return false;
                if (videoFrameRate != builder.videoFrameRate) return false;
                if (videoSize != null ? !videoSize.equals(builder.videoSize) : builder.videoSize !=
                        null)
                    return false;
                if (mVideoImageFit != builder.mVideoImageFit) return false;
                return videoCameraFacing == builder.videoCameraFacing;

            }

            @Override
            @CallSuper
            public int hashCode() {
                int result = videoSize != null ? videoSize.hashCode() : 0;
                result = 31 * result + (mVideoImageFit != null ? mVideoImageFit.hashCode() : 0);
                result = 31 * result + (mVideoImageScale != null ? mVideoImageScale.hashCode() : 0);
                result = 31 * result + (videoCameraFacing != null ? videoCameraFacing.hashCode() : 0);
                result = 31 * result + videoFrameRate;
                return result;
            }

            @CallSuper
            protected MoreObjects.ToStringHelper toStringHelper() {
                return MoreObjects.toStringHelper(this)
                        .add("videoSize", videoSize)
                        .add("mVideoImageFit", mVideoImageFit)
                        .add("getVideoImageScale", mVideoImageScale)
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