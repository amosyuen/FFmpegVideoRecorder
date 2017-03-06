package com.amosyuen.videorecorder.util;


import org.bytedeco.javacpp.avcodec;

import java.io.Serializable;

/**
 * Parameters for encoding audio and video using FFmpegFrameRecorder.
 */
public interface FFmpegRecorderParams extends Serializable {

    /** Valid audio codecs for encoding from https://ffmpeg.org/general.html#Audio-Codecs */
    enum AudioCodec {
        AAC("AAC", avcodec.AV_CODEC_ID_AAC),
        AAC_LATM("AAC LATM", avcodec.AV_CODEC_ID_AAC_LATM),
        AC("AC-3", avcodec.AV_CODEC_ID_AC3),
        ADPCM_G722("ADPCM G.722", avcodec.AV_CODEC_ID_ADPCM_G722),
        ADPCM_G726("ADPCM G.726", avcodec.AV_CODEC_ID_ADPCM_G726),
        ADPCM_G726LE("ADPCM G.726 LE", avcodec.AV_CODEC_ID_ADPCM_G726LE),
        ADPCM_IMA_QT("ADPCM IMA QuickTime", avcodec.AV_CODEC_ID_ADPCM_IMA_QT),
        ADPCM_IMA_WAV("ADPCM IMA WAV", avcodec.AV_CODEC_ID_ADPCM_IMA_WAV),
        ADPCM_MS("ADPCM Microsoft", avcodec.AV_CODEC_ID_ADPCM_MS),
        ADPCM_SWF("ADPCM Shockwave Flash", avcodec.AV_CODEC_ID_ADPCM_SWF),
        ADPCM_YAMAHA("ADPCM Yamaha", avcodec.AV_CODEC_ID_ADPCM_YAMAHA),
        AMR_NB("AMR-NB", avcodec.AV_CODEC_ID_AMR_NB),
        AMR_WB("AMR-WB", avcodec.AV_CODEC_ID_AMR_WB),
        ALAC("Apple lossless audio", avcodec.AV_CODEC_ID_ALAC),
        DCA("DCA (DTS Coherent Acoustics)", avcodec.AV_CODEC_ID_DTS),
        DPCM("DPCM id RoQ", avcodec.AV_CODEC_ID_ROQ_DPCM),
        EAC3("Enhanced AC-3", avcodec.AV_CODEC_ID_EAC3),
        FLAC("FLAC (Free Lossless Audio Codec)", avcodec.AV_CODEC_ID_FLAC),
        G723_1("G.723.1", avcodec.AV_CODEC_ID_G723_1),
        GSM("GSM", avcodec.AV_CODEC_ID_GSM),
        GSM_MS("GSM Microsoft variant", avcodec.AV_CODEC_ID_GSM_MS),
        ILBC("iLBC (Internet Low Bitrate Codec)", avcodec.AV_CODEC_ID_ILBC),
        MLP("MLP (Meridian Lossless Packing)", avcodec.AV_CODEC_ID_MLP),
        MP2("MP2 (MPEG audio layer 2)", avcodec.AV_CODEC_ID_MP2),
        MP3("MP3 (MPEG audio layer 3)", avcodec.AV_CODEC_ID_MP3),
        NELLYMOSER("Nellymoser Asao", avcodec.AV_CODEC_ID_NELLYMOSER),
        OPUS("Opus", avcodec.AV_CODEC_ID_OPUS),
        PCM_F32BE("PCM 32-bit floating point big-endian", avcodec.AV_CODEC_ID_PCM_F32BE),
        PCM_F32LE("PCM 32-bit floating point little-endian", avcodec.AV_CODEC_ID_PCM_F32LE),
        PCM_F64BE("PCM 64-bit floating point big-endian", avcodec.AV_CODEC_ID_PCM_F64BE),
        PCM_F64LE("PCM 64-bit floating point little-endian", avcodec.AV_CODEC_ID_PCM_F64LE),
        PCM_ALAW("PCM A-law", avcodec.AV_CODEC_ID_PCM_ALAW),
        PCM_S24DAUD("PCM D-Cinema audio signed 24-bit", avcodec.AV_CODEC_ID_PCM_S24DAUD),
        PCM_MULAW("PCM mu-law", avcodec.AV_CODEC_ID_PCM_MULAW),
        PCM_S16BE("PCM signed 16-bit big-endian", avcodec.AV_CODEC_ID_PCM_S16BE),
        PCM_S16BE_PLANAR("PCM signed 16-bit big-endian planar", avcodec.AV_CODEC_ID_PCM_S16BE_PLANAR),
        PCM_S16LE("PCM signed 16-bit little-endian", avcodec.AV_CODEC_ID_PCM_S16LE),
        PCM_S16LE_PLANAR("PCM signed 16-bit little-endian planar", avcodec.AV_CODEC_ID_PCM_S16LE_PLANAR),
        PCM_S24BE("PCM signed 24-bit big-endian", avcodec.AV_CODEC_ID_PCM_S24BE),
        PCM_S24LE("PCM signed 24-bit little-endian", avcodec.AV_CODEC_ID_PCM_S24LE),
        PCM_S24LE_PLANAR("PCM signed 24-bit little-endian planar", avcodec.AV_CODEC_ID_PCM_S24LE_PLANAR),
        PCM_S32BE("PCM signed 32-bit big-endian", avcodec.AV_CODEC_ID_PCM_S32BE),
        PCM_S32LE("PCM signed 32-bit little-endian", avcodec.AV_CODEC_ID_PCM_S32LE),
        PCM_S32LE_PLANAR("PCM signed 32-bit little-endian planar", avcodec.AV_CODEC_ID_PCM_S32LE_PLANAR),
        PCM_S8("PCM signed 8-bit", avcodec.AV_CODEC_ID_PCM_S8),
        PCM_S8_PLANAR("PCM signed 8-bit planar", avcodec.AV_CODEC_ID_PCM_S8_PLANAR),
        PCM_U16BE("PCM unsigned 16-bit big-endian", avcodec.AV_CODEC_ID_PCM_U16BE),
        PCM_U16LE("PCM unsigned 16-bit little-endian", avcodec.AV_CODEC_ID_PCM_U16LE),
        PCM_U24BE("PCM unsigned 24-bit big-endian", avcodec.AV_CODEC_ID_PCM_U24BE),
        PCM_U24LE("PCM unsigned 24-bit little-endian", avcodec.AV_CODEC_ID_PCM_U24LE),
        PCM_U32BE("PCM unsigned 32-bit big-endian", avcodec.AV_CODEC_ID_PCM_U32BE),
        PCM_U32LE("PCM unsigned 32-bit little-endian", avcodec.AV_CODEC_ID_PCM_U32LE),
        PCM_U8("PCM unsigned 8-bit", avcodec.AV_CODEC_ID_PCM_U8),
        RA_144("RealAudio 1.0 (14.4K)", avcodec.AV_CODEC_ID_RA_144),
        RA_288("RealAudio 3.0 (dnet)", avcodec.AV_CODEC_ID_RA_288),
        SMPTE_KLV("SMPTE 302M AES3 audio", avcodec.AV_CODEC_ID_SMPTE_KLV),
        SONIC("Sonic", avcodec.AV_CODEC_ID_SONIC),
        SONIC_LS("Sonic lossless", avcodec.AV_CODEC_ID_SONIC_LS),
        SPEEX("Speex", avcodec.AV_CODEC_ID_SPEEX),
        TTA("True Audio (TTA)", avcodec.AV_CODEC_ID_TTA),
        TRUEHD("TrueHD", avcodec.AV_CODEC_ID_TRUEHD),
        VORBIS("Vorbis", avcodec.AV_CODEC_ID_VORBIS),
        WAVPACK("WavPack", avcodec.AV_CODEC_ID_WAVPACK),
        WMAV1("Windows Media Audio 1", avcodec.AV_CODEC_ID_WMAV1),
        WMAV2("Windows Media Audio 2", avcodec.AV_CODEC_ID_WMAV2);

        public final String name;
        public final int ffmpegCodecValue;

        AudioCodec(String name, int ffmpegCodecValue) {
            this.name = name;
            this.ffmpegCodecValue = ffmpegCodecValue;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** Valid video codecs for encoding from https://ffmpeg.org/general.html#Video-Codecs */
    enum VideoCodec {
        A64_MULTI("A64 multicolor", avcodec.AV_CODEC_ID_A64_MULTI),
        A64_MULTI5("A64 multicolor extended with 5th color", avcodec.AV_CODEC_ID_A64_MULTI5),
        AMV("AMV Video", avcodec.AV_CODEC_ID_AMV),
        ASV1("Asus v1", avcodec.AV_CODEC_ID_ASV1),
        ASV2("Asus v2", avcodec.AV_CODEC_ID_ASV2),
        AVRP("Avid 1:1 10-bit RGB Packer", avcodec.AV_CODEC_ID_AVRP),
        AYUV("AYUV", avcodec.AV_CODEC_ID_AYUV),
        CAVS("Chinese AVS video", avcodec.AV_CODEC_ID_CAVS),
        CLJR("Cirrus Logic AccuPak", avcodec.AV_CODEC_ID_CLJR),
        DIRAC("Dirac", avcodec.AV_CODEC_ID_DIRAC),
        DNXHD("DNxHD", avcodec.AV_CODEC_ID_DNXHD),
        DVVIDEO("DV (Digital Video)", avcodec.AV_CODEC_ID_DVVIDEO),
        FFV1("FFmpeg video codec #1", avcodec.AV_CODEC_ID_FFV1),
        FFVHUFF("HuffYUV FFmpeg variant", avcodec.AV_CODEC_ID_FFVHUFF),
        FLASHSV("Flash Screen Video v1", avcodec.AV_CODEC_ID_FLASHSV),
        FLASHSV2("Flash Screen Video v2", avcodec.AV_CODEC_ID_FLASHSV2),
        FLV1("Flash Video (FLV)", avcodec.AV_CODEC_ID_FLV1),
        H261("H.261", avcodec.AV_CODEC_ID_H261),
        H263("H.263", avcodec.AV_CODEC_ID_H263),
        H263I("H.263I", avcodec.AV_CODEC_ID_H263I),
        H263P("H.263P", avcodec.AV_CODEC_ID_H263P),
        H264("H.264", avcodec.AV_CODEC_ID_H264),
        HEVC("HEVC", avcodec.AV_CODEC_ID_HEVC),
        HUFFYUV("HuffYUV", avcodec.AV_CODEC_ID_HUFFYUV),
        JPEG2000("JPEG 2000", avcodec.AV_CODEC_ID_JPEG2000),
        MJPEG("Motion JPEG", avcodec.AV_CODEC_ID_MJPEG),
        MJPEGB("Motion JPEG-B", avcodec.AV_CODEC_ID_MJPEGB),
        MPEG1VIDEO("MPEG-1 video", avcodec.AV_CODEC_ID_MPEG1VIDEO),
        MPEG2VIDEO("MPEG-2 video", avcodec.AV_CODEC_ID_MPEG2VIDEO),
        MPEG2VIDEO_XVMC("MPEG-2 video XVMC", avcodec.AV_CODEC_ID_MPEG2VIDEO_XVMC),
        MPEG4("MPEG-4 part 2", avcodec.AV_CODEC_ID_MPEG4),
        MSMPEG4V2("MPEG-4 part 2 Microsoft variant v2", avcodec.AV_CODEC_ID_MSMPEG4V2),
        MSMPEG4V3("MPEG-4 part 2 Microsoft variant v3", avcodec.AV_CODEC_ID_MSMPEG4V3),
        PRORES("Apple ProRes", avcodec.AV_CODEC_ID_PRORES),
        QTRLE("Quicktime Animation (RLE) video", avcodec.AV_CODEC_ID_QTRLE),
        R10K("R10K AJA Kona 10-bit RGB Codec", avcodec.AV_CODEC_ID_R10K),
        R210("R210 Quicktime Uncompressed RGB 10-bit", avcodec.AV_CODEC_ID_R210),
        RAWVIDEO("Raw Video", avcodec.AV_CODEC_ID_RAWVIDEO),
        ROQ("id RoQ video", avcodec.AV_CODEC_ID_ROQ),
        RV10("RealVideo 1.0", avcodec.AV_CODEC_ID_RV10),
        RV20("RealVideo 2.0", avcodec.AV_CODEC_ID_RV20),
        SNOW("Snow", avcodec.AV_CODEC_ID_SNOW),
        SVQ1("Sorenson Vector Quantizer 1", avcodec.AV_CODEC_ID_SVQ1),
        THEORA("Theora", avcodec.AV_CODEC_ID_THEORA),
        UTVIDEO("Ut Video", avcodec.AV_CODEC_ID_UTVIDEO),
        V210("v210 QuickTime uncompressed 4:2:2 10-bit", avcodec.AV_CODEC_ID_V210),
        V210X("v210 QuickTime uncompressed 4:2:2 10-bit X", avcodec.AV_CODEC_ID_V210X),
        V308("v308 QuickTime uncompressed 4:4:4", avcodec.AV_CODEC_ID_V308),
        V408("v408 QuickTime uncompressed 4:4:4:4", avcodec.AV_CODEC_ID_V408),
        V410("v410 QuickTime uncompressed 4:4:4 10-bit", avcodec.AV_CODEC_ID_V410),
        VP8("VP8", avcodec.AV_CODEC_ID_VP8),
        VP9("VP9", avcodec.AV_CODEC_ID_VP9),
        WMV1("Windows Media Video 7", avcodec.AV_CODEC_ID_WMV1),
        WMV2("Windows Media Video 8", avcodec.AV_CODEC_ID_WMV2),
        Y41P("y41p Brooktree uncompressed 4:1:1 12-bit", avcodec.AV_CODEC_ID_Y41P),
        YUV4("yuv4", avcodec.AV_CODEC_ID_YUV4),
        ZLIB("ZLIB", avcodec.AV_CODEC_ID_ZLIB),
        ZMBV("Zip Motion Blocks Video", avcodec.AV_CODEC_ID_ZMBV);

        public final String name;
        public final int ffmpegCodecValue;

        VideoCodec(String name, int ffmpegCodecValue) {
            this.name = name;
            this.ffmpegCodecValue = ffmpegCodecValue;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** Video quality must be set to negative one to use the bitrate. */
    int getVideoBitrate();
    int getVideoCodec();
    int getVideoFrameRate();
    /** Value depends on the video codec. Will override the video bitrate parameter. */
    int getVideoQuality();
    int getVideoWidth();
    int getVideoHeight();

    /** Audio quality must be set to negative one to use the bitrate. */
    int getAudioBitrate();
    int getAudioChannelCount();
    int getAudioCodec();
    /** Value depends on the audio codec. Will override the audio bitrate parameter. */
    int getAudioQuality();
    int getAudioSamplingRateHz();

    String getVideoOutputFormat();

    class Builder {
        private int videoBitrate = 1000000;
        private int videoCodec = VideoCodec.H264.ffmpegCodecValue;
        private int videoFrameRate = 20;
        private int videoQuality = -1;
        private int videoWidth;
        private int videoHeight;
        private int audioBitrate = 128000;
        private int audioChannelCount = 1;
        private int audioCodec = AudioCodec.AAC.ffmpegCodecValue;
        private int audioQuality = -1;
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

        /**
         * Sets the video bitrate and sets the video quality to negative one. Video quality must be
         * negative one to use the video bitrate.
         */
        public Builder videoBitrate(int val) {
            videoBitrate = val;
            videoQuality = -1;
            return this;
        }

        public Builder videoCodec(int val) {
            videoCodec = val;
            return this;
        }

        public Builder videoCodec(VideoCodec val) {
            videoCodec = val.ffmpegCodecValue;
            return this;
        }

        public Builder videoFrameRate(int val) {
            videoFrameRate = val;
            return this;
        }

        /**
         * Value depends on the video codec. Will override the video bitrate parameter.
         * For <a href='https://trac.ffmpeg.org/wiki/Encode/H.264'>H.264</a> choose a value from
         * 0-51 where 0 is lossless, 23 is default, and 51 is worst possible. Consider 18 to be
         * nearly visually lossless.
         */
        public Builder videoQuality(int val) {
            videoQuality = val;
            return this;
        }

        /**
         * Width will be rounded up to the closest multiple of 2. If height is defined, height will
         * be recalculated to maintain the aspect ratio. Recommended to use a multiple of 16 for
         * best encoding efficiency.
         */
        public Builder videoWidth(int val) {
            videoWidth = val;
            return this;
        }

        /**
         * If only height is specified, the height will be changed to make sure the width is a
         * multiple of 2 while maintain the camera aspect ratio.
         */
        public Builder videoHeight(int val) {
            videoHeight = val;
            return this;
        }

        /**
         * Sets the audio bitrate and sets the audio quality to negative one. Audio quality must be
         * negative one to use the audio bitrate.
         */
        public Builder audioBitrate(int val) {
            audioBitrate = val;
            audioQuality = -1;
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

        public Builder audioCodec(AudioCodec val) {
            audioCodec = val.ffmpegCodecValue;
            return this;
        }

        /**
         * Value depends on the audio codec. Will override the audio bitrate parameter.
         * For <a href='https://trac.ffmpeg.org/wiki/Encode/AAC'>AAC</a> choose a value between 1
         * and 5, where 1 is the lowest quality and 5 is the highest quality.
         */
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

            @Override
            public String toString() {
                return "FFmpegRecorderParamsImpl{" +
                        "videoBitrate=" + videoBitrate +
                        ", videoCodec=" + videoCodec +
                        ", videoFrameRate=" + videoFrameRate +
                        ", videoQuality=" + videoQuality +
                        ", videoWidth=" + videoWidth +
                        ", videoHeight=" + videoHeight +
                        ", audioBitrate=" + audioBitrate +
                        ", audioChannelCount=" + audioChannelCount +
                        ", audioCodec=" + audioCodec +
                        ", audioQuality=" + audioQuality +
                        ", audioSamplingRateHz=" + audioSamplingRateHz +
                        ", videoOutputFormat='" + videoOutputFormat + '\'' +
                        '}';
            }
        }
    }
}