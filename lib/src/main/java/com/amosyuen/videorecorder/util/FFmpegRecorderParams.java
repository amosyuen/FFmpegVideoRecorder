package com.amosyuen.videorecorder.util;

import org.bytedeco.javacpp.avcodec;

import java.io.Serializable;

/**
 * Parameters for encoding audio and video using FFmpegFrameRecorder.
 */
public interface FFmpegRecorderParams extends Serializable {

    int getVideoBitrate();
    int getVideoCodec();
    int getVideoFrameRate();
    /** Value from 0 to 10, where 0 is best quality and 10 is worst quality. */
    int getVideoQuality();
    int getVideoWidth();
    int getVideoHeight();

    int getAudioBitrate();
    int getAudioChannelCount();
    int getAudioCodec();
    /** Value from 0 to 10, where 0 is best quality and 10 is worst quality. */
    int getAudioQuality();
    int getAudioSamplingRateHz();

    String getVideoOutputFormat();

    class Builder {
        private int videoBitrate = 1000000;
        private int videoCodec = avcodec.AV_CODEC_ID_H264;
        private int videoFrameRate = 24;
        private int videoQuality = 5;
        private int videoWidth;
        private int videoHeight;
        private int audioBitrate = 128000;
        private int audioChannelCount = 1;
        private int audioCodec = avcodec.AV_CODEC_ID_AAC;
        private int audioQuality = 5;
        private int audioSamplingRateHz = 44100;
        private String videoOutputFormat = "mp4";

        public Builder() {}

        public Builder merge(FFmpegRecorderParams copy) {
            videoBitrate = copy.getVideoBitrate();
            videoCodec = copy.getVideoCodec();
            videoFrameRate = copy.getVideoFrameRate();
            videoQuality = copy.getVideoQuality();
            videoWidth = copy.getVideoWidth();
            videoHeight = copy.getVideoHeight();
            audioBitrate = copy.getAudioBitrate();
            audioChannelCount = copy.getAudioChannelCount();
            audioCodec = copy.getAudioCodec();
            audioQuality = copy.getAudioQuality();
            audioSamplingRateHz = copy.getAudioSamplingRateHz();
            videoOutputFormat = copy.getVideoOutputFormat();
            return this;
        }

        public Builder videoBitrate(int val) {
            videoBitrate = val;
            return this;
        }

        public Builder videoCodec(int val) {
            videoCodec = val;
            return this;
        }

        public Builder videoFrameRate(int val) {
            videoFrameRate = val;
            return this;
        }

        /** Value from 0 to 10, where 0 is best quality and 10 is worst quality. */
        public Builder videoQuality(int val) {
            videoQuality = val;
            return this;
        }

        public Builder videoWidth(int val) {
            videoWidth = val;
            return this;
        }

        public Builder videoHeight(int val) {
            videoHeight = val;
            return this;
        }

        public Builder audioBitrate(int val) {
            audioBitrate = val;
            return this;
        }

        public Builder audioChannelCount(int val) {
            audioChannelCount = val;
            return this;
        }

        public Builder audioCodec(int val) {
            audioCodec = val;
            return this;
        }

        /** Value from 0 to 10, where 0 is best quality and 10 is worst quality. */
        public Builder audioQuality(int val) {
            audioQuality = val;
            return this;
        }

        public Builder audioSamplingRateHz(int val) {
            audioSamplingRateHz = val;
            return this;
        }

        public Builder videoOutputFormat(String val) {
            videoOutputFormat = val;
            return this;
        }

        public FFmpegRecorderParams build() { return new FFmpegRecorderParamsImpl(this); }

        static class FFmpegRecorderParamsImpl implements FFmpegRecorderParams {
            private final int videoBitrate;
            private final int videoCodec;
            private final int videoFrameRate;
            private final int videoQuality;
            private final int videoWidth;
            private final int videoHeight;
            private final int audioBitrate;
            private final int audioChannelCount;
            private final int audioCodec;
            private final int audioQuality;
            private final int audioSamplingRateHz;
            private final String videoOutputFormat;

            protected FFmpegRecorderParamsImpl(Builder builder) {
                videoBitrate = builder.videoBitrate;
                videoCodec = builder.videoCodec;
                videoFrameRate = builder.videoFrameRate;
                videoQuality = builder.videoQuality;
                videoWidth = builder.videoWidth;
                videoHeight = builder.videoHeight;
                audioBitrate = builder.audioBitrate;
                audioChannelCount = builder.audioChannelCount;
                audioCodec = builder.audioCodec;
                audioQuality = builder.audioQuality;
                audioSamplingRateHz = builder.audioSamplingRateHz;
                videoOutputFormat = builder.videoOutputFormat;
            }

            @Override
            public String getVideoOutputFormat() {
                return videoOutputFormat;
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
            public int getVideoQuality() {
                return videoQuality;
            }

            @Override
            public int getVideoWidth() {
                return videoWidth;
            }

            @Override
            public int getVideoHeight() {
                return videoHeight;
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
            public int getAudioQuality() {
                return audioQuality;
            }

            @Override
            public int getAudioSamplingRateHz() {
                return audioSamplingRateHz;
            }
        }
    }
}