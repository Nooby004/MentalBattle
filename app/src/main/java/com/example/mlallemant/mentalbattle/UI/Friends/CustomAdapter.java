package com.example.mlallemant.mentalbattle.UI.Friends;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by m.lallemant on 30/10/2017.
 */

public class CustomAdapter extends ArrayAdapter<DataModel> implements View.OnClickListener {

    private ArrayList<DataModel> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView username;
        ImageView connect;
    }

    public CustomAdapter (ArrayList<DataModel> data, Context context){
        super(context, R.layout.row_item_template, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v){
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        DataModel dataModel = (DataModel) object;

        makeToast("click on " + dataModel.player.getName());
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        // Get the data item for this position
        DataModel dataModel = getItem(position);

        //Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;

        final View result;

        if (convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item_template, parent, false);

            viewHolder.username = (TextView) convertView.findViewById(R.id.row_item_tv_username);
            viewHolder.connect = (ImageView) convertView.findViewById(R.id.row_item_iv_connect);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        if (dataModel != null){
            if (dataModel.getPlayer() != null) viewHolder.username.setText(dataModel.getPlayer().getName());

            if (dataModel.getFriendAcq() != null) {
                if (dataModel.getFriendAcq().equals(Utils.ACK_OK)) {
                    if (dataModel.getConnected()) {
                        GradientDrawable gd = (GradientDrawable) viewHolder.connect.getBackground();
                        gd.setColor(ContextCompat.getColor(mContext, R.color.greenColor));
                    } else {
                        GradientDrawable gd = (GradientDrawable) viewHolder.connect.getBackground();
                        gd.setColor(ContextCompat.getColor(mContext, R.color.redColor));
                    }
                } else {
                    GradientDrawable gd = (GradientDrawable) viewHolder.connect.getBackground();
                    gd.setColor(ContextCompat.getColor(mContext, R.color.whiteGrayColor));
                }
            }
        }






        return convertView;
    }


    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }
}
