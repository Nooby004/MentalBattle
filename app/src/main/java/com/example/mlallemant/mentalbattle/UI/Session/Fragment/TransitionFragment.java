package com.example.mlallemant.mentalbattle.UI.Session.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m.lallemant on 17/11/2017.
 */

public class TransitionFragment extends Fragment {

    //UI
    private TextView tv_round_number;
    private ListView lv_ranking;
    private TextView tv_counter;
    private ProgressBar pg_counter;

    //UTILS
    private final static int WAITING_TIME = 10000; //10s
    private Session session;
    private int currentRoundSessionNumber;
    private ArrayList<Player> playerModel;
    private RankingPlayerAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.session_transition_fragment, container, false);

        Bundle bundle = getArguments();
        session = bundle.getParcelable("session");
        currentRoundSessionNumber = bundle.getInt("currentRoundSessionNumber");

        initUI(v);
        launchCountDown();
        return v;
    }

    private void initUI(View v) {
        tv_round_number = (TextView) v.findViewById(R.id.session_transition_tv_round_number);
        lv_ranking = (ListView) v.findViewById(R.id.session_transition_lv_ranking);
        tv_counter = (TextView) v.findViewById(R.id.session_transition_tv_counter);
        pg_counter = (ProgressBar) v.findViewById(R.id.session_transition_pg_counter);

        tv_round_number.setText("ROUND " + currentRoundSessionNumber);

        playerModel = new ArrayList<>();
        adapter = new RankingPlayerAdapter(playerModel, getActivity());
        lv_ranking.setAdapter(adapter);
        adapter.clear();

        List<Player> players = session.getPlayerList();

        Log.e("ERROR", session.getPlayerList().get(0).getName());
        adapter.addAll(players);

    }



    private void launchCountDown(){

        new CountDownTimer(WAITING_TIME, 1000){
            public void onTick(long millisUntilFinished){

                String remainingTime = ""+millisUntilFinished/1000;
                tv_counter.setText(remainingTime);
            }

            public void onFinish(){
                //LAUNCH ROUND
            }

        }.start();
    }
}
