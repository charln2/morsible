package com.charln2.morsible;

import android.graphics.Color;

import retrofit2.http.HEAD;

import static android.graphics.Color.CYAN;

/**
 * Created by charl on 5/11/2017.
 */

public class User {
    private String key;
    private boolean isButtonActivated;
    private int soundId;
    private String highlightColor;

    public User() {
        isButtonActivated = false;
        soundId = R.raw.tone_600hz;
        highlightColor = "#FFAA77";
    }

    public User(String key, boolean isButtonActivated, int soundId, String highlightColor) {
        this.key = key;
        this.isButtonActivated = isButtonActivated;
        this.soundId = soundId;
        this.highlightColor = highlightColor;
    }

    public String getKey() {
        return key;
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

    public void setKey(String key) {
        this.key = key;
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
    }

    @Override
    public String toString() {
        return "[isActive:" + isButtonActivated +
                " soundId:" + soundId +
                " highlightColor:" + highlightColor + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return this.key == ((User)obj).getKey();
    }
}
