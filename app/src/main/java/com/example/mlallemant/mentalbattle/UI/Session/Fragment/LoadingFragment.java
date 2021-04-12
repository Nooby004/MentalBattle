package com.example.mlallemant.mentalbattle.UI.Session.Fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.loading_activity, container, false);

        final Bundle bundle = getArguments();
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
            public void updateSessionUI(final Session session) {
                if (session != null) {

                    boolean isEverybodyReady = true;

                    for (final Player player : session.getPlayerList()) {
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

    private void launchTransitionFragment(final Session session) {
        final SessionActivity sa = (SessionActivity) getActivity();
        sa.addCurrentRoundSessionNumber();

        db.removeListenerCurrentSession(session);
        final TransitionFragment tf = new TransitionFragment();

        final Bundle args = new Bundle();
        args.putInt("currentRoundSessionNumber", sa.getCurrentRoundSessionNumber());
        args.putParcelable("session", session);
        args.putParcelable("currentPlayer", currentPlayer);
        tf.setArguments(args);

        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_session, tf);
        ft.commit();
    }

}
