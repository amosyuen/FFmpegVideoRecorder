package com.amosyuen.videorecorder.audio;

import java.io.Serializable;

/**
 * Params for audio recording.
 */
public interface AudioRecorderParams extends Serializable {

    int getAudioChannelCount();
    int getAudioSamplingRateHz();

    class Builder {
        private int audioChannelCount = 1;
        private int audioSamplingRateHz = 44100;

        public Builder() {}

        public Builder merge(AudioRecorderParams copy) {
            audioChannelCount = copy.getAudioChannelCount();
            audioSamplingRateHz = copy.getAudioSamplingRateHz();
            return this;
        }

        public Builder audioChannelCount(int val) {
            audioChannelCount = val;
            return this;
        }

        public Builder audioSamplingRateHz(int val) {
            audioSamplingRateHz = val;
            return this;
        }

        public AudioRecorderParams build() { return new AudioRecorderParamsImpl(this); }

        static class AudioRecorderParamsImpl implements AudioRecorderParams {
            private final int audioChannelCount;
            private final int audioSamplingRateHz;

            protected AudioRecorderParamsImpl(Builder builder) {
                audioChannelCount = builder.audioChannelCount;
                audioSamplingRateHz = builder.audioSamplingRateHz;
            }

            @Override
            public int getAudioChannelCount() {
                return audioChannelCount;
            }

            @Override
            public int getAudioSamplingRateHz() {
                return audioSamplingRateHz;
            }
        }
    }
}