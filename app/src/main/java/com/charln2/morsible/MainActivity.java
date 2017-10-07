package com.charln2.morsible;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
    private static final String LOG_TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;

    //TODO: move logic to Layout
    private static final int BORDER_WIDTH = 16;
    List<User> users = new ArrayList<>();
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRootRef;
    private DatabaseReference mSessionRef;
    private DatabaseReference mUserRef;
    private ChildEventListener mChildEventListener;
    //UI
    private User mUser; // user display
    private Button b;
    private ListView mUserListView;
    private UserAdapter mUserAdapter;
    // Audio
    private MediaPlayer mp;
    private AudioManager am;
    private AudioManager.OnAudioFocusChangeListener amFocusChangeListener;

    /**
     * Initializes all necessary components for a user's session
     *
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Database
        mDatabase = FirebaseDatabase.getInstance();
        mRootRef = mDatabase.getReference();
        mSessionRef = mRootRef.child("session");

        // Authorization
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Already signed in
                    onSignedInInit(user.getDisplayName());
                } else {
                    // User is signed out
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

        // Audio
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

        // UI
        // TODO: Move logic to User object
        mUser = new User();
        mUserListView = (ListView) findViewById(R.id.userListView);
        mUserAdapter = new UserAdapter(this, R.layout.item_user, users);
        mUserListView.setAdapter(mUserAdapter);
        b = (Button) findViewById(R.id.button);
        final GradientDrawable gd = (GradientDrawable) b.getBackground();
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mUser.setButtonActivated(true);
                        mUserRef.setValue(mUser);
                        gd.setStroke(BORDER_WIDTH, Color.parseColor(mUser.getHighlightColor()));
                        break;
                    case MotionEvent.ACTION_UP:
                        mUser.setButtonActivated(false);
                        mUserRef.setValue(mUser);
                        gd.setStroke(BORDER_WIDTH,
                                ContextCompat.getColor(getApplicationContext(), R.color.tw__transparent));
                        break;
                }
                return false;
            }
        });
    }


    // ==== ANDROID LIFECYCLE OVERRIDES ====
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                //Signed In
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
        acquireMediaPlayer();
        mUserAdapter.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
//            mAuthListener = null; DON'T DO THIS
        }
        detachDBRefListener();
        if (mUserRef != null)
            mUserRef.removeValue();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    // ____ ANDROID LIFECYCLE OVERRIDES ____

    private void onSignedInInit(String userName) {
        mUser.setUserName(userName);
        mUserRef = mSessionRef.child(userName);
        mUserRef.setValue(mUser);
        attachDBRefListener();
    }

    private void onSignedOutCleanup() {
        detachDBRefListener();
        if (mUserRef != null)
            mUserRef.removeValue();
    }

    private void attachDBRefListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    User aUser = dataSnapshot.getValue(User.class);
                    mUserAdapter.add(aUser);
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    User aUser = dataSnapshot.getValue(User.class);
                    mUserAdapter.remove(aUser);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    User aUser = dataSnapshot.getValue(User.class);
                    int i = mUserAdapter.getPosition(aUser);
                    mUserAdapter.getItem(i).updateValues(aUser);
                    mUserAdapter.notifyDataSetChanged();
                    if (aUser.isButtonActivated()) {
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        AuthUI.getInstance().signOut(this);
    }
}
