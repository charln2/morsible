package com.charln2.morsible;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 1;
    private final int BORDER_WIDTH = 16;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRootRef;
    private DatabaseReference mSessionRef;
    private DatabaseReference mUserRef;

    private ValueEventListener mValueEventListener;
    private ChildEventListener mChildEventListener;
    //UI/ Resources
    private MediaPlayer mp;
    private int cloudSoundId;
    private Button b;
    private TextView mActiveTextView;
    private TextView mNameTextView;
    private GradientDrawable gd;
    //todo: username
    List<User> userBoxes = new ArrayList<>();
    private ListView mUserListView;
    private UserAdapter mUserAdapter;
    private User mUser;
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
        mSessionRef = mRootRef.child("session");
//        mSessionRef.setValue(new User()); // worked!

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // Already signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(getApplicationContext(), "Signed in!", Toast.LENGTH_SHORT).show();
                    onSignedInInit(user.getDisplayName()); //Todo: pass user's displayname
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
//        mp = MediaPlayer.create(this, mUser.getSoundId());
//        mp.start();
//        mp.setLooping(true);
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        amFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.v(TAG, "AUDIOFOCUS_GAIN");
                    if (requestAudioFocus()) {
                        mp.start();
                    }
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
        mUser = new User();
        mUserListView = (ListView) findViewById(R.id.userListView);
        mUserAdapter = new UserAdapter(this, R.layout.item_user, userBoxes);
        mUserListView.setAdapter(mUserAdapter);
        mNameTextView = (TextView) findViewById(R.id.nameTextView);
        b = (Button) findViewById(R.id.button);
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        _log("ACTION_DOWN");
                        mUser.setButtonActivated(true);
                        mUserRef.setValue(mUser);
//                        mp.start();
                        break;
                    case MotionEvent.ACTION_UP:
                        _log("ACTION_UP");
                        mUser.setButtonActivated(false);
                        mUserRef.setValue(mUser);
//                        mp.pause();
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

        // remove user from db
        mUserRef.removeValue();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }

    private void onSignedInInit(String userName) {
        // set username
        mUser.setUserName(userName);
//        mNameTextView.setText(userName);
        mUserRef = mSessionRef.child(userName);
        mUserRef.setValue(mUser);
//        mUserAdapter.clear();
        //push this user onto db
        attachDBRefListener();
//        mSessionRef.setValue(new User());
        // clear adapter
    }

    private void onSignedOutCleanup() {
        // unset username

        detachDBRefListener();
    }

    private void attachDBRefListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    User aUser = dataSnapshot.getValue(User.class);
//                    mUserAdapter.clear();
                    mUserAdapter.add(aUser);
                    mUserAdapter.notifyDataSetChanged();
//                    rebuildArrayAdapter(dataSnapshot);
                }
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    User aUser = dataSnapshot.getValue(User.class);
                    mUserAdapter.remove(aUser);
//                    rebuildArrayAdapter();
                }
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    User u = dataSnapshot.getValue(User.class);
                    _log(u.toString());
//                    if (mp==null || t.getSoundId() != mUser.getSoundId()) {
//                        mp = MediaPlayer.create(MainActivity.this, t.getSoundId());
//                    }
                    int i = mUserAdapter.getPosition(u);
                    mUserAdapter.getItem(i).updateValues(u);
//                    mUserAdapter.insert(u, i);
//                    mUserAdapter.remove(u);
                    mUserAdapter.notifyDataSetChanged();
//                    mUserListView.invalidateViews();
                    if (u.isButtonActivated()) {
//                        _makeToast("isActive");
//                        _log("isActive");

//                        acquireMediaPlayer();
//                        if (requestAudioFocus()) {
//                            mp.start();
//                        }
//        mActiveTextView = (TextView) findViewById(R.id.textview);
//                        int pos = mUserAdapter.getPosition(u);
//                        View curView = mUserAdapter.getItem(pos);
//                        gd = (GradientDrawable) mActiveTextView.getBackground();
//                        setBorderColor(Color.parseColor(u.getHighlightColor()));

                        //get received sound if null or different

//                        if (!mp.isPlaying()) {
//                            if (requestAudioFocus()) {
//                                Log.v(TAG, "AUDIOFOCUS_GAIN GRANTED, starting...");
//                                mp.start();
//                                mConditonRef.setValue(true);
//                            }
//                        }
                    } else {
//                        mUserAdapter.notifyDataSetChanged();

//                        _makeToast("notActive");
//                        _log("notActive");
//                        setBorderColor(R.color.colorBorderDefault);
//                        if (mp.isPlaying()) { // I don't know why this fixes it.
//                            mp.pause();
//                        }
                    }
//                    mUserAdapter.notifyDataSetChanged();

                }
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mSessionRef.addChildEventListener(mChildEventListener);
        }
//        if (mValueEventListener == null) {
//            mValueEventListener = new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    User t = dataSnapshot.getValue(User.class);
//                    _log(t.toString());
////                    if (mp==null || t.getSoundId() != mUser.getSoundId()) {
////                        mp = MediaPlayer.create(MainActivity.this, t.getSoundId());
////                    }
//                    if (t.isButtonActivated()) {
//                        _makeToast("isActive");
//                        _log("isActive");
////                        acquireMediaPlayer();
//                        if (requestAudioFocus()) {
//                            mp.start();
//                        }
////                        setBorderColor(Color.parseColor(t.getHighlightColor()));
//
//                        //get received sound if null or different
//
////                        if (!mp.isPlaying()) {
////                            if (requestAudioFocus()) {
////                                Log.v(TAG, "AUDIOFOCUS_GAIN GRANTED, starting...");
////                                mp.start();
////                                mConditonRef.setValue(true);
////                            }
////                        }
//                    } else {
////                        _makeToast("notActive");
////                        _log("notActive");
////                        setBorderColor(R.color.colorBorderDefault);
//                        if (mp.isPlaying()) { // I don't know why this fixes it.
//                            mp.pause();
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    _log("OnCancelled");
//                }
//            };
//            mSessionRef.addValueEventListener(mValueEventListener);
//        }
    }

    private void detachDBRefListener() {
        if (mChildEventListener != null) {
            mSessionRef.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
//        if (mValueEventListener != null) {
//            mSessionRef.removeEventListener(mValueEventListener);
//            mValueEventListener = null;
//        }
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
            mp = MediaPlayer.create(MainActivity.this, mUser.getSoundId());
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
