package com.charln2.morsible;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName()+"text";
    private static final int RC_SIGN_IN = 1;

    //Firebase
    private DatabaseReference mSessionRef;
    private DatabaseReference mUserRef;
    private ChildEventListener mChildEventListener;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //UI
    List<FBPacket> users = new ArrayList<>();
    private User mUser; // user of application
    private ListView mUserListView;
    private FBPacketAdapter mPacketAdapter;

    // Audio
    private MediaPlayer mp;
    private AudioManager am;
    private AudioManager.OnAudioFocusChangeListener amFocusChangeListener;


    // ==== INIT ====
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Database session
        mSessionRef = FirebaseDatabase.getInstance().getReference().child("session");

        Log.v(LOG_TAG, "got session ref");
        // Authentication
        initAuthentication();

        // Audio
        initAudio();
        // UI content
        mUserListView = (ListView) findViewById(R.id.userListView);
        mPacketAdapter = new FBPacketAdapter(this, R.layout.item_user, users);
        mUserListView.setAdapter(mPacketAdapter);

        mUser = new User(this, mSessionRef, mPacketAdapter);
    }

    /**
     * Logic for app authentication. Initializes FirebaseAuth and FirebaseAuth.AuthStateListener
     * objects.
     */
    private void initAuthentication() {
        Log.v(LOG_TAG,"authenticating");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Already signed in
                    Log.v(LOG_TAG, "signed in");
                    onSignedInInit(user.getDisplayName());
                } else {
                    // User is signed out
                    Log.v(LOG_TAG, "signed out");
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                    ))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    /**
     * Sets up the app for the logged in user, giving one db reference to the User object.
     * @param userName
     */
    private void onSignedInInit(String userName) {
        Log.v(LOG_TAG, "onSignedInInit");
        mUser.setUserName(userName);
        Log.v(LOG_TAG, "user = " + mUser.toString());
//        mUserRef = mSessionRef.child(userName);
//        mUser.setDBRef(mUserRef);
        attachDBRefListener();
    }

    private void onSignedOutCleanup() {
        Log.v(LOG_TAG, "onSignedOutCleanup");
        detachDBRefListener();
        mUser.removeFBRef();
    }

    private void initAudio() {
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        amFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (requestAudioFocus()) {
                            mp.start();
                        }
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        releaseMediaPlayer();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        mp.pause();
                        mp.seekTo(0);
                        break;
                    default:
                }
            }
        };
    }

    private boolean requestAudioFocus() {
        return am.requestAudioFocus(amFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void releaseMediaPlayer() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
        am.abandonAudioFocus(amFocusChangeListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Signed In
            } else if (resultCode == RESULT_CANCELED) {
                // Close application
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPacketAdapter.clear();
        mAuth.addAuthStateListener(mAuthListener);
//        attachDBRefListener();
        acquireMediaPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
//            mAuthListener = null; DON'T DO THIS
        }
        detachDBRefListener();
        mUser.removeFBRef();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void attachDBRefListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FBPacket fbPack = dataSnapshot.getValue(FBPacket.class);
                    mPacketAdapter.add(fbPack);
                    mPacketAdapter.notifyDataSetChanged();
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    FBPacket fbPack = dataSnapshot.getValue(FBPacket.class);
                    mPacketAdapter.remove(fbPack);
                    mPacketAdapter.notifyDataSetChanged();
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    FBPacket fbPack = dataSnapshot.getValue(FBPacket.class);
                    int i = mPacketAdapter.getPosition(fbPack);
                    if (i > -1) {
                        mPacketAdapter.getItem(i).updateValues(fbPack);
                        mPacketAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(LOG_TAG, "Trouble retrieving child user from database");
                    }

                    // Play tone if updated user button is active
                    if (fbPack.isButtonActivated()) {
                        acquireMediaPlayer();
                        if (requestAudioFocus()) {
                            mp.start();
                        }
                    } else {
                        if (mp.isPlaying()) { // I don't know why this fixes it.
                            mp.pause();
                        }
                    }
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mSessionRef.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDBRefListener() {
        if (mChildEventListener != null) {
            mSessionRef.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void acquireMediaPlayer() {
        if (mp == null) {
            mp = MediaPlayer.create(MainActivity.this, mUser.getSoundId());
        }
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
            case R.id.clear:
                mUser.clearBuffer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        AuthUI.getInstance().signOut(this);
    }
}
