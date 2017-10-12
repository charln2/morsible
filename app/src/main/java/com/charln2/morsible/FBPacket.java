package com.charln2.morsible;

import android.app.Activity;
import android.support.v4.content.ContextCompat;

/**
 * Created by charl on 10/12/2017.
 */

public class FBPacket {
    private String userName;
    private boolean buttonActivated;
    private String highlightColor;
    private int soundId;
    private String message;
    private String key;

    public FBPacket() {
        userName = "Unknown";
        soundId = R.raw.tone_600hz;
        message = "";
        key = "";
    }

    public FBPacket(Activity _activity) {
        this();
        highlightColor = "#" + Integer.toHexString(ContextCompat.getColor(_activity.getApplicationContext(), R.color.warmYellow));
    }

    public FBPacket(boolean buttonActivated, String highlightColor, int soundId, String message, String key) {
        this();
        this.buttonActivated = buttonActivated;
        this.highlightColor = highlightColor;
        this.soundId = soundId;
        this.message = message;
        this.key = key;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isButtonActivated() {
        return buttonActivated;
    }

    public void setButtonActivated(boolean buttonActivated) {
        this.buttonActivated = buttonActivated;
    }

    public String getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
    }

    public int getSoundId() {
        return soundId;
    }

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void updateValues(FBPacket fbPack) {
        this.userName = fbPack.userName;
        this.buttonActivated = fbPack.buttonActivated;
        this.highlightColor = fbPack.highlightColor;
        this.soundId = fbPack.soundId;
        this.message = fbPack.message;
        this.key = fbPack.key;
    }

}
