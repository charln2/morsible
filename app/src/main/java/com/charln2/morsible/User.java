package com.charln2.morsible;

import android.app.Activity;
import android.graphics.Color;

import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

import retrofit2.http.HEAD;

import static android.graphics.Color.CYAN;

public class User {
    private String userName;
    private String key;
    private boolean isButtonActivated;
    private int soundId;
    private String highlightColor;
    private MorseInterpreter morseInterpreter;
    private DatabaseReference dbRef;

    private User(){} // do not allow instantiation of empty constructor
    public User(Activity _activity) {
        morseInterpreter = new MorseInterpreter(_activity, this);
        isButtonActivated = false;
        soundId = R.raw.tone_600hz;
        highlightColor = "#FFAA77";
    }
    public User(Activity _activity, DatabaseReference dbRef) {
        this(_activity);
        this.dbRef = dbRef;
    }

    public User(String key, boolean isButtonActivated, int soundId, String highlightColor) {
        userName = "Anonymous";
        this.key = key;
        this.isButtonActivated = isButtonActivated;
        this.soundId = soundId;
        this.highlightColor = highlightColor;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
    }
    public void pushToFB() {
        dbRef.setValue(this); // update db val
    }

    public void setDBRef(DatabaseReference dbRef) {
        this.dbRef = dbRef;
        pushToFB();

    }
    @Override
    public String toString() {
        return "[user:" + userName +
                "isActive:" + isButtonActivated +
                " soundId:" + soundId +
                " highlightColor:" + highlightColor + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return this.userName.equals(((User) obj).getUserName());
    }
}
