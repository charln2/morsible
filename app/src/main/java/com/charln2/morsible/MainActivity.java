package com.charln2.morsible;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;
    private final int BORDER_WIDTH = 16;
    FirebaseDatabase mDatabase;
    DatabaseReference mRootRef;
    DatabaseReference mToneRef;
    ValueEventListener mValueEventListener;
    //UI/ Resources
    private MediaPlayer mp;
    private Button b;
    private TextView tv;
    private GradientDrawable gd;
    //todo: username, ListView, Adapter
    private Tone mTone;
    private AudioManager am;
    private AudioManager.OnAudioFocusChangeListener amFocusChangeListener;
    //Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Firebase Components
        mDatabase = FirebaseDatabase.getInstance();
        mRootRef = mDatabase.getReference();
        mToneRef = mRootRef.child("tone");
//        mToneRef.setValue(new Tone()); // worked!

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Already signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(getApplicationContext(), "Signed in!", Toast.LENGTH_SHORT).show();
                    onSignedInInit(); //Todo: pass user's displayname
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(getApplicationContext(), "onAuthStateChanged user == null! signedout!", Toast.LENGTH_SHORT).show();
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
            }
        };

        // Init UI/Resources
        mp = MediaPlayer.create(this, R.raw.tone_600hz);
        mp.setLooping(true);
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        amFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.v(TAG, "AUDIOFOCUS_GAIN");
                        mp.start();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.v(TAG, "AUDIOFOCUS_LOSS");
                        releaseMediaPlayer();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        Log.v(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.v(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        mp.pause();
                        mp.seekTo(0);
                        break;
                    default:
                }
            }
        };
        mTone = new Tone();
        b = (Button) findViewById(R.id.button);
        tv = (TextView) findViewById(R.id.textview);
        gd = (GradientDrawable) tv.getBackground();
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        _log("ACTION_DOWN");
                        mTone.setButtonActivated(true);
                        mToneRef.setValue(mTone);
                        break;
                    case MotionEvent.ACTION_UP:
                        _log("ACTION_UP");
                        mTone.setButtonActivated(false);
                        mToneRef.setValue(mTone);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: made it inside!\n RequestCode = " + requestCode + "\nresultCode =" + resultCode);
        _makeToast("onActivityResult: inside");
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: result ok!");
                Toast.makeText(MainActivity.this, "Signed in on Result!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: result cancelled!");
                Toast.makeText(MainActivity.this, "Sign-in Cancelled on Result!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
        acquireMediaPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        // clear message adapter, if applicable
        detachDBRefListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void onSignedInInit() {
        //set Username
        attachDBRefListener();
        mToneRef.setValue(new Tone());
    }

    private void onSignedOutCleanup() {
        // unset username
        // clear adapter
        detachDBRefListener();
    }

    private void attachDBRefListener() {
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Tone t = dataSnapshot.getValue(Tone.class);
                    _log(t.toString());
                    if (t.isButtonActivated()) {
                        _makeToast("isActive");
                        _log("isActive");
                        setBorderColor(Color.parseColor(t.getHighlightColor()));
                        //                          mp = MediaPlayer.create(MainActivity.this, R.raw.tone_600hz);
                        //                        // todo: find better method for reassigning tone after release
                        //                        if (mp==null) {
                        //                            Log.v(TAG, "Oh no mp is null!... but we got this!");
                        //                            mp = MediaPlayer.create(MainActivity.this, R.raw.tone_600hz);
                        //                        }
                        //                        if (!mp.isPlaying()) {
                        //                            if (requestAudioFocus()) {
                        //                                Log.v(TAG, "AUDIOFOCUS_GAIN GRANTED, starting...");
                        //                                mp.start();
                        ////                                mConditonRef.setValue(true);
                        //                            }
                        //                        }

                    } else {
                        _makeToast("notActive");
                        _log("notActive");
                        setBorderColor(R.color.colorBorderDefault);
                        //                        mp.pause();
                        //                            releaseMediaPlayer();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    _log("OnCancelled");
                }
            };
            mToneRef.addValueEventListener(mValueEventListener);
        }
    }

    private void detachDBRefListener() {
        if (mValueEventListener != null) {
            mToneRef.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    private void _log(String str) {
        Log.d(TAG, str);
    }

    private void setButtonText(String s) {
        b.setText(s);
    }

    private void setBorderColor(int colorHex) {
        gd.setStroke(BORDER_WIDTH, colorHex); // set stroke width and stroke color
    }

    private boolean requestAudioFocus() {
        return am.requestAudioFocus(amFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)

                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void releaseMediaPlayer() {
        Log.v(TAG, "I, the media player, am being released!");
        if (mp != null) {
            mp.release();
            mp = null;
        }
        Log.v(TAG, "I'm the auudiofocuschangelistener and I'm going, too!");
        am.abandonAudioFocus(amFocusChangeListener);
    }

    private void acquireMediaPlayer() {
        if (mp == null) {
            mp = MediaPlayer.create(MainActivity.this, R.raw.tone_600hz);
        }
    }

    private void _makeToast(String s) {
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    // OptionsMenu
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
        AuthUI.getInstance().signOut(this);
    }
}
