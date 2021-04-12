package com.example.mlallemant.mentalbattle.UI.Session;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Session.Fragment.RoundFragment;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;

/**
 * Created by m.lallemant on 17/11/2017.
 */

public class SessionActivity extends AppCompatActivity {

    //UI
    private FrameLayout fl_session;

    //Utils
    private Session session;
    private DatabaseManager db;
    private Player currentPlayer;
    private int currentRoundSessionNumber = 0;
    private boolean isCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_activity);

        Intent intent = getIntent();
        session = intent.getParcelableExtra("session");
        currentPlayer = intent.getParcelableExtra("currentPlayer");
        isCreator = intent.getBooleanExtra("isCreator", false);

        db = DatabaseManager.getInstance();

        initUI();
    }


    @Override
    public void onBackPressed(){
        if (isCreator) {
            db.deleteSession(session);
        } else {
            db.removePlayerInSession(session, currentPlayer);
        }
    }


    private void initUI(){
        fl_session = (FrameLayout) findViewById(R.id.fl_session);
        launchRoundFragment();
    }

    public int getCurrentRoundSessionNumber(){
        return currentRoundSessionNumber;
    }

    public void deleteSession(){
        db.deleteSession(session);
    }

    public void addCurrentRoundSessionNumber(){
        currentRoundSessionNumber++;
    }

    public boolean isCreator(){
        return isCreator;
    }

    private void launchRoundFragment() {
        db.removeListenerCurrentSession(session);
        RoundFragment rf = new RoundFragment();
        Bundle args = new Bundle();
        args.putParcelable("session", session);
        args.putParcelable("currentPlayer", currentPlayer);
        rf.setArguments(args);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_session, rf);
        ft.commit();
    }
}
