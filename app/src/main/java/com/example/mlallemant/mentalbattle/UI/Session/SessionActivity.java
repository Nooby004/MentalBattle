package com.example.mlallemant.mentalbattle.UI.Session;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Game.Fragment.PlayerFindFragment;
import com.example.mlallemant.mentalbattle.UI.Session.Fragment.LoadingFragment;
import com.example.mlallemant.mentalbattle.UI.Session.Fragment.TransitionFragment;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;
import com.example.mlallemant.mentalbattle.Utils.Utils;

/**
 * Created by m.lallemant on 17/11/2017.
 */

public class SessionActivity extends AppCompatActivity{

    //UI
    private FrameLayout fl_session;

    //Utils
    private Session session;
    private DatabaseManager db;
    private Player currentPlayer;
    private int currentRoundSessionNumber = 0;
    private final int maxRoundSession = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_activity);

        Intent intent = getIntent();
        session = intent.getParcelableExtra("session");
        currentPlayer = intent.getParcelableExtra("currentPlayer");

        db = DatabaseManager.getInstance();
        db.initListenerCurrentSession(session);

        initUI();
        initListener();
    }

    private void initUI(){
        fl_session = (FrameLayout) findViewById(R.id.fl_session);
        launchLoadingScreen();
        currentRoundSessionNumber++;
    }

    private void initListener(){

        db.setOnSessionUpdateListener(new DatabaseManager.OnSessionUpdateListener() {
            @Override
            public void updateSessionUI(Session session) {

                boolean isEverybodyReady = true;

                for (Player player : session.getPlayerList()){
                    if (player.getReady().equals(Utils.SESSION_RDY_NO)){
                        isEverybodyReady = false;
                    }
                }

                if (isEverybodyReady) {
                    launchTransitionFragment(session);
                }

            }
        });
    }


    private void launchLoadingScreen(){
        LoadingFragment lf = new LoadingFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fl_session, lf);
        ft.commit();
    }

    private void launchTransitionFragment(Session session){
        TransitionFragment tf = new TransitionFragment();

        Bundle args = new Bundle();
        args.putInt("currentRoundSessionNumber", currentRoundSessionNumber);
        args.putParcelable("session", session);
        tf.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fl_session, tf);
        ft.commit();
    }

}
