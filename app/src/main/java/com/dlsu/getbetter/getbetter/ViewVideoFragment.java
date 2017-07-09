package com.dlsu.getbetter.getbetter;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewVideoFragment extends DialogFragment implements MediaController.MediaPlayerControl{

    VideoView videoView;
    MediaController nMediaController;
    Handler handler;

    String TAG = "video duration";

    public ViewVideoFragment() {
        // Required empty public constructor
    }

    public static ViewVideoFragment newInstance(String videoFile, String videoTitle) {
        ViewVideoFragment frag = new ViewVideoFragment();
        Bundle args = new Bundle();
        args.putString("video", videoFile);
        args.putString("title", videoTitle);
        frag.setArguments(args);
        return frag;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        nMediaController = new MediaController(getActivity());
        nMediaController.setAnchorView(videoView);
        int width = 500;
        int length = 500;
        getDialog().getWindow().setLayout(width, length);

        return inflater.inflate(R.layout.fragment_view_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoView = (VideoView)view.findViewById(R.id.video_view);
        String title = getArguments().getString("title", "Video Attachment");
        String outputFile = getArguments().getString("video");
        getDialog().setTitle(title);

        videoView.setVideoPath(outputFile);
        videoView.setMediaController(nMediaController);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i(TAG, "duration = " + videoView.getDuration());
            }
        });

        videoView.start();

    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int i) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
