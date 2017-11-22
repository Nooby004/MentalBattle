package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.UI.Session.SessionActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by m.lallemant on 15/11/2017.
 */

public class SessionLauncherFragment extends Fragment {

    private final static String TAG = "SessionLauncherFragment";

    //UI
    private ImageView iv_back;
    private TextView tv_name;
    private ImageView iv_list_view;
    private TextView tv_nb_players;
    private TextView tv_nb_players_title;
    private Button btn_launch_ready;
    private ListView lv_players;

    //Utils
    private Player currentPlayer;
    private DatabaseManager db;
    private boolean isCreator = false;
    private String name;
    private String password;
    private Session session;
    private int nbPlayer = 0;
    private boolean isReady = false;
    private ArrayList<Player> playerModel;
    private PlayersAdapter adapter;
    private boolean isEverybodyReady = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_session_fragment, container, false);

        MenuActivity menuActivity = (MenuActivity) getActivity();

        currentPlayer = menuActivity.getCurrentPlayer();

        isCreator = getArguments().getBoolean("creator");
        name = getArguments().getString("name");
        password = getArguments().getString("password");
        session = new Session(name, password, Utils.SESSION_STATE_WAITING);

        db = DatabaseManager.getInstance();
        db.initListenerCurrentSession(session);

        initUI(v);
        initListener();

        menuActivity.setOnBackPressedListener(new MenuActivity.OnBackPressedListener() {
            @Override
            public void doBack() {
                if (db != null) {
                    quitSession();
                }
            }
        });

        return v;
    }


    private void initUI(View v) {
        iv_back = (ImageView) v.findViewById(R.id.iv_session_back);
        tv_name = (TextView) v.findViewById(R.id.tv_session_name);
        iv_list_view = (ImageView) v.findViewById(R.id.iv_session_list_view);
        tv_nb_players = (TextView) v.findViewById(R.id.tv_session_nb_player);
        tv_nb_players_title = (TextView) v.findViewById(R.id.tv_session_nb_player_title);
        btn_launch_ready = (Button) v.findViewById(R.id.btn_session_launch_ready);
        lv_players = (ListView) v.findViewById(R.id.lv_session_players);

        lv_players.setVisibility(View.VISIBLE);
        iv_list_view.setVisibility(View.INVISIBLE);

        playerModel = new ArrayList<>();
        adapter = new PlayersAdapter(playerModel, session,isCreator, getActivity());
        lv_players.setAdapter(adapter);
        adapter.clear();

        if(isCreator) {
            btn_launch_ready.setText("LAUNCH");
        } else {
            btn_launch_ready.setText("READY ?");
        }

        nbPlayer++;
        tv_nb_players.setText(""+nbPlayer);
    }


    private void initListener(){
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.removeListenerCurrentSession(session);
                db.insertPlayerInLobby(currentPlayer);
                if (isCreator) {
                    db.deleteSession(session);
                    returnSelectorFragment();
                } else {
                    quitSession();
                    db.insertPlayerInLobby(currentPlayer);
                }

            }
        });

        btn_launch_ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCreator) {
                    if (isEverybodyReady) {
                        db.updateStateSession(session, Utils.SESSION_STATE_LAUNCH_PARTY);
                    }else {
                        makeToast("All the players are not ready");
                    }
                } else {
                    if (!isReady) {
                        isReady = true;
                        btn_launch_ready.setText("READY !");
                        btn_launch_ready.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.shape_green));
                        db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_YES);
                    } else {
                        isReady = false;
                        btn_launch_ready.setText("READY ?");
                        btn_launch_ready.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.shape_orange));
                        db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_NO);
                    }
                }
            }
        });


        db.setOnSessionUpdateListener(new DatabaseManager.OnSessionUpdateListener() {
            @Override
            public void updateSessionUI(Session session_) {

                if (session_ != null) {
                    session = session_;

                    List<Player> players = session_.getPlayerList();
                    adapter.clear();
                    adapter.addAll(players);
                    tv_nb_players.setText("" + players.size());

                    boolean currentPlayerIsInList = false;
                    boolean isEverybodyReady_tmp = true;

                    for (Player player : players) {
                        if (player.getId().equals(currentPlayer.getId())) {
                            currentPlayerIsInList = true;
                        }
                        if (!player.getReady().equals(Utils.SESSION_RDY_YES)){
                            isEverybodyReady_tmp = false;
                        }
                    }

                    if (session_.getState().equals(Utils.SESSION_STATE_LAUNCH_PARTY)){
                        //launchSessionActivity();
                        launchCountDown();
                    }

                    isEverybodyReady = isEverybodyReady_tmp;
                    if (isCreator) {
                        if (isEverybodyReady) {
                            btn_launch_ready.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.shape_green));
                        } else {
                            btn_launch_ready.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.shape_orange));
                        }
                    }


                    if (!currentPlayerIsInList) {
                        displayAlertSession("You have been removed from the current session by the creator.");
                        quitSession();
                    }



                } else {
                    displayAlertSession("The current session does not exist anymore.");
                    quitSession();
                }
            }
        });
    }

    private void returnSelectorFragment(){
        SelectorFragment selectorFragment = new SelectorFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, selectorFragment);
        ft.commit();
    }

    private void quitSession(){
        db.updatePlayerNew(session, currentPlayer, Utils.SESSION_LEFT);
        db.removeListenerCurrentSession(session);
        db.removePlayerInSession(session, currentPlayer);
        returnSelectorFragment();
    }

    private void launchSessionActivity() {
        db.removeListenerCurrentSession(session);
        Intent intent = new Intent(getActivity(), SessionActivity.class);
        intent.putExtra("isCreator", isCreator);
        intent.putExtra("session", session);
        intent.putExtra("currentPlayer", currentPlayer);
        startActivity(intent);
        getActivity().finish();
    }

    private void displayAlertSession(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Info");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }

    private void launchCountDown(){
        tv_nb_players_title.setText("Game will begin in ");
        tv_nb_players.setText("5");

        new CountDownTimer(5000,1000) {
            public void onTick(long millisUntilFinished) {
                tv_nb_players.setText(""+(int) millisUntilFinished/1000);
            }

            public void onFinish() {
                tv_nb_players.setText("0");
                launchSessionActivity();
            }

        }.start();
    }
}
