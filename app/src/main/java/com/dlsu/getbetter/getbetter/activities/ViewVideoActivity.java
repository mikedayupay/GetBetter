package com.dlsu.getbetter.getbetter.activities;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;

import com.dlsu.getbetter.getbetter.R;

import java.io.File;

public class ViewVideoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView videoTextView;
    private Button backbtn;

    private MediaPlayer mediaPlayer;
    private Uri uri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        Bundle extras = getIntent().getExtras();
        String videoUrl = extras.getString("videoUrl");
        String videoTitle = extras.getString("videoTitle");

        surfaceView = (SurfaceView)findViewById(R.id.view_video);
        videoTextView = (TextView)findViewById(R.id.view_video_title);
        backbtn = (Button)findViewById(R.id.view_video_back_btn);

        if (videoUrl != null) {
            uri = Uri.fromFile(new File(videoUrl));
        }
        videoTextView.setText(videoTitle);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFixedSize(180, 144);
        mediaPlayer = new MediaPlayer();






    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}

