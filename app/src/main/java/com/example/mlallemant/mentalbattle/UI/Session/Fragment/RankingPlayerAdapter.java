package com.example.mlallemant.mentalbattle.UI.Session.Fragment;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Player;

import java.util.ArrayList;

/**
 * Created by m.lallemant on 17/11/2017.
 */

public class RankingPlayerAdapter extends ArrayAdapter<Player> implements View.OnClickListener {

    private ArrayList<Player> dataSet;
    Context mContext;


    private static class ViewHolder {
        TextView tv_position;
        TextView tv_username;
        TextView tv_score;
    }

    public RankingPlayerAdapter(ArrayList<Player> data, Context context){
        super(context, R.layout.session_transition_row_item_template, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);

        Player rankingPlayerModel = (Player) object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Player rankingPlayerModel = getItem(position);
        final RankingPlayerAdapter.ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new RankingPlayerAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.session_transition_row_item_template, parent, false);

            viewHolder.tv_position = (TextView) convertView.findViewById(R.id.session_transition_row_tv_position);
            viewHolder.tv_username= (TextView) convertView.findViewById(R.id.session_transition_row_tv_username);
            viewHolder.tv_score = (TextView) convertView.findViewById(R.id.session_transition_row_tv_score);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        if (rankingPlayerModel != null) {

            //POSITION
            viewHolder.tv_position.setText(String.valueOf(position+1));

            if (position == 0) {
                viewHolder.tv_position.setTextColor(ContextCompat.getColor(mContext, R.color.greenColor));
                viewHolder.tv_username.setTextColor(ContextCompat.getColor(mContext, R.color.greenColor));
                viewHolder.tv_score.setTextColor(ContextCompat.getColor(mContext, R.color.greenColor));
            }

            if (rankingPlayerModel.getName() != null) {
                viewHolder.tv_username.setText(rankingPlayerModel.getName());
            }

            if(rankingPlayerModel.getScore() != null) {
                viewHolder.tv_score.setText(String.valueOf(rankingPlayerModel.getScore()));
            }

        }

        return convertView;
    }

}
