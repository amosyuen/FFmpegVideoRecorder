package com.amosyuen.videorecorder.demo;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amosyuen.videorecorder.FFmpegPreviewActivity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.io.IOException;
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


/**
 * Fragment for displaying the list of recorded videos.
 */
public class VideoRecorderResultsFragment extends Fragment {

    private static final String ADAPTER_KEY = "adapter";

    private RecyclerView mRecyclerView;

    private VideoFileAdapter mVideoFileAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            mVideoFileAdapter = new VideoFileAdapter();
            loadExistingFiles();
        } else {
            mVideoFileAdapter = (VideoFileAdapter) savedInstanceState.getSerializable(ADAPTER_KEY);
        }

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
        HashMap<Integer, File> videoFiles = new HashMap<>();
        ArrayList<File> thumbnailFiles = new ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            if (name.endsWith(VideoRecorderRequestFragment.VIDEO_FILE_EXTENSION)) {
                try {
                    videoFiles.put(
                            getFileBase(name, VideoRecorderRequestFragment.VIDEO_FILE_POSTFIX),
                            file);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            } else if (name.endsWith(VideoRecorderRequestFragment.THUMBNAIL_FILE_EXTENSION)) {
                thumbnailFiles.add(file);
            }
        }

        for (File thumbnailFile : thumbnailFiles) {
            try {
                Integer fileBase = getFileBase(
                        thumbnailFile.getName(), VideoRecorderRequestFragment.THUMBNAIL_FILE_POSTFIX);
                File videoFile = videoFiles.get(fileBase);
                if (videoFile != null) {
                    mVideoFileAdapter.mVideoFiles.add(new VideoFile(videoFile, thumbnailFile));
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        if (!mVideoFileAdapter.mVideoFiles.isEmpty()) {
            Collections.sort(mVideoFileAdapter.mVideoFiles);
            mVideoFileAdapter.notifyDataSetChanged();
        }
    }

    private int getFileBase(String name, String postfix) {
        return Integer.parseInt(name.substring(0, name.length() - postfix.length()));
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
            videoFile.videoFile.delete();
            videoFile.thumbnailFile.delete();
            int index = mVideoFiles.indexOf(videoFile);
            mVideoFiles.remove(index);
            notifyItemRemoved(index);
        }

        public void clearVideoFiles() {
            for (VideoFile videoFile : mVideoFiles) {
                videoFile.videoFile.delete();
                videoFile.thumbnailFile.delete();
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

        class VideoFileViewHolder extends RecyclerView.ViewHolder
                implements OnClickListener {

            private View mView;
            private ImageView mThumbnailImageView;
            private TextView mThumbnailFileSizeTextView;
            private TextView mVideoFileDateTextView;
            private TextView mVideoFileSizeTextView;
            private TextView mVideoWidthTextView;
            private TextView mVideoHeightTextView;
            private TextView mVideoLengthTextView;
            private TextView mVideoFrameRateTextView;
            private TextView mVideoBitrateTextView;
            private TextView mAudioSampleRateTextView;
            private TextView mAudioChannelsTextView;

            private VideoFile mVideoFile;

            public VideoFileViewHolder(View view) {
                super(view);
                mView = view;
                mView.setOnClickListener(this);

                mThumbnailImageView = (ImageView) view.findViewById(R.id.thumbnail);
                mThumbnailFileSizeTextView = (TextView) view.findViewById(R.id.thumbnail_file_size);

                mVideoFileDateTextView = (TextView) view.findViewById(R.id.video_file_date);
                mVideoFileSizeTextView = (TextView) view.findViewById(R.id.video_file_size);
                mVideoWidthTextView = (TextView) view.findViewById(R.id.video_width);
                mVideoHeightTextView = (TextView) view.findViewById(R.id.video_height);
                mVideoLengthTextView = (TextView) view.findViewById(R.id.video_length);
                mVideoFrameRateTextView = (TextView) view.findViewById(R.id.video_frame_rate);
                mVideoBitrateTextView = (TextView) view.findViewById(R.id.video_bitrate);

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
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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

            public void setVideoFile(VideoFile videoFile) {
                mVideoFile = videoFile;
                mThumbnailImageView.setImageBitmap(
                        BitmapFactory.decodeFile(videoFile.thumbnailFile.getAbsolutePath()));
                mThumbnailFileSizeTextView.setText(
                        Util.getHumanReadableByteCount(videoFile.thumbnailFile.length(), true));
                mVideoFileDateTextView.setText(Util.getHumanReadableDate(
                        videoFile.videoFile.lastModified()));
                mVideoFileSizeTextView.setText(Util.getHumanReadableByteCount(
                        videoFile.videoFile.length(), true));

                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                try {
                    metadataRetriever.setDataSource(videoFile.videoFile.getAbsolutePath());
                    mVideoBitrateTextView.setText(Util.getHumanReadableBitrate(
                            Integer.parseInt(metadataRetriever.extractMetadata(
                                    MediaMetadataRetriever.METADATA_KEY_BITRATE)), true));
                } catch (Exception e) {
                    e.printStackTrace();
                    mVideoBitrateTextView.setText("");
                }

                MediaExtractor mediaExtractor = new MediaExtractor();
                try {
                    mediaExtractor.setDataSource(videoFile.videoFile.getAbsolutePath());
                    for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                        MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                        switch (mediaFormat.getString(KEY_MIME)) {
                            case MIMETYPE_VIDEO_AVC:
                            case MIMETYPE_VIDEO_DOLBY_VISION:
                            case MIMETYPE_VIDEO_H263:
                            case MIMETYPE_VIDEO_HEVC:
                            case MIMETYPE_VIDEO_MPEG2:
                            case MIMETYPE_VIDEO_MPEG4:
                            case MIMETYPE_VIDEO_RAW:
                            case MIMETYPE_VIDEO_VP8:
                            case MIMETYPE_VIDEO_VP9:
                                mVideoWidthTextView.setText(String.format(Locale.US, "%d px",
                                        mediaFormat.getInteger(MediaFormat.KEY_WIDTH)));
                                mVideoHeightTextView.setText(String.format(Locale.US, "%d px",
                                        mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)));
                                mVideoLengthTextView.setText(Util.getHumanReadableDuration(
                                        mediaFormat.getLong(MediaFormat.KEY_DURATION)));
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
                                mAudioSampleRateTextView.setText(Util.getHumanReadableBitrate(
                                        mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), true));
                                mAudioChannelsTextView.setText(String.format(Locale.US, "%d",
                                        mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)));
                                break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mVideoFileSizeTextView.setText("");
                    mVideoWidthTextView.setText("");
                    mVideoHeightTextView.setText("");
                    mVideoLengthTextView.setText("");
                    mVideoFrameRateTextView.setText("");
                    mVideoBitrateTextView.setText("");
                    mAudioSampleRateTextView.setText("");
                    mAudioChannelsTextView.setText("");
                }
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mView.getContext(), FFmpegPreviewActivity.class);
                intent.setData(Uri.fromFile(mVideoFile.videoFile));
                mView.getContext().startActivity(intent);
            }
        }
    }
}
