package com.example.mlallemant.mentalbattle.UI.Friends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;

import java.util.ArrayList;

/**
 * Created by m.lallemant on 13/11/2017.
 */

public class FriendSearchAdapter extends ArrayAdapter<FriendSearchModel> implements View.OnClickListener {

    private final ArrayList<FriendSearchModel> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView username;
    }

    public FriendSearchAdapter(final ArrayList<FriendSearchModel> data, final Context context) {
        super(context, R.layout.friend_search_row_item_template, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(final View v) {
        final int position = (Integer) v.getTag();
        final Object object = getItem(position);

        final FriendSearchModel friendSearchModel = (FriendSearchModel) object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // Get the data item for this position
        final FriendSearchModel friendSearchModel = getItem(position);

        //Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder;

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.friend_search_row_item_template, parent, false);

            viewHolder.username = (TextView) convertView.findViewById(R.id.friend_search_tv_username);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        if (friendSearchModel != null) {
            viewHolder.username.setText(friendSearchModel.player.getName());

        }
        return convertView;
    }
}
