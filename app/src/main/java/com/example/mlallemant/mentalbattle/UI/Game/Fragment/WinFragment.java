package com.example.mlallemant.mentalbattle.UI.Game.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mlallemant.mentalbattle.R;

/**
 * Created by m.lallemant on 20/10/2017.
 */

public class WinFragment extends Fragment{

    OnNextGame mCallBack;

    public interface OnNextGame{
        void launchNextGame();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.win_fragment, container, false);

        //get arguments
        Bundle args = getArguments();
        String winnerName = args.getString("winnerName");
        String looserName = args.getString("looserName");
        String winnerScore = args.getString("winnerScore");
        String looserScore = args.getString("looserScore");
        String resultGame = args.getString("resultGame");

        TextView tv_winner = (TextView) v.findViewById(R.id.win_tv_winner_player);
        TextView tv_looser = (TextView) v.findViewById(R.id.win_tv_looser_player);
        TextView tv_resultGame = (TextView) v.findViewById(R.id.win_tv_win_lose);

        String twinner = winnerName.split(" ")[0] + " - " + winnerScore;
        String tlooser = looserName.split(" ")[0] + " - " + looserScore;

        tv_winner.setText(twinner);
        tv_looser.setText(tlooser);

        if (resultGame.equals("YOU LOSE !")){
            tv_resultGame.setTextColor(ContextCompat.getColor(getActivity(), R.color.whiteGrayColor));
        }else{
            tv_resultGame.setTextColor(ContextCompat.getColor(getActivity(), R.color.orangeColor));
        }
        tv_resultGame.setText(resultGame);

        Button btn_next = (Button) v.findViewById(R.id.win_btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mCallBack.launchNextGame();
            }
        });

        return v;
    }

     @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (activity instanceof OnNextGame) {
            mCallBack = (OnNextGame) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNextGame) {
            mCallBack = (OnNextGame) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }


}
