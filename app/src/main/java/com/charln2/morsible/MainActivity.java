package com.charln2.morsible;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import static android.R.attr.button;
import static android.R.attr.id;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mp;
    private Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp = MediaPlayer.create(this, R.raw.tone_600hz);

        b = (Button) findViewById(R.id.button);
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!mp.isPlaying()) {
                        mp.start();
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mp.pause();
//                    mp.seekTo(0);
                }
                return true;
            }
        });
    }
}
