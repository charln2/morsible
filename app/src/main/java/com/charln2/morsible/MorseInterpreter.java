package com.charln2.morsible;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by charl on 10/6/2017.
 */

public class MorseInterpreter {
    private static final int BORDER_WIDTH = 16;

    public Activity activityMain; // to grab UI and Firebase elements
    private User mUser;
    // Todo: button logic
    private Button b;

    // Todo: countdown timer logic
    private StringBuffer sb; // array to store touch input

    public MorseInterpreter(Activity _activity, User user) { // User, fbRef
        this.activityMain = _activity;
        this.mUser = user;

        sb = new StringBuffer();
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
                        break;
                    case MotionEvent.ACTION_UP:
                        mUser.setButtonActivated(false); // disable tone
                        mUser.pushToFB();
                        gd.setStroke(BORDER_WIDTH,
                                ContextCompat.getColor(getApplicationContext(),
                                        R.color.tw__transparent)); // reset border indicateorcolor
                        break;
                }
                return false;
            }
        });
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
