package com.example.mlallemant.mentalbattle.UI;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Fragment.LobbyFragment;
import com.example.mlallemant.mentalbattle.UI.Fragment.PlayFragment;
import com.example.mlallemant.mentalbattle.UI.Fragment.WinFragment;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;

/**
 * Created by m.lallemant on 16/10/2017.
 */

public class GameActivity extends AppCompatActivity implements LobbyFragment.OnCountdownFinish, PlayFragment.OnGameFinish {


    private Game game;
    private DatabaseManager db;
    private Player currentPlayer, otherPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        String currentPlayerId;
        String id = getIntent().getExtras().getString("idGame");
        currentPlayerId = getIntent().getExtras().getString("currentPlayerId");

        db = DatabaseManager.getInstance();
        game = db.getGameById(id);

        currentPlayer = db.getPlayerById(currentPlayerId);

        if (game.getPlayer1().getId().equals(currentPlayer.getId())){
            otherPlayer = game.getPlayer2();
        }else{
            otherPlayer = game.getPlayer1();
        }

        db.setInGameByPlayer(Utils.INGAME, currentPlayer);
        db.setIsSearchingGameByPlayer(!Utils.SEARCHINGGAME, currentPlayer);


        //Create lobby fragment
        LobbyFragment lf = new LobbyFragment();
        Bundle args = new Bundle();
        args.putString("currentPlayer", currentPlayer.getName());
        args.putString("otherPlayer", otherPlayer.getName());
        lf.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fl_game, lf);
        ft.commit();

    }


    @Override
    public void onStop()
    {
        super.onStop();
        db.deleteUserById(currentPlayer.getId());
        db.deleteGame(game);
    }

    public void launchGame(){

        //create play fragment
        PlayFragment playFragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString("currentPlayer", currentPlayer.getName());
        args.putString("otherPlayer", otherPlayer.getName());
        args.putString("currentPlayerID", currentPlayer.getId());
        args.putString("otherPlayerID", otherPlayer.getId());
        args.putString("gameID",game.getId());
        playFragment.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_game, playFragment);
        ft.commit();
    }

    public void displayWinScreen(String winnerName, String looserName, Integer winnerScore, Integer looserScore) {
        //create win fragment
        WinFragment winFragment = new WinFragment();
        Bundle args = new Bundle();
        args.putString("winnerName", winnerName);
        args.putString("looserName", looserName);
        args.putString("winnerScore", String.valueOf(winnerScore));
        args.putString("looserScore", String.valueOf(looserScore));

        winFragment.setArguments(args);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_game, winFragment);
        ft.commit();

    }




}
