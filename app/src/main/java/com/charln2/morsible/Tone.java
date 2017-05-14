package com.charln2.morsible;

import android.graphics.Color;

import static android.graphics.Color.CYAN;

/**
 * Created by charl on 5/11/2017.
 */

public class Tone {
    private boolean isButtonActivated;
    private int soundId;
    private int highlightColor;

    public Tone() {
        isButtonActivated = false;
        soundId = R.raw.tone_600hz;
        highlightColor = Color.CYAN;
    }

    public Tone(boolean buttonActive, int soundId, int highlightColor) {
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

    public void setSoundId(int soundId) {
        this.soundId = soundId;
    }

    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
    }

    public boolean isActive() {
        return isButtonActivated;
    }

    public int getSoundId() {
        return soundId;
    }

    public int getHighlightColor() {
        return highlightColor;
    }

    @Override
    public String toString() {
        return "[isActive:"+isButtonActivated+
                " soundId:"+soundId+
                " highlightColor:"+highlightColor+"]";
    }
}
