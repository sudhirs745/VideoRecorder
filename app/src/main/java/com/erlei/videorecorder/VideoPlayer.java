package com.erlei.videorecorder;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class VideoPlayer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

      final String  image_or_video_path=getIntent().getStringExtra("file_path");
        Log.e("video_come",image_or_video_path +" ");

        VideoView videoView = findViewById(R.id.VideoView);
        //MediaController mediaController = new MediaController(this);
        // mediaController.setAnchorView(videoView);
        //videoView.setMediaController(mediaController);

        if(image_or_video_path!=null) {
            videoView.setVideoPath(image_or_video_path);
            videoView.start();
          //  videoView.start();
            videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.e("", "onPrepared");

                }
            });


        }

        Button btNext= findViewById(R.id.btnNext);
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              Intent intent = new Intent(VideoPlayer.this, PoastActivity.class);
                intent.putExtra("file_path",image_or_video_path);
                startActivity(new Intent(VideoPlayer.this, PoastActivity.class));
                finish();
            }
        });


    }


}
