package com.charln2.morsible;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by charl on 10/6/2017.
 */

public class MorseInterpreter {
    private final String LOG_TAG = getClass().getSimpleName();

    private static final int BORDER_WIDTH = 16;
//    private final int DOT_DURATION = 50; // milliseconds
    private final int DASH_TIMEOUT = 200;//DOT_DURATION * 3; // milliseconds
    private final int CHAR_TIMEOUT = (int) Math.round(DASH_TIMEOUT * 1.12); //DOT_DURATION * 3; // milliseconds
    private final int SPACE_TIMEOUT = (int) Math.round(DASH_TIMEOUT * 4); // milliseconds
    private CountDownTimer charCountdown;

    private long startTime;
    private long endTime;
    private HashMap<String, Character> morseDict;
    private StringBuilder workingChar; // stores dots and dashes to be parsed into chars


    public Activity activityMain; // to grab UI and Firebase elements

    private User mUser;
    private Button b;

    public MorseInterpreter(Activity _activity, User user) { // User, fbRef
        this.activityMain = _activity;
        this.mUser = user;

        buildMorseDictionary();
        workingChar = new StringBuilder();
        charCountdown = new CountDownTimer(CHAR_TIMEOUT, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                char ch = '?';
                if (morseDict.containsKey(workingChar.toString())) {
                    ch = morseDict.get(workingChar.toString());
                }
                appendToMessage("" + ch);
                workingChar.setLength(0); // clear StringBuilder
            }
        };

        b = (Button) activityMain.findViewById(R.id.button);
        final GradientDrawable gd = (GradientDrawable) b.getBackground();

        b.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        // sound
                        mUser.setButtonActivated(true);

                        // glow
                        gd.setStroke(BORDER_WIDTH, Color.parseColor(mUser.getHighlightColor())); //change

                        // message
                        charCountdown.cancel();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        // sound
                        mUser.setButtonActivated(false);

                        // glow
                        gd.setStroke(BORDER_WIDTH,
                                ContextCompat.getColor(getApplicationContext(),
                                        R.color.tw__transparent)); // reset border indicateor color

                        // message
                        long endTimePrev = endTime;
                        endTime = System.currentTimeMillis();

                        // append space, if idle long enough
                        long idleTime = startTime - endTimePrev; // check letter, check space,
                        String space = "";
                        Log.v("morsebuffer", "idleTime: " + idleTime);
                        if (idleTime >= SPACE_TIMEOUT) {
                            appendToMessage(" ");
//                            space = " ";
                        }

                        // parse . or -
                        long touchTime = endTime - startTime;
                        if (touchTime >= DASH_TIMEOUT) {
                            workingChar.append('-');
                        } else {
                            workingChar.append('.');
                        }
                        Log.v("morsebuffer", "touchTime = " + touchTime);

                        // parse char from working sb via morseDictionary, if idle long enough
                        // countdown timer?
                        charCountdown.start();
                        if (idleTime >= CHAR_TIMEOUT) {
                        }
                        break;
                }
                mUser.pushToFB();
                return false;
            }
        });
    }

    private void buildMorseDictionary() {
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
//        for(Map.Entry<String, Character> entry : morseDict.entrySet()) {
//            String str = String.format("%-6s: %s", entry.getKey(), entry.getValue());
//            Log.v(LOG_TAG, str);
//            Log.v(LOG_TAG, "" + morseDict.size());
//        }
    }

    public void appendToMessage(String s) {
        mUser.append(s);
    }
}
