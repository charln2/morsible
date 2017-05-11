package com.charln2.morsible;

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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "My_Audio";
    private static final int RC_SIGN_IN = 1;
    //UI/ Resources
    private MediaPlayer mp;
    private Button b;
    private boolean buttonActive;
    AudioManager am;
    AudioManager.OnAudioFocusChangeListener amFocusChangeListener;
    //todo: username, ListView, Adapter

    //Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference mRootRef;
    DatabaseReference mConditonRef;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Firebase Components
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mConditonRef = mRootRef.child("condition");

        // Init Listeners
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Already signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(getApplicationContext(), "Signed in!", Toast.LENGTH_SHORT).show();
                    initUserComponents(); //Todo: pass user's displayname
                } else {
                    // User is signed out
                    destroyUserComponents();
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Toast.makeText(getApplicationContext(), "onAuthStateChanged user == null! signedout!", Toast.LENGTH_SHORT).show();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                    ))
//                                    .setProviders(AuthUI.EMAIL_PROVIDER,
//                                            AuthUI.GOOGLE_PROVIDER)
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
        b = (Button) findViewById(R.id.button);
        buttonActive = false;
//                            b.setHighlightColor(Color.CYAN);

        //todo: prevent hiccup by resetting clip when reaching end or looping somehow.
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                          mp = MediaPlayer.create(MainActivity.this, R.raw.tone_600hz);
                        // todo: find better method for reassigning tone after release
                        if (mp==null) {
                            Log.v(TAG, "Oh no mp is null!... but we got this!");
                            mp = MediaPlayer.create(MainActivity.this, R.raw.tone_600hz);
                        }
                        if (!mp.isPlaying()) {
                            if (requestAudioFocus()) {
                                Log.v(TAG, "AUDIOFOCUS_GAIN GRANTED, starting...");
                                mp.start();
                                mConditonRef.setValue(true);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mp.pause();
                        mConditonRef.setValue(false);
//                            releaseMediaPlayer();
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
        mConditonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean b = dataSnapshot.getValue(Boolean.class);
                buttonActive = b;
                setButtonText(Boolean.toString(b));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        acquireMediaPlayer();
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

    private void _log(String str) {
        Log.d(TAG, str);
    }

    private void setButtonText(String s) {
        b.setText(s);
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
