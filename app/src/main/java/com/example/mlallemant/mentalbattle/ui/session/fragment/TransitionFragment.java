package com.example.mlallemant.mentalbattle.ui.session.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity;
import com.example.mlallemant.mentalbattle.ui.session.SessionActivity;
import com.example.mlallemant.mentalbattle.utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.utils.Player;
import com.example.mlallemant.mentalbattle.utils.Session;
import com.example.mlallemant.mentalbattle.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by m.lallemant on 17/11/2017.
 */

public class TransitionFragment extends Fragment {

    //UI
    private TextView tv_round_number;
    private ListView lv_ranking;
    private Button btn_next_round;

    //UTILS
    private Session session;
    private int currentRoundSessionNumber;
    private Player currentPlayer;
    private ArrayList<Player> playerModel;
    private RankingPlayerAdapter adapter;
    private final static int MAX_ROUND = 3;
    private boolean isCreator = false;
    private DatabaseManager db;
 

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.session_transition_fragment, container, false);

        Bundle bundle = getArguments();
        session = bundle.getParcelable("session");
        currentPlayer = bundle.getParcelable("currentPlayer");
        currentRoundSessionNumber = bundle.getInt("currentRoundSessionNumber");

        isCreator = ((SessionActivity) getActivity()).isCreator();

        db = DatabaseManager.getInstance();
        db.initListenerCurrentSession(session);

        initUI(v);
        initListener();

        return v;
    }

    private void initUI(View v) {
        tv_round_number = (TextView) v.findViewById(R.id.session_transition_tv_round_number);
        lv_ranking = (ListView) v.findViewById(R.id.session_transition_lv_ranking);
        btn_next_round = (Button) v.findViewById(R.id.session_transition_btn_next_round);

        String text = "Ranking - Round " + currentRoundSessionNumber + "/" + MAX_ROUND;
        tv_round_number.setText(text);

        playerModel = new ArrayList<>();
        adapter = new RankingPlayerAdapter(playerModel, getActivity());
        lv_ranking.setAdapter(adapter);
        adapter.clear();

        Collections.sort(session.getPlayerList(), new Comparator<Player>() {
            @Override
            public int compare(Player lhs, Player rhs) {
                return rhs.getScore().compareTo(lhs.getScore());
            }
        });

        List<Player> players = session.getPlayerList();
        adapter.addAll(players);


        if (currentRoundSessionNumber >= MAX_ROUND) {
            tv_round_number.setText("Final Ranking");
            btn_next_round.setEnabled(true);
            text = "Return menu";
            btn_next_round.setText(text);
        } else {
            if (isCreator) {
                btn_next_round.setEnabled(true);
                btn_next_round.setText("Next Round");
            } else {
                btn_next_round.setEnabled(false);
                text = "Waiting creator for next round...";
                btn_next_round.setText(text);
            }
        }
    }

    private void initListener() {
        btn_next_round.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentRoundSessionNumber >= MAX_ROUND) {
                    ((SessionActivity) getActivity()).deleteSession();
                    launchMenuActivity();
                } else {
                    if (isCreator) {
                        db.updateStateSession(session, Utils.SESSION_STATE_LAUNCH_ROUND);
                    }
                }
            }
        });

        db.setOnSessionUpdateListener(new DatabaseManager.OnSessionUpdateListener() {
            @Override
            public void updateSessionUI(Session session) {
                if (session != null) {

                    if (session.getState().equals(Utils.SESSION_STATE_LAUNCH_ROUND)) {
                        launchRoundFragment();
                    }
                }
            }
        });
    }

    private void launchRoundFragment() {
        db.removeListenerCurrentSession(session);
        db.updateStateSession(session, Utils.SESSION_STATE_LAUNCH_PARTY);
        RoundFragment rf = new RoundFragment();
        Bundle args = new Bundle();
        args.putInt("currentRoundSessionNumber", currentRoundSessionNumber);
        args.putParcelable("session", session);
        args.putParcelable("currentPlayer", currentPlayer);
        rf.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_session, rf);
        ft.commit();
    }


    private void launchMenuActivity() {
        Intent intent = new Intent(getActivity(), MenuActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void makeToast(String text) {
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }
}
