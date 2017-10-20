package com.example.mlallemant.mentalbattle.UI.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;

/**
 * Created by m.lallemant on 20/10/2017.
 */

public class WinFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.win_fragment, container, false);

        //get arguments
        Bundle args = getArguments();
        String winnerName = args.getString("winnerName");
        String looserName = args.getString("looserName");
        String winnerScore = args.getString("winnerScore");
        String looserScore = args.getString("looserScore");

        TextView tv_winner = (TextView) v.findViewById(R.id.win_tv_winner_player);
        TextView tv_looser = (TextView) v.findViewById(R.id.win_tv_looser_player);

        String twinner = winnerName + " - " + winnerScore;
        String tlooser = looserName + " - " + looserScore;

        tv_winner.setText(twinner);
        tv_looser.setText(tlooser);


        return v;

    }

}
