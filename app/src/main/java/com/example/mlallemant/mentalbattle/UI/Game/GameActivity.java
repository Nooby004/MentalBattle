package com.example.mlallemant.mentalbattle.UI.Game;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Fragment.PlayerFindFragment;
import com.example.mlallemant.mentalbattle.UI.Fragment.PlayFragment;
import com.example.mlallemant.mentalbattle.UI.Fragment.WinFragment;
import com.example.mlallemant.mentalbattle.UI.Lobby.PlayAsGuest;
import com.example.mlallemant.mentalbattle.UI.Lobby.PlayAsRegistered;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.Random;

/**
 * Created by m.lallemant on 16/10/2017.
 */

public class GameActivity extends AppCompatActivity implements PlayerFindFragment.OnCountdownFinish, PlayFragment.OnGameFinish, WinFragment.OnNextGame {

    private Boolean gameIsFinished = false;

    private Game game;
    private DatabaseManager db;
    private Player currentPlayer, otherPlayer;

    private Boolean appGoesToBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        String currentPlayerId;
        String id = getIntent().getExtras().getString("idGame");
        currentPlayerId = getIntent().getExtras().getString("currentPlayerId");

        db = DatabaseManager.getInstance();
        game = db.getCurrentGame();

        if (game.getPlayer1().getId().equals(currentPlayerId)){
            currentPlayer = game.getPlayer1();
            otherPlayer = game.getPlayer2();
        }else{
            currentPlayer = game.getPlayer2();
            otherPlayer = game.getPlayer1();
        }
        db.deletePlayerSearchingPlayer(currentPlayer);

        //Create lobby fragment
        PlayerFindFragment lf = new PlayerFindFragment();
        Bundle args = new Bundle();
        args.putString("currentPlayer", currentPlayer.getName());
        args.putString("otherPlayer", otherPlayer.getName());
        lf.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fl_game, lf);
        ft.commit();

        db.setOnRageQuitListener(new DatabaseManager.OnRageQuitListener() {
            @Override
            public void alertUserPlayerRageQuit() {
                if (!gameIsFinished) {
                    displayWinScreen(currentPlayer.getName(), otherPlayer.getName(), 999, 0, "YOU WIN BY RAGEQUIT !");
                }
            }
        });
    }


    @Override
    public void onStop()
    {
        super.onStop();
        if (!appGoesToBackground) {
            db.deleteCurrentGame(game);
            finish();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (!appGoesToBackground) {
            db.deleteCurrentGame(game);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        gameIsFinished = true;
        db.deleteCurrentGame(game);
        db.notifyFriendsYouAreDisconnected(currentPlayer);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        appGoesToBackground = true;
    }


    @Override
    public void onResume(){
        super.onResume();
        appGoesToBackground = false;

    }

    public void launchGame(){

        //create play fragment
        if (!gameIsFinished) {
            PlayFragment playFragment = new PlayFragment();
            Bundle args = new Bundle();
            args.putString("currentPlayer", currentPlayer.getName());
            args.putString("otherPlayer", otherPlayer.getName());
            args.putString("currentPlayerID", currentPlayer.getId());
            args.putString("otherPlayerID", otherPlayer.getId());
            args.putString("gameID", game.getId());
            playFragment.setArguments(args);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fl_game, playFragment);
            ft.commit();
        }
    }

    public void displayWinScreen(String winnerName, String looserName, Integer winnerScore, Integer looserScore, String resultGame) {

        gameIsFinished = true;
        //create win fragment
        WinFragment winFragment = new WinFragment();
        Bundle args = new Bundle();
        args.putString("winnerName", winnerName);
        args.putString("looserName", looserName);
        args.putString("winnerScore", String.valueOf(winnerScore));
        args.putString("looserScore", String.valueOf(looserScore));
        args.putString("resultGame", resultGame);

        calculXpGain(winnerName, winnerScore, looserScore);

        winFragment.setArguments(args);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_game, winFragment);
        ft.commit();
    }

    public void launchNextGame(){
        //Return on LoginActivity
        db.deleteCurrentGame(game);

        if(Utils.AUTHENTIFICATION_TYPE == Utils.AUTHENTIFICATION_GUEST){
            launchPlayAsGuestActivity();
        } else {
            launchPlayAsRegisterActivity();
        }


    }

    private void launchPlayAsGuestActivity(){
        Intent intent = new Intent(GameActivity.this, PlayAsGuest.class);
        this.startActivity(intent);
        finish();
    }

    private void launchPlayAsRegisterActivity(){
        Intent intent = new Intent(GameActivity.this, PlayAsRegistered.class);
        this.startActivity(intent);
        finish();
    }

    private void calculXpGain(String winnerName, Integer winnerScore, Integer looserScore){
        int gainXP = 0;

        boolean currentPlayerWin = false;

        if (winnerName.equals(currentPlayer.getName())) {
            currentPlayerWin = true;
        }

        Random r = new Random();
        int rand = r.nextInt(4 - 1) + 1;

        int base;
        if (currentPlayerWin){
            db.setNbWinLoseByPlayer(currentPlayer, (currentPlayer.getNb_win()+1), (currentPlayer.getNb_lose()));
            base = (winnerScore * 5) + (rand * getLevelByXp(currentPlayer.getXp()));
            gainXP = (int) Math.round(base + 0.3 * base);
        } else {
            db.setNbWinLoseByPlayer(currentPlayer, (currentPlayer.getNb_win()), (currentPlayer.getNb_lose()+1));
            base = (looserScore * 5) + (rand * getLevelByXp(currentPlayer.getXp()));
            gainXP = (int) Math.round(base - 0.2 * base);
        }

        if (winnerScore == 999){
            gainXP = 0;
            db.setNbWinLoseByPlayer(otherPlayer, otherPlayer.getNb_win(), (otherPlayer.getNb_lose()+1));
        }


        int xpToSet = currentPlayer.getXp() + gainXP;

        db.setCurrentPlayerXp(currentPlayer, xpToSet);
        //SET SCORE CURRENT PLAYER
    }



    private int getLevelByXp(int XP){
        int level;
        level = (int) Math.round ((Math.sqrt(100 * (2 * XP + 25) + 50) / 100));
        return level;
    }



}
