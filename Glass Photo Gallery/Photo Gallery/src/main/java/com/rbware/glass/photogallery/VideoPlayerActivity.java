package com.rbware.glass.photogallery;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String videoUrl = getIntent().getExtras().getString("videoUrl");
        if (videoUrl != null){
            VideoView videoView = new VideoView(this);
            videoView.setMediaController(new MediaController(this));
            videoView.setVideoURI(Uri.parse(videoUrl));
            videoView.requestFocus();
            videoView.start();

            setContentView(videoView);
        } else {
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}