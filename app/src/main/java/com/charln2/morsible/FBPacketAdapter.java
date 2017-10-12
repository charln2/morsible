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
import android.widget.TextView;

import java.util.List;

public class FBPacketAdapter extends ArrayAdapter<FBPacket> {
    private static final int BORDER_WIDTH = 16;
    private final String TAG = "FBPacketAdapter";

    public FBPacketAdapter(Context context, int resource, List<FBPacket> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_user, parent, false);
        }

        TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);

        FBPacket fbPacket = getItem(position);
        GradientDrawable gd = (GradientDrawable) convertView.getBackground();
        if (fbPacket.isButtonActivated()) {
//            Log.d(TAG, "changing view color of user to active");
            gd.setStroke(BORDER_WIDTH, Color.parseColor(fbPacket.getHighlightColor()));
        } else if (!fbPacket.isButtonActivated()) {
//            Log.d(TAG, "changing view color of user to inactive");
            gd.setStroke(BORDER_WIDTH,
                    ContextCompat.getColor(getContext(), R.color.colorBorderDefault));
        }
        messageTextView.setText(fbPacket.getMessage().trim());
        nameTextView.setText(fbPacket.getUserName());
//        notifyDataSetChanged();
        return convertView;
    }
}