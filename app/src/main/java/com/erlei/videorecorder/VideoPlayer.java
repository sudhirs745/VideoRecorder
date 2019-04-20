package com.erlei.videorecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

public class VideoPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

      String  image_or_video_path=getIntent().getStringExtra("file_path");
        Log.e("video_come",image_or_video_path +" ");

        VideoView videoView = findViewById(R.id.VideoView);
        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);

        if(image_or_video_path!=null) {
            videoView.setVideoPath(image_or_video_path);
            videoView.start();
        }
    }
}
