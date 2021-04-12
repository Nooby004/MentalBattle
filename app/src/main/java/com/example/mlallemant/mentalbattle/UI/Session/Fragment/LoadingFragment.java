package com.example.mlallemant.mentalbattle.UI.Session.Fragment;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Session.SessionActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;
import com.example.mlallemant.mentalbattle.Utils.Utils;


/**
 * Created by m.lallemant on 17/11/2017.
 */

public class LoadingFragment extends Fragment {

    //UTILS
    private DatabaseManager db;
    private Session session;
    private Player currentPlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.loading_activity, container, false);

        Bundle bundle = getArguments();
        session = bundle.getParcelable("session");
        currentPlayer = bundle.getParcelable("currentPlayer");

        db = DatabaseManager.getInstance();
        db.initListenerCurrentSession(session);

        initListener();
        return v;
    }

    private void initListener() {
        db.setOnSessionUpdateListener(new DatabaseManager.OnSessionUpdateListener() {
            @Override
            public void updateSessionUI(Session session) {
                if (session != null) {

                    boolean isEverybodyReady = true;

                    for (Player player : session.getPlayerList()) {
                        if (player.getReady().equals(Utils.SESSION_RDY_NO)) {
                            isEverybodyReady = false;
                        }
                    }

                    if (isEverybodyReady) {
                        launchTransitionFragment(session);
                    }
                }
            }
        });
    }

    private void launchTransitionFragment(Session session){
        SessionActivity sa = (SessionActivity) getActivity();
        sa.addCurrentRoundSessionNumber();

        db.removeListenerCurrentSession(session);
        TransitionFragment tf = new TransitionFragment();

        Bundle args = new Bundle();
        args.putInt("currentRoundSessionNumber", sa.getCurrentRoundSessionNumber());
        args.putParcelable("session", session);
        args.putParcelable("currentPlayer", currentPlayer);
        tf.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_session, tf);
        ft.commit();
    }

}
