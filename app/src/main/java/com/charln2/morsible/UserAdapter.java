package com.charln2.morsible;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.R.id.message;

public class UserAdapter extends ArrayAdapter<User> {
    private static final int BORDER_WIDTH = 16;
    private final String TAG = "UserAdapter";

    public UserAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_user, parent, false);
        }

        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);

        User userBlock = getItem(position);
        GradientDrawable gd = (GradientDrawable) convertView.getBackground();
        if (userBlock.isButtonActivated()) {
            Log.d(TAG, "changing view color of user to active");
            gd.setStroke(BORDER_WIDTH, Color.parseColor(userBlock.getHighlightColor()));
        } else if (!userBlock.isButtonActivated()) {
            Log.d(TAG, "changing view color of user to inactive");
            gd.setStroke(BORDER_WIDTH, ContextCompat.getColor(getContext(), R.color.colorBorderDefault));
        }
//        messageTextView.setText(message.getText());
        nameTextView.setText(userBlock.getUserName());

        return convertView;
    }
}