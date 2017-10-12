package com.charln2.morsible;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

public class User {
    private static final String LOG_TAG = User.class.getSimpleName();

    private String userName;
    private String key;
    private boolean isButtonActivated;
    private int soundId;
    private String highlightColor;
    private MorseInterpreter morseInterpreter;
    private DatabaseReference mUserRef;
    private StringBuilder message;
    private String str;

    private User() {
    } // do not allow instantiation of empty constructor

    public User(Activity _activity) {
        morseInterpreter = new MorseInterpreter(_activity, this);
        isButtonActivated = false;
        soundId = R.raw.tone_600hz;
        highlightColor = "#FFAA77";
        message = new StringBuilder();
    }

    public User(Activity _activity, DatabaseReference dbRef) {
        this(_activity);
        this.mUserRef = dbRef;
    }

    // copy constructor
    public User(String key, boolean isButtonActivated, int soundId, String highlightColor, StringBuilder message) {
        userName = "Anonymous";
        this.key = key;
        this.isButtonActivated = isButtonActivated;
        this.soundId = soundId;
        this.highlightColor = highlightColor;
        this.message = message;
        str = message.toString();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isButtonActivated() {
        return isButtonActivated;
    }

    public void setButtonActivated(boolean buttonActivated) {
        isButtonActivated = buttonActivated;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public String getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
    }

    public void updateValues(User u) {
        this.isButtonActivated = u.isButtonActivated;
        this.soundId = u.soundId;
        this.highlightColor = u.highlightColor;
        //todo: message buffer
        this.message = u.message;
    }

    public void pushToFB() {
        mUserRef.setValue(this); // update db val
        Log.v("morsebuffer", message.toString());
    }

    public void setDBRef(DatabaseReference dbRef) {
        mUserRef = dbRef;
        pushToFB();
    }

    public void append(String ch) {
        message.append(ch);
        pushToFB();
    }

    @Override
    public String toString() {
        return "[user:" + userName +
                "isActive:" + isButtonActivated +
                " soundId:" + soundId +
                " highlightColor:" + highlightColor +
                " message: " + message + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return this.userName.equals(((User) obj).getUserName());
    }

    public void clearBuffer() {
        message.setLength(0);
    }
}
