package com.charln2.morsible;

import android.graphics.Color;

import static android.graphics.Color.CYAN;

/**
 * Created by charl on 5/11/2017.
 */

public class Tone {
    private boolean isButtonActivated;
    private int soundId;
    private String highlightColor;

    public Tone() {
        isButtonActivated = false;
        soundId = R.raw.tone_600hz;
        highlightColor = "#4FA5D5";
    }

    public Tone(boolean buttonActive, int soundId, String highlightColor) {
        this.isButtonActivated = buttonActive;
        this.soundId = soundId;
        this.highlightColor = highlightColor;
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

    @Override
    public String toString() {
        return "[isActive:" + isButtonActivated +
                " soundId:" + soundId +
                " highlightColor:" + highlightColor + "]";
    }
}
