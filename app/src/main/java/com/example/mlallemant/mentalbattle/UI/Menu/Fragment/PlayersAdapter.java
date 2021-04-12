package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;

/**
 * Created by m.lallemant on 16/11/2017.
 */

public class PlayersAdapter extends ArrayAdapter<Player> implements View.OnClickListener {

    private ArrayList<Player> dataSet;
    Context mContext;
    private DatabaseManager db;
    private Session session;
    private boolean isCreator;

    private static class ViewHolder {
        TextView username;
        ImageView delete;
    }

    public PlayersAdapter(ArrayList<Player> data, Session session, boolean isCreator, Context context) {
        super(context, R.layout.session_player_row_item_template, data);
        this.dataSet = data;
        this.mContext = context;
        this.session = session;
        this.isCreator = isCreator;
        db = DatabaseManager.getInstance();
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);

        Player playerModel = (Player) object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Player playerModel = getItem(position);
        final ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.session_player_row_item_template, parent,false);

            viewHolder.username = (TextView) convertView.findViewById(R.id.session_row_tv_username);
            viewHolder.delete = (ImageView) convertView.findViewById(R.id.session_row_iv_delete);
            viewHolder.delete.setTag(position);

            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        if (playerModel != null) {

            //USERNAME
            if (playerModel.getName() != null) {
                viewHolder.username.setText(playerModel.getName());
                if (playerModel.getReady().equals(Utils.SESSION_RDY_YES)) {
                    viewHolder.username.setTextColor(ContextCompat.getColor(mContext, R.color.greenColor));
                } else {
                    viewHolder.username.setTextColor(ContextCompat.getColor(mContext, R.color.redColor));
                }
            }

            //DELETE
            if (isCreator) {
                if (playerModel.getNew_().equals(Utils.SESSION_CREATOR)) {
                    viewHolder.delete.setVisibility(View.INVISIBLE);
                }else {
                    viewHolder.delete.setVisibility(View.VISIBLE);
                    viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            db.removePlayerInSession(session, playerModel);
                        }
                    });
                }
            } else {
                viewHolder.delete.setVisibility(View.INVISIBLE);
            }


        }

        return convertView;
    }

}
