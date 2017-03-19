package com.amosyuen.videorecorder.recorder.params;


import com.google.common.base.Optional;

import org.bytedeco.javacpp.avcodec;

import java.io.Serializable;

/**
 * Parameters for encoding video.
 */
public interface EncoderParamsI extends VideoFrameRateParamsI, Serializable {

    /**
     * Audio codecs for encoding from https://ffmpeg.org/general.html#Audio-Codecs. Not all of them
     * are supported
     */
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

    /**
     * Video codecs for encoding from https://ffmpeg.org/general.html#Video-Codecs. Not all of them
     * are supported.
     */
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

    /**
     * Output formats from https://www.ffmpeg.org/ffmpeg-formats.html#Muxers. Not all of them are
     * supported.
     */
    enum OutputFormat {
        AIFF("Audio Interchange File Format"),
        ASF("Advanced Systems Format"),
        AVI("Audio Video Interleaved"),
        CHROMAPRINT("Chromaprint"),
        FLV("Adobe Flash Video"),
        GIF("Animated GIF"),
        HLS("Apple HTTP Live Streaming"),
        ICO("ICO"),
        MKV("Matroska"),
        MOV("Apple Mov"),
        MP4("MPEG-4"),
        ISMV("IIS Smooth Streaming"),
        AAX("Audible AAX"),
        MP3("MPEG-3"),
        MPEGTS("MPEG-2 Transport Stream"),
        MXF("MXF"),
        OGG("OGG");

        public final String name;

        OutputFormat(String name) {
            this.name = name;
        }

        public String getFileExtension() {
            return name().toLowerCase();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Get video bitrate in bits per second.
     */
    Optional<Integer> getVideoBitrate();

    /**
     * Get video codec.
     */
    VideoCodec getVideoCodec();

    /**
     * Get audio bitrate in bits per second.
     */
    Optional<Integer> getAudioBitrate();

    /**
     * Get number of channels of audio to record.
     */
    Optional<Integer> getAudioChannelCount();

    /**
     * Get audio sampling rate in hertz.
     */
    Optional<Integer> getAudioSamplingRateHz();

    /**
     * Get audio codec.
     */
    AudioCodec getAudioCodec();

    /**
     * Get video output format.
     */
    OutputFormat getOutputFormat();

    interface BuilderI<T extends BuilderI<T>> extends VideoFrameRateParamsI.BuilderI<T> {

        /**
         * Sets the video bitrate in bits per second. If not set, the video bitrate will be
         * unrestricted.
         */
        T setVideoBitrate(int val);
        /**
         * Sets the video bitrate in bits per second. If not set, the video bitrate will be
         * unrestricted.
         */
        T setVideoBitrate(Optional<Integer> val);

        /**
         * Set the video codec to use to encode the video. Default value is {@link VideoCodec#H264}.
         */
        T setVideoCodec(VideoCodec val);

        /**
         * Sets the audio bitrate in bits per second. If not set, the audio bitrate will be
         * unrestricted.
         */
        T setAudioBitrate(int val);
        /**
         * Sets the audio bitrate in bits per second. If not set, the audio bitrate will be
         * unrestricted.
         */
        T setAudioBitrate(Optional<Integer> val);

        /**
         * Sets the number of audio channels to use when record. If not set, the default number of
         * channels will be used.
         */
        T setAudioChannelCount(int val);
        /**
         * Sets the number of audio channels to use when record. If not set, the default number of
         * channels will be used.
         */
        T setAudioChannelCount(Optional<Integer> val);

        /**
         * Sets the sampling rate frequency in hertz to use when recording. If not set, the default
         * microphone sampling rate will be used.
         */
        T setAudioSamplingRateHz(int val);
        /**
         * Sets the sampling rate frequency in hertz to use when recording. If not set, the default
         * microphone sampling rate will be used.
         */
        T setAudioSamplingRateHz(Optional<Integer> val);

        /**
         * Set the audio codec to use to encode the audio. Default value is {@link AudioCodec#AAC}.
         */
        T setAudioCodec(AudioCodec val);

        /**
         * Sets the file output format to save the video as. Default value is
         * {@link OutputFormat#MP4}.
         */
        T setOutputFormat(OutputFormat val);

        @Override
        EncoderParamsI build();
    }
}