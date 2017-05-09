package com.charln2.morsible;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.Touch;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import static android.R.attr.button;
import static android.R.attr.id;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity Class";
    private static final int RC_SIGN_IN = 1;
    //UI/ Resources
    private MediaPlayer mp;
    private Button b;
    //todo: username, ListView, Adapter

    //Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDBRefListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
        attachDBRefListener();
        //clear adapter
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener == null) {
            //TODO: Move to onPause?
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Signed in on Reslt!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Sign-in Cancelled on Reslt!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Firebase Components
        mAuth = FirebaseAuth.getInstance();

        // Init Listeners
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(getApplicationContext(), "Signed in!", Toast.LENGTH_SHORT).show();
                    initUserComponents(); //Todo: pass user's displayname
                } else {
                    // User is signed out
                    destroyUserComponents();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                            ))
                                    .build(),
                            RC_SIGN_IN);
                }
                // ...
            }
        };



        // Init UI/Resources
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

    private void initUserComponents() {
        //set Username
        attachDBRefListener();
        // display messages (attach database ref listener)
    }

    private void destroyUserComponents() {
        // unset username
        // clear adapter
        // detach dbRef listener
    }

    private void attachDBRefListener() {
        //if childEventListener == null
            //create/attach listener

    }

    private void detachDBRefListener() {
        //if eventListener != null
            //dbRef.removeEventListener(mChildEventListener);
            // set to null
    }
}
