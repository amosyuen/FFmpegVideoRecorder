package com.amosyuen.videorecorder.recorder.params;


import android.net.Uri;
import android.support.annotation.CallSuper;

import com.amosyuen.videorecorder.camera.CameraControllerI;
import com.amosyuen.videorecorder.recorder.common.ImageSize;
import com.amosyuen.videorecorder.recorder.common.ScaleType;
import com.google.common.base.MoreObjects;

import java.io.File;
import java.io.Serializable;

import static org.bytedeco.javacpp.opencv_core.add;

/**
 * Parameters for recording video.
 */
public interface RecorderParams extends
        CameraParams,
        EncoderParams,
        VideoSizeParams,
        VideoTransformerParams,
        Serializable {

    /**
     * Get min recording length in milliseconds.
     */
    long getMinRecordingMillis();

    /**
     * Get max recording length in milliseconds. A value of 0 allows infinite length.
     */
    long getMaxRecordingMillis();

    /**
     * Get max recording file size in bytes. A value of 0 allows infinite size.
     */
    long getMaxFileSizeBytes();

    /**
     * Get URI of the file to save the video.
     */
    String getVideoOutputUri();

    /**
     * Get URI of the file to save the thumbnail. If no value is set, no thumbnail will be saved.
     */
    String getVideoThumbnailOutputUri();

    abstract class AbstractBuilder<T extends AbstractBuilder<T>> implements
            CameraParams.BuilderI<T>,
            EncoderParams.BuilderI<T>,
            VideoSizeParams.BuilderI<T>,
            VideoTransformerParams.BuilderI<T> {
        protected ImageSize.Builder videoSize = ImageSize.UNDEFINED.toBuilder();
        protected CameraControllerI.Facing videoCameraFacing = CameraControllerI.Facing.BACK;
        protected ScaleType videoScaleType = ScaleType.FILL;
        protected boolean canUpscaleVideo = false;
        protected boolean canPadVideo = true;
        protected int videoBitrate = 1000000;
        protected int videoCodec = VideoCodec.H264.ffmpegCodecValue;
        protected int videoFrameRate = 30;

        protected int audioBitrate = 128000;
        protected int audioChannelCount = 1;
        protected int audioCodec = AudioCodec.AAC.ffmpegCodecValue;
        protected int audioSamplingRateHz = 44100;

        protected long minRecordingMillis = 0;
        protected long maxRecordingMillis = 0;
        protected long maxFileSizeBytes = 0;

        protected String videoOutputFormat = "mp4";
        protected String videoOutputUri;
        protected String videoThumbnailOutputUri;

        public AbstractBuilder(File videoOutputFile) {
            this(Uri.fromFile(videoOutputFile));
        }

        public AbstractBuilder(Uri videoOutputUri) {
            this(videoOutputUri.toString());
        }

        public AbstractBuilder(String videoOutputUri) {
            this.videoOutputUri = videoOutputUri;
        }

        public T merge(RecorderParams copy) {
            videoSize = copy.getVideoSize().toBuilder();
            videoCameraFacing = copy.getVideoCameraFacing();
            videoScaleType = copy.getVideoScaleType();
            canUpscaleVideo = copy.canUpscaleVideo();
            canPadVideo = copy.canPadVideo();
            videoBitrate = copy.getVideoBitrate();
            videoCodec = copy.getVideoCodec();
            videoFrameRate = copy.getVideoFrameRate();

            audioBitrate = copy.getAudioBitrate();
            audioChannelCount = copy.getAudioChannelCount();
            audioCodec = copy.getAudioCodec();
            audioSamplingRateHz = copy.getAudioSamplingRateHz();

            minRecordingMillis = copy.getMinRecordingMillis();
            maxRecordingMillis = copy.getMaxRecordingMillis();
            maxFileSizeBytes = copy.getMaxFileSizeBytes();

            videoOutputFormat = copy.getVideoOutputFormat();
            videoOutputUri = copy.getVideoOutputUri();
            videoThumbnailOutputUri = copy.getVideoThumbnailOutputUri();
            return (T) this;
        }

        @Override
        public T videoWidth(int val) {
            videoSize.width(val);
            return (T) this;
        }

        @Override
        public T videoHeight(int val) {
            videoSize.height(val);
            return (T) this;
        }

        @Override
        public T videoSize(int width, int height) {
            videoSize.setSize(width, height);
            return (T) this;
        }

        @Override
        public T videoSize(ImageSize imageSize) {
            videoSize.setSize(imageSize);
            return (T) this;
        }

        @Override
        public T videoCameraFacing(CameraControllerI.Facing val) {
            videoCameraFacing = val;
            return (T) this;
        }

        public T videoScaleType(ScaleType val) {
            videoScaleType = val;
            return (T) this;
        }

        public T canUpscaleVideo(boolean val) {
            canUpscaleVideo = val;
            return (T) this;
        }

        @Override
        public T canPadVideo(boolean val) {
            canPadVideo = val;
            return (T) this;
        }

        @Override
        public T videoBitrate(int val) {
            videoBitrate = val;
            return (T) this;
        }

        @Override
        public T videoCodec(int val) {
            videoCodec = val;
            return (T) this;
        }

        @Override
        public T videoCodec(VideoCodec val) {
            videoCodec = val.ffmpegCodecValue;
            return (T) this;
        }

        @Override
        public T videoFrameRate(int val) {
            videoFrameRate = val;
            return (T) this;
        }

        @Override
        public T audioBitrate(int val) {
            audioBitrate = val;
            return (T) this;
        }

        @Override
        public T audioChannelCount(int val) {
            audioChannelCount = val;
            return (T) this;
        }

        @Override
        public T audioCodec(int val) {
            audioCodec = val;
            return (T) this;
        }

        @Override
        public T audioCodec(AudioCodec val) {
            audioCodec = val.ffmpegCodecValue;
            return (T) this;
        }

        @Override
        public T audioSamplingRateHz(int val) {
            audioSamplingRateHz = val;
            return (T) this;
        }

        @Override
        public T videoOutputFormat(String val) {
            videoOutputFormat = val;
            return (T) this;
        }

        public T minRecordingMillis(long val) {
            minRecordingMillis = val;
            return (T) this;
        }

        public T maxRecordingMillis(long val) {
            maxRecordingMillis = val;
            return (T) this;
        }

        public T maxFileSizeBytes(long val) {
            maxFileSizeBytes = val;
            return (T) this;
        }

        public T videoThumbnailOutput(File file) {
            videoThumbnailOutputUri = Uri.fromFile(file).toString();
            return (T) this;
        }

        public T videoThumbnailOutputUri(Uri uri) {
            videoThumbnailOutputUri = uri.toString();
            return (T) this;
        }

        public T videoThumbnailOutputUri(String val) {
            videoThumbnailOutputUri = val;
            return (T) this;
        }

        public abstract RecorderParams build();

        @Override
        @CallSuper
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AbstractBuilder<?> that = (AbstractBuilder<?>) o;

            if (canUpscaleVideo != that.canUpscaleVideo) return false;
            if (canPadVideo != that.canPadVideo) return false;
            if (videoBitrate != that.videoBitrate) return false;
            if (videoCodec != that.videoCodec) return false;
            if (videoFrameRate != that.videoFrameRate) return false;
            if (audioBitrate != that.audioBitrate) return false;
            if (audioChannelCount != that.audioChannelCount) return false;
            if (audioCodec != that.audioCodec) return false;
            if (audioSamplingRateHz != that.audioSamplingRateHz) return false;
            if (minRecordingMillis != that.minRecordingMillis) return false;
            if (maxRecordingMillis != that.maxRecordingMillis) return false;
            if (maxFileSizeBytes != that.maxFileSizeBytes) return false;
            if (videoSize != null ? !videoSize.equals(that.videoSize) : that.videoSize != null)
                return false;
            if (videoCameraFacing != that.videoCameraFacing) return false;
            if (videoScaleType != that.videoScaleType) return false;
            if (videoOutputFormat != null ? !videoOutputFormat.equals(that.videoOutputFormat) :
                    that.videoOutputFormat != null)
                return false;
            if (videoOutputUri != null ? !videoOutputUri.equals(that.videoOutputUri) : that
                    .videoOutputUri != null)
                return false;
            return videoThumbnailOutputUri != null ? videoThumbnailOutputUri.equals(that
                    .videoThumbnailOutputUri) : that.videoThumbnailOutputUri == null;

        }

        @Override
        @CallSuper
        public int hashCode() {
            int result = videoSize != null ? videoSize.hashCode() : 0;
            result = 31 * result + (videoCameraFacing != null ? videoCameraFacing.hashCode() : 0);
            result = 31 * result + (videoScaleType != null ? videoScaleType.hashCode() : 0);
            result = 31 * result + (canUpscaleVideo ? 1 : 0);
            result = 31 * result + (canPadVideo ? 1 : 0);
            result = 31 * result + videoBitrate;
            result = 31 * result + videoCodec;
            result = 31 * result + videoFrameRate;
            result = 31 * result + audioBitrate;
            result = 31 * result + audioChannelCount;
            result = 31 * result + audioCodec;
            result = 31 * result + audioSamplingRateHz;
            result = 31 * result + (int) (minRecordingMillis ^ (minRecordingMillis >>> 32));
            result = 31 * result + (int) (maxRecordingMillis ^ (maxRecordingMillis >>> 32));
            result = 31 * result + (int) (maxFileSizeBytes ^ (maxFileSizeBytes >>> 32));
            result = 31 * result + (videoOutputFormat != null ? videoOutputFormat.hashCode() : 0);
            result = 31 * result + (videoOutputUri != null ? videoOutputUri.hashCode() : 0);
            result = 31 * result + (videoThumbnailOutputUri != null ? videoThumbnailOutputUri.hashCode() : 0);
            return result;
        }

        @CallSuper
        protected MoreObjects.ToStringHelper toStringHelper() {
            return MoreObjects.toStringHelper(this)
                    .add("videoSize", videoSize)
                    .add("videoCameraFacing", videoCameraFacing)
                    .add("videoScaleType", videoScaleType)
                    .add("canUpscaleVideo", canUpscaleVideo)
                    .add("canPadVideo", canPadVideo)
                    .add("videoBitrate", videoBitrate)
                    .add("videoCodec", videoCodec)
                    .add("videoFrameRate", videoFrameRate)
                    .add("audioBitrate", audioBitrate)
                    .add("audioChannelCount", audioChannelCount)
                    .add("audioCodec", audioCodec)
                    .add("audioSamplingRateHz", audioSamplingRateHz)
                    .add("minRecordingMillis", minRecordingMillis)
                    .add("maxRecordingMillis", maxRecordingMillis)
                    .add("maxFileSizeBytes", maxFileSizeBytes)
                    .add("videoOutputFormat", videoOutputFormat)
                    .add("videoOutputUri", videoOutputUri)
                    .add("videoThumbnailOutputUri", videoThumbnailOutputUri);
        }

        @Override
        public final String toString() {
            return toStringHelper().toString();
        }
    }

    class Builder extends AbstractBuilder<Builder> {

        public Builder(File videoOutputFile) {
            super(videoOutputFile);
        }

        public Builder(Uri videoOutputUri) {
            super(videoOutputUri);
        }

        public Builder(String videoOutputUri) {
            super(videoOutputUri);
        }

        public RecorderParams build() {
            return new RecorderParamsImpl(this);
        }

        public static class RecorderParamsImpl implements RecorderParams {
            private final ImageSize videoSize;
            private final CameraControllerI.Facing videoCameraFacing;
            private final ScaleType videoScaleType;
            private final boolean canUpscaleVideo;
            private final boolean canPadVideo;
            private final int videoBitrate;
            private final int videoCodec;
            private final int videoFrameRate;

            private final int audioBitrate;
            private final int audioChannelCount;
            private final int audioCodec;
            private final int audioSamplingRateHz;

            private final long minRecordingMillis;
            private final long maxRecordingMillis;
            private final long maxFileSizeBytes;

            private final String videoOutputFormat;
            private final String videoOutputUri;
            private final String videoThumbnailOutputUri;

            public RecorderParamsImpl(AbstractBuilder builder) {
                videoSize = builder.videoSize.build();
                videoCameraFacing = builder.videoCameraFacing;
                videoScaleType = builder.videoScaleType;
                canUpscaleVideo = builder.canUpscaleVideo;
                canPadVideo = builder.canPadVideo;
                videoBitrate = builder.videoBitrate;
                videoCodec = builder.videoCodec;
                videoFrameRate = builder.videoFrameRate;

                audioBitrate = builder.audioBitrate;
                audioChannelCount = builder.audioChannelCount;
                audioCodec = builder.audioCodec;
                audioSamplingRateHz = builder.audioSamplingRateHz;

                minRecordingMillis = builder.minRecordingMillis;
                maxRecordingMillis = builder.maxRecordingMillis;
                maxFileSizeBytes = builder.maxFileSizeBytes;

                videoOutputFormat = builder.videoOutputFormat;
                videoOutputUri = builder.videoOutputUri;
                videoThumbnailOutputUri = builder.videoThumbnailOutputUri;
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
            public ScaleType getVideoScaleType() {
                return videoScaleType;
            }

            @Override
            public boolean canUpscaleVideo() {
                return canUpscaleVideo;
            }

            @Override
            public boolean canPadVideo() {
                return canPadVideo;
            }

            @Override
            public int getVideoBitrate() {
                return videoBitrate;
            }

            @Override
            public int getVideoCodec() {
                return videoCodec;
            }

            @Override
            public int getVideoFrameRate() {
                return videoFrameRate;
            }

            @Override
            public int getAudioBitrate() {
                return audioBitrate;
            }

            @Override
            public int getAudioChannelCount() {
                return audioChannelCount;
            }

            @Override
            public int getAudioCodec() {
                return audioCodec;
            }

            @Override
            public int getAudioSamplingRateHz() {
                return audioSamplingRateHz;
            }

            @Override
            public long getMinRecordingMillis() {
                return minRecordingMillis;
            }

            @Override
            public long getMaxRecordingMillis() {
                return maxRecordingMillis;
            }

            @Override
            public long getMaxFileSizeBytes() {
                return maxFileSizeBytes;
            }

            @Override
            public String getVideoOutputFormat() {
                return videoOutputFormat;
            }

            @Override
            public String getVideoOutputUri() {
                return videoOutputUri;
            }

            @Override
            public String getVideoThumbnailOutputUri() {
                return videoThumbnailOutputUri;
            }

            @Override
            @CallSuper
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                AbstractBuilder<?> that = (AbstractBuilder<?>) o;

                if (canUpscaleVideo != that.canUpscaleVideo) return false;
                if (canPadVideo != that.canPadVideo) return false;
                if (videoBitrate != that.videoBitrate) return false;
                if (videoCodec != that.videoCodec) return false;
                if (videoFrameRate != that.videoFrameRate) return false;
                if (audioBitrate != that.audioBitrate) return false;
                if (audioChannelCount != that.audioChannelCount) return false;
                if (audioCodec != that.audioCodec) return false;
                if (audioSamplingRateHz != that.audioSamplingRateHz) return false;
                if (minRecordingMillis != that.minRecordingMillis) return false;
                if (maxRecordingMillis != that.maxRecordingMillis) return false;
                if (maxFileSizeBytes != that.maxFileSizeBytes) return false;
                if (videoSize != null ? !videoSize.equals(that.videoSize) : that.videoSize != null)
                    return false;
                if (videoCameraFacing != that.videoCameraFacing) return false;
                if (videoScaleType != that.videoScaleType) return false;
                if (videoOutputFormat != null ? !videoOutputFormat.equals(that.videoOutputFormat) :
                        that.videoOutputFormat != null)
                    return false;
                if (videoOutputUri != null ? !videoOutputUri.equals(that.videoOutputUri) : that
                        .videoOutputUri != null)
                    return false;
                return videoThumbnailOutputUri != null ? videoThumbnailOutputUri.equals(that
                        .videoThumbnailOutputUri) : that.videoThumbnailOutputUri == null;

            }

            @Override
            @CallSuper
            public int hashCode() {
                int result = videoSize != null ? videoSize.hashCode() : 0;
                result = 31 * result + (videoCameraFacing != null ? videoCameraFacing.hashCode() : 0);
                result = 31 * result + (videoScaleType != null ? videoScaleType.hashCode() : 0);
                result = 31 * result + (canUpscaleVideo ? 1 : 0);
                result = 31 * result + (canPadVideo ? 1 : 0);
                result = 31 * result + videoBitrate;
                result = 31 * result + videoCodec;
                result = 31 * result + videoFrameRate;
                result = 31 * result + audioBitrate;
                result = 31 * result + audioChannelCount;
                result = 31 * result + audioCodec;
                result = 31 * result + audioSamplingRateHz;
                result = 31 * result + (int) (minRecordingMillis ^ (minRecordingMillis >>> 32));
                result = 31 * result + (int) (maxRecordingMillis ^ (maxRecordingMillis >>> 32));
                result = 31 * result + (int) (maxFileSizeBytes ^ (maxFileSizeBytes >>> 32));
                result = 31 * result + (videoOutputFormat != null ? videoOutputFormat.hashCode() : 0);
                result = 31 * result + (videoOutputUri != null ? videoOutputUri.hashCode() : 0);
                result = 31 * result + (videoThumbnailOutputUri != null ? videoThumbnailOutputUri.hashCode() : 0);
                return result;
            }

            @CallSuper
            protected MoreObjects.ToStringHelper toStringHelper() {
                return MoreObjects.toStringHelper(this)
                        .add("videoSize", videoSize)
                        .add("videoCameraFacing", videoCameraFacing)
                        .add("videoScaleType", videoScaleType)
                        .add("canUpscaleVideo", canUpscaleVideo)
                        .add("canPadVideo", canPadVideo)
                        .add("videoBitrate", videoBitrate)
                        .add("videoCodec", videoCodec)
                        .add("videoFrameRate", videoFrameRate)
                        .add("audioBitrate", audioBitrate)
                        .add("audioChannelCount", audioChannelCount)
                        .add("audioCodec", audioCodec)
                        .add("audioSamplingRateHz", audioSamplingRateHz)
                        .add("minRecordingMillis", minRecordingMillis)
                        .add("maxRecordingMillis", maxRecordingMillis)
                        .add("maxFileSizeBytes", maxFileSizeBytes)
                        .add("videoOutputFormat", videoOutputFormat)
                        .add("videoOutputUri", videoOutputUri)
                        .add("videoThumbnailOutputUri", videoThumbnailOutputUri);
            }

            @Override
            public final String toString() {
                return toStringHelper().toString();
            }
        }
    }
}