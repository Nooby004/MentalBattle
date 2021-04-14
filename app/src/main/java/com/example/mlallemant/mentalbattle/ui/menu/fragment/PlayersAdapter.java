package com.example.mlallemant.mentalbattle.ui.menu.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.utils.Player;
import com.example.mlallemant.mentalbattle.utils.Session;
import com.example.mlallemant.mentalbattle.utils.Utils;

import java.util.ArrayList;

/**
 * Created by m.lallemant on 16/11/2017.
 */

public class PlayersAdapter extends ArrayAdapter<Player> implements View.OnClickListener {

    private final ArrayList<Player> dataSet;
    Context mContext;
    private final DatabaseManager db;
    private final Session session;
    private final boolean isCreator;

    private static class ViewHolder {
        TextView username;
        ImageView delete;
    }

    public PlayersAdapter(final ArrayList<Player> data, final Session session, final boolean isCreator, final Context context) {
        super(context, R.layout.session_player_row_item_template, data);
        this.dataSet = data;
        this.mContext = context;
        this.session = session;
        this.isCreator = isCreator;
        db = DatabaseManager.getInstance();
    }

    @Override
    public void onClick(final View v) {
        final int position = (Integer) v.getTag();
        final Object object = getItem(position);

        final Player playerModel = (Player) object;
    }

    private int lastPosition = -1;

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final Player playerModel = getItem(position);
        final ViewHolder viewHolder;
        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.session_player_row_item_template, parent, false);

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
                } else {
                    viewHolder.delete.setVisibility(View.VISIBLE);
                    viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
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
