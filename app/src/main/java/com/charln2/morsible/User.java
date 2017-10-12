package com.charln2.morsible;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

public class User {
    private static final String LOG_TAG = User.class.getSimpleName();

    private FBPacket fbPacket;

    private MorseInterpreter morseInterpreter;
    private DatabaseReference mFBRef;

    private StringBuilder messageBuilder;

    private FBPacketAdapter adapter;
//    private String str;

    private User() {
    } // do not allow instantiation of empty constructor

    // Main Constructor
    public User(Activity _activity, DatabaseReference sessionRef, FBPacketAdapter adapter) {
        morseInterpreter = new MorseInterpreter(_activity, this);
        messageBuilder = new StringBuilder();
        this.adapter = adapter;
        this.fbPacket = new FBPacket(_activity);
        this.mFBRef = sessionRef.push();
    }

    // Copy constructor
    public User(User user) {
        this.fbPacket = new FBPacket(user.isButtonActivated(), user.getHighlightColor(), user.getSoundId(), user.getMessageBuilder(), user.getKey());
        this.fbPacket.setUserName(user.getUserName());
        messageBuilder = new StringBuilder();
    }

    public String getUserName() {
        return fbPacket.getUserName();
    }

    public void setUserName(String userName) {
        fbPacket.setUserName(userName);
    }

    public boolean isButtonActivated() {
        return fbPacket.isButtonActivated();
    }

    public void setButtonActivated(boolean buttonActivated) {
        fbPacket.setButtonActivated(buttonActivated);
    }

    public int getSoundId() {
        return fbPacket.getSoundId();
    }

    public void setSoundId(int soundId) {
        fbPacket.setSoundId(soundId);
    }

    public String getHighlightColor() {
        return fbPacket.getHighlightColor();
    }

    public void setHighlightColor(String highlightColor) {
        fbPacket.setHighlightColor(highlightColor);
    }

    public void setMessageBuilder(StringBuilder messageBuilder) {
        fbPacket.setMessage(messageBuilder.toString());
    }

    public String getMessageBuilder() {
        return fbPacket.getMessage();
    }

    public void updateValues(User u) {
        fbPacket.setButtonActivated(u.isButtonActivated());
        fbPacket.setSoundId(u.getSoundId());
        fbPacket.setHighlightColor(u.getHighlightColor());
        fbPacket.setMessage(u.getMessageBuilder());
        this.messageBuilder = u.messageBuilder;
    }

    public void pushToFB() {
        mFBRef.setValue(fbPacket); // update db val
        adapter.notifyDataSetChanged();
//        mFBRef.child("messageBuilder").setValue(messageBuilder);
        Log.v("morsebuffer", messageBuilder.toString());
    }

    public void removeFBRef() {
        if (mFBRef != null) {
            mFBRef.removeValue();
        }
    }
//    public void setDBRef(DatabaseReference dbRef) {
//        mFBRef = dbRef;
//        pushToFB();
//    }

    public void append(String ch) {
        messageBuilder.append(ch);
        fbPacket.setMessage(messageBuilder.toString());
        pushToFB();
    }

//    @Override
//    public String toString() {
//        return "[user:" + fbPacket.us +
//                "isActive:" + isButtonActivated +
//                " soundId:" + soundId +
//                " highlightColor:" + highlightColor +
//                " messageBuilder: " + messageBuilder + "]";
//    }

    @Override
    public boolean equals(Object obj) {
        return fbPacket.getUserName().equals(((User) obj).getUserName());
    }

    public void clearBuffer() {
        messageBuilder.setLength(0);
        fbPacket.setMessage("");
        pushToFB();
    }

    public String getKey() {
        return fbPacket.getKey();
    }
}
