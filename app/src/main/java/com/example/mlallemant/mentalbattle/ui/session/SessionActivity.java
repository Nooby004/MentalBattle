package com.example.mlallemant.mentalbattle.ui.session;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.ui.session.fragment.RoundFragment;
import com.example.mlallemant.mentalbattle.utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.utils.Player;
import com.example.mlallemant.mentalbattle.utils.Session;

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
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_activity);

        final Intent intent = getIntent();
        session = intent.getParcelableExtra("session");
        currentPlayer = intent.getParcelableExtra("currentPlayer");
        isCreator = intent.getBooleanExtra("isCreator", false);

        db = DatabaseManager.getInstance();

        initUI();
    }


    @Override
    public void onBackPressed() {
        if (isCreator) {
            db.deleteSession(session);
        } else {
            db.removePlayerInSession(session, currentPlayer);
        }
    }


    private void initUI() {
        fl_session = (FrameLayout) findViewById(R.id.fl_session);
        launchRoundFragment();
    }

    public int getCurrentRoundSessionNumber() {
        return currentRoundSessionNumber;
    }

    public void deleteSession() {
        db.deleteSession(session);
    }

    public void addCurrentRoundSessionNumber() {
        currentRoundSessionNumber++;
    }

    public boolean isCreator() {
        return isCreator;
    }

    private void launchRoundFragment() {
        db.removeListenerCurrentSession(session);
        final RoundFragment rf = new RoundFragment();
        final Bundle args = new Bundle();
        args.putParcelable("session", session);
        args.putParcelable("currentPlayer", currentPlayer);
        rf.setArguments(args);

        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_session, rf);
        ft.commit();
    }
}
