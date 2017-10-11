package com.charln2.morsible;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by charl on 10/6/2017.
 */

public class MorseInterpreter {
    private final String LOG_TAG = getClass().getSimpleName();

    private static final int BORDER_WIDTH = 16;
    private final int DOT_DURATION = 50; // milliseconds
    private final int DASH_DURATION = DOT_DURATION * 3; // milliseconds
    private final int LETTER_DURATION = DOT_DURATION * 3; // milliseconds
    private final int SPACE_DURATION = DOT_DURATION * 7; // milliseconds

    private long lastStartTime;
    private long lastEndTime;
    private HashMap<String, Character> morseDict;
    private StringBuilder workingChar; // stores dots and dashes to be parsed into chars


    public Activity activityMain; // to grab UI and Firebase elements

    private User mUser;
    // Todo: button logic
    private Button b;

    public MorseInterpreter(Activity _activity, User user) { // User, fbRef
        this.activityMain = _activity;
        this.mUser = user;

        buildMorseDict();
        b = (Button) activityMain.findViewById(R.id.button);
        final GradientDrawable gd = (GradientDrawable) b.getBackground();
        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        mUser.setButtonActivated(true); // queue tone
                        mUser.pushToFB();
                        gd.setStroke(BORDER_WIDTH, Color.parseColor(mUser.getHighlightColor())); //change

                        // todo: parse button input via timer, append to sb, update FB val
                        lastStartTime = System.currentTimeMillis();
                        Log.v(LOG_TAG, "lastStartTime currentTimeMillis: " + lastStartTime);
                        break;
                    case MotionEvent.ACTION_UP:
                        mUser.setButtonActivated(false); // disable tone
                        mUser.pushToFB();
                        gd.setStroke(BORDER_WIDTH,
                                ContextCompat.getColor(getApplicationContext(),
                                        R.color.tw__transparent)); // reset border indicateorcolor

//                        if (lastEndTime != null) {
//
//                        }
                        long timeIdle = parseTimeIdle(); // check letter, check space,
                        if(timeIdle <= LETTER_DURATION) {

                        }
                        if(timeIdle <= SPACE_DURATION) {

                        }
                        lastEndTime = System.currentTimeMillis();
                        break;
                }

                return false;
            }
        });
    }

    private void buildMorseDict() {
        morseDict = new HashMap<>();
        try {
            InputStream is = getApplicationContext().getAssets().open("morsetranslation.txt");
            Scanner sc = new Scanner(is);

            while (sc.hasNextLine()) {
                String[] line = sc.nextLine().split("\t", -1);
                morseDict.put(line[1], line[0].charAt(0));
            }

            sc.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error opening morse dictionary");
            e.printStackTrace();
        }

        for(Map.Entry<String, Character> entry : morseDict.entrySet()) {
            String str = String.format("%-6s: %s", entry.getKey(), entry.getValue());
            Log.v(LOG_TAG, str);
//            Log.v(LOG_TAG, "" + morseDict.size());
        }

    }

    private long parseTimeIdle() {
        return (lastEndTime != 0) ? lastEndTime - lastStartTime : 0;
    }

    public void append(char ch) {
        //todo: append ".", "_", or "\b"
    }

    private void updateDisplay() {

    }

    public void clear() {
        // Todo: clear buffer
        // Todo: update Firebase? (might be automatic)
    }
}
