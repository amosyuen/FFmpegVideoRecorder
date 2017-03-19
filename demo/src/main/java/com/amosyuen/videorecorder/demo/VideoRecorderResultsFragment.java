package com.amosyuen.videorecorder.demo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amosyuen.videorecorder.activity.FFmpegPreviewActivity;
import com.amosyuen.videorecorder.activity.params.FFmpegPreviewActivityParams;
import com.amosyuen.videorecorder.recorder.params.EncoderParamsI;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.media.MediaFormat.KEY_MIME;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AC3;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AMR_NB;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AMR_WB;
import static android.media.MediaFormat.MIMETYPE_AUDIO_EAC3;
import static android.media.MediaFormat.MIMETYPE_AUDIO_FLAC;
import static android.media.MediaFormat.MIMETYPE_AUDIO_G711_ALAW;
import static android.media.MediaFormat.MIMETYPE_AUDIO_G711_MLAW;
import static android.media.MediaFormat.MIMETYPE_AUDIO_MPEG;
import static android.media.MediaFormat.MIMETYPE_AUDIO_MSGSM;
import static android.media.MediaFormat.MIMETYPE_AUDIO_OPUS;
import static android.media.MediaFormat.MIMETYPE_AUDIO_QCELP;
import static android.media.MediaFormat.MIMETYPE_AUDIO_RAW;
import static android.media.MediaFormat.MIMETYPE_AUDIO_VORBIS;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_DOLBY_VISION;
import static android.media.MediaFormat.MIMETYPE_VIDEO_H263;
import static android.media.MediaFormat.MIMETYPE_VIDEO_HEVC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_MPEG2;
import static android.media.MediaFormat.MIMETYPE_VIDEO_MPEG4;
import static android.media.MediaFormat.MIMETYPE_VIDEO_RAW;
import static android.media.MediaFormat.MIMETYPE_VIDEO_VP8;
import static android.media.MediaFormat.MIMETYPE_VIDEO_VP9;
import static com.amosyuen.videorecorder.demo.VideoRecorderRequestFragment.FILE_PREFIX;


/**
 * Fragment for displaying the list of recorded videos.
 */
public class VideoRecorderResultsFragment extends Fragment {

    private static final String ADAPTER_KEY = "adapter";

    private static final String LOG_TAG = "VideoRecorderResults";

    private RecyclerView mRecyclerView;

    private VideoFileAdapter mVideoFileAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mVideoFileAdapter = new VideoFileAdapter();
            loadExistingFiles();
        } else {
            mVideoFileAdapter = (VideoFileAdapter) savedInstanceState.getSerializable(ADAPTER_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_recorder_results, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setAdapter(mVideoFileAdapter);
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext())
                .showLastDivider()
                .build());

        AppCompatButton deleteAllButton = (AppCompatButton) view.findViewById(R.id.delete_all);
        deleteAllButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setCancelable(false)
                        .setTitle(R.string.are_you_sure)
                        .setMessage(R.string.delete_all_video_files)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mVideoFileAdapter.clearVideoFiles();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ADAPTER_KEY, mVideoFileAdapter);
    }

    public void addVideoFile(VideoFile videoFile) {
        mVideoFileAdapter.addVideoFile(videoFile);
        mRecyclerView.smoothScrollToPosition(0);
    }

    private void loadExistingFiles() {
        File dir = getContext().getExternalCacheDir();
        File[] files = dir.listFiles();
        ArrayList<File> videoFiles = new ArrayList<>();
        HashMap<String, File> thumbnailFiles = new HashMap<>();
        for (File file : files) {
            String[] parts = file.getName().split("\\.");
            if (parts.length != 2 || !parts[0].startsWith(FILE_PREFIX)) {
                continue;
            }
            String ext = parts[1];
            if (ext.equals(VideoRecorderRequestFragment.THUMBNAIL_FILE_EXTENSION)) {
                thumbnailFiles.put(parts[0], file);
            } else {
                try {
                    EncoderParamsI.OutputFormat.valueOf(ext.toUpperCase());
                    videoFiles.add(file);
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, String.format("Unsupported video file: %s", file.getName()));
                }
            }
        }

        for (File file : videoFiles) {
            String[] parts = file.getName().split("\\.");
            File thumbnailFile = thumbnailFiles.get(parts[0]);
            if (thumbnailFile == null) {
                Log.e(LOG_TAG, String.format("Video file is missing thumbnail %s", file.getName()));
                continue;
            }
            mVideoFileAdapter.mVideoFiles.add(VideoFile.create(file, thumbnailFile));
        }

        if (!mVideoFileAdapter.mVideoFiles.isEmpty()) {
            Collections.sort(mVideoFileAdapter.mVideoFiles);
            mVideoFileAdapter.notifyDataSetChanged();
        }
    }

    static class VideoFileAdapter
            extends RecyclerView.Adapter<VideoFileAdapter.VideoFileViewHolder>
            implements Serializable {

        private List<VideoFile> mVideoFiles = new ArrayList<>();

        public VideoFileAdapter() {
            setHasStableIds(true);
        }

        public void addVideoFile(VideoFile videoFile) {
            mVideoFiles.add(0, videoFile);
            notifyItemInserted(0);
        }

        public void deleteVideoFile(VideoFile videoFile) {
            videoFile.getVideoFile().delete();
            videoFile.getThumbnailFile().delete();
            int index = mVideoFiles.indexOf(videoFile);
            mVideoFiles.remove(index);
            notifyItemRemoved(index);
        }

        public void clearVideoFiles() {
            for (VideoFile videoFile : mVideoFiles) {
                videoFile.getVideoFile().delete();
                videoFile.getThumbnailFile().delete();
            }
            mVideoFiles.clear();
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return mVideoFiles.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return mVideoFiles.size();
        }

        @Override
        public VideoFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_video_file, parent, false);
            return new VideoFileViewHolder(v);
        }

        @Override
        public void onBindViewHolder(VideoFileViewHolder holder, int position) {
            holder.setVideoFile(mVideoFiles.get(position));
        }

        class VideoFileViewHolder extends RecyclerView.ViewHolder {

            private View mView;
            private ImageView mThumbnailImageView;
            private TextView mThumbnailFileSizeTextView;
            private TextView mVideoFileDateTextView;
            private TextView mVideoFileSizeTextView;
            private TextView mVideoFileFormatTextView;
            private TextView mVideoLengthTextView;

            private TextView mVideoCodecTextView;
            private TextView mVideoWidthTextView;
            private TextView mVideoHeightTextView;
            private TextView mVideoFrameRateTextView;
            private TextView mVideoBitrateTextView;

            private TextView mAudioCodecTextView;
            private TextView mAudioSampleRateTextView;
            private TextView mAudioChannelsTextView;

            private VideoFile mVideoFile;

            public VideoFileViewHolder(View view) {
                super(view);
                mView = view;

                mThumbnailImageView = (ImageView) view.findViewById(R.id.thumbnail);
                mThumbnailFileSizeTextView = (TextView) view.findViewById(R.id.thumbnail_file_size);

                mVideoFileDateTextView = (TextView) view.findViewById(R.id.video_file_date);
                mVideoFileSizeTextView = (TextView) view.findViewById(R.id.video_file_size);
                mVideoFileFormatTextView = (TextView) view.findViewById(R.id.video_file_format);
                mVideoLengthTextView = (TextView) view.findViewById(R.id.video_length);

                mVideoCodecTextView = (TextView) view.findViewById(R.id.video_codec);
                mVideoWidthTextView = (TextView) view.findViewById(R.id.video_width);
                mVideoHeightTextView = (TextView) view.findViewById(R.id.video_height);
                mVideoFrameRateTextView = (TextView) view.findViewById(R.id.video_frame_rate);
                mVideoBitrateTextView = (TextView) view.findViewById(R.id.video_bitrate);

                mAudioCodecTextView = (TextView) view.findViewById(R.id.audio_codec);
                mAudioSampleRateTextView = (TextView) view.findViewById(R.id.audio_sample_rate);
                mAudioChannelsTextView = (TextView) view.findViewById(R.id.audio_channels);

                ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(mView.getContext())
                                .setCancelable(false)
                                .setTitle(R.string.are_you_sure)
                                .setMessage(R.string.delete_video_file)
                                .setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteVideoFile(mVideoFile);
                                            }
                                        })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                    }
                });
            }

            public void setVideoFile(final VideoFile videoFile) {
                mView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mView.getContext(), FFmpegPreviewActivity.class);
                        intent.putExtra(
                                FFmpegPreviewActivity.REQUEST_PARAMS_KEY,
                                FFmpegPreviewActivityParams.builder(mView.getContext())
                                        .setVideoFileUri(videoFile.getVideoFile()).build());
                        mView.getContext().startActivity(intent);
                    }
                });

                mVideoFile = videoFile;
                mThumbnailImageView.setBackground(new BitmapDrawable(
                        mView.getContext().getResources(),
                        BitmapFactory.decodeFile(videoFile.getThumbnailFile().getAbsolutePath())));
                mThumbnailImageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mView.getContext(), ThumbnailActivity.class);
                        intent.setData(Uri.fromFile(mVideoFile.getThumbnailFile()));
                        mView.getContext().startActivity(intent);
                    }
                });
                mThumbnailFileSizeTextView.setText(
                        Util.getHumanReadableByteCount(videoFile.getThumbnailFile().length(), true));
                mVideoFileDateTextView.setText(Util.getHumanReadableDate(
                        videoFile.getVideoFile().lastModified()));
                mVideoFileSizeTextView.setText(Util.getHumanReadableByteCount(
                        videoFile.getVideoFile().length(), true));
                String[] parts = videoFile.getVideoFile().getName().split("\\.");
                mVideoFileFormatTextView.setText(parts[1]);

                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                try {
                    metadataRetriever.setDataSource(videoFile.getVideoFile().getAbsolutePath());
                    mVideoBitrateTextView.setText(Util.getHumanReadableBitrate(
                            Integer.parseInt(metadataRetriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_BITRATE)), true));
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error extracting video birate", e);
                    mVideoBitrateTextView.setText("");
                } finally {
                    metadataRetriever.release();
                }

                MediaExtractor mediaExtractor = new MediaExtractor();
                mVideoCodecTextView.setText(R.string.unknown);
                mAudioCodecTextView.setText(R.string.unknown);
                try {
                    mediaExtractor.setDataSource(videoFile.getVideoFile().getAbsolutePath());
                    for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                        MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                        String mimeType = mediaFormat.getString(KEY_MIME);
                        switch (mimeType) {
                            case MIMETYPE_VIDEO_AVC:
                            case MIMETYPE_VIDEO_DOLBY_VISION:
                            case MIMETYPE_VIDEO_H263:
                            case MIMETYPE_VIDEO_HEVC:
                            case MIMETYPE_VIDEO_MPEG2:
                            case MIMETYPE_VIDEO_MPEG4:
                            case MIMETYPE_VIDEO_RAW:
                            case MIMETYPE_VIDEO_VP8:
                            case MIMETYPE_VIDEO_VP9:
                                mVideoLengthTextView.setText(Util.getHumanReadableDuration(
                                        mediaFormat.getLong(MediaFormat.KEY_DURATION)));
                                mVideoCodecTextView.setText(Util.getMimetype(mimeType));
                                mVideoWidthTextView.setText(String.format(Locale.US, "%d px",
                                        mediaFormat.getInteger(MediaFormat.KEY_WIDTH)));
                                mVideoHeightTextView.setText(String.format(Locale.US, "%d px",
                                        mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)));
                                mVideoFrameRateTextView.setText(String.format(Locale.US, "%d fps",
                                        mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)));
                                break;
                            case MIMETYPE_AUDIO_AAC:
                            case MIMETYPE_AUDIO_AC3:
                            case MIMETYPE_AUDIO_AMR_NB:
                            case MIMETYPE_AUDIO_AMR_WB:
                            case MIMETYPE_AUDIO_EAC3:
                            case MIMETYPE_AUDIO_FLAC:
                            case MIMETYPE_AUDIO_G711_ALAW:
                            case MIMETYPE_AUDIO_G711_MLAW:
                            case MIMETYPE_AUDIO_MPEG:
                            case MIMETYPE_AUDIO_MSGSM:
                            case MIMETYPE_AUDIO_OPUS:
                            case MIMETYPE_AUDIO_QCELP:
                            case MIMETYPE_AUDIO_RAW:
                            case MIMETYPE_AUDIO_VORBIS:
                                mAudioCodecTextView.setText(Util.getMimetype(mimeType));
                                mAudioSampleRateTextView.setText(String.format(Locale.US, "%d Hz",
                                        mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)));
                                mAudioChannelsTextView.setText(String.format(Locale.US, "%d",
                                        mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)));
                                break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error extracting media metadata", e);
                    mVideoLengthTextView.setText("");
                    mVideoCodecTextView.setText("");
                    mVideoWidthTextView.setText("");
                    mVideoHeightTextView.setText("");
                    mVideoFrameRateTextView.setText("");

                    mAudioCodecTextView.setText("");
                    mAudioSampleRateTextView.setText("");
                    mAudioChannelsTextView.setText("");
                } finally {
                    mediaExtractor.release();
                }
            }
        }
    }
}
