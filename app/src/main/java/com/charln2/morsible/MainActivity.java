package com.charln2.morsible;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.Touch;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.zip.Inflater;

import static android.R.attr.button;
import static android.R.attr.id;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity Class";
    private static final int RC_SIGN_IN = 1;
    //UI/ Resources
    private MediaPlayer mp;
    private Button b;

    //Firebase Components

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Firebase Components

        // Init Listeners

        // Init UI/Resources
        mp = MediaPlayer.create(this, R.raw.tone_600hz);
        b = (Button) findViewById(R.id.button);
//                            b.setHighlightColor(Color.CYAN);

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
                return false; // So the OnTouchListener does not consume event; its color can change
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        //todo: paste authui logout code here or delete in merge conflict
    }
}
