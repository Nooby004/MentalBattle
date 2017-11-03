package com.example.mlallemant.mentalbattle.Utils;

import android.os.AsyncTask;
import android.util.Log;

import com.example.mlallemant.mentalbattle.UI.Lobby.PlayAsRegistered;

import java.util.UUID;

/**
 * Created by m.lallemant on 27/10/2017.
 */

public class SearchGameTask extends AsyncTask <String, Void, Game> {

    public interface AsyncResponse {
        void onFinishTask(Game game);
    }

    private Player currentPlayer;
    private Game currentGame;
    private DatabaseManager db;
    private final static String TAG = "SearchGameTask";


    public AsyncResponse delegate = null;

    public SearchGameTask(AsyncResponse delegate){
        this.delegate = delegate;
        db = DatabaseManager.getInstance();
    }

    public void setParams(Player player, Game game){
        this.currentPlayer = player;
        this.currentGame = game;
    }

    public Game getCurrentGame(){
        return currentGame;
    }

    public Player getCurrentPlayer(){
        return currentPlayer;
    }

    @Override
    protected Game doInBackground(String... urls) {
        Game returnGame = null;

        try{
            long start_time = System.currentTimeMillis();
            long wait_time = Utils.SEARCH_TIME;
            long end_time = start_time + wait_time;

            Game availableGame = db.findAvailableGame();

            if(availableGame == null){
                // if no game available, we create one
                Log.e(TAG, "no game available");
                Player tmpPlayer = new Player("","",0);
                String id = getRandomId();
                Game game = new Game(id, currentPlayer, tmpPlayer);
                db.insertAvailableGame(game);
                currentGame = game;

                while(System.currentTimeMillis() < end_time){
                    if (isCancelled()){
                        db.deleteAvailableGame(currentGame);
                        break;
                    }

                    Game tmpGame = db.getAvailableGame(game);

                    if (tmpGame != null && tmpGame.getPlayer1() != null && tmpGame.getPlayer2() != null) {
                        if ((!tmpGame.getPlayer1().getId().equals("")) && (!tmpGame.getPlayer2().getId().equals(""))) {
                            returnGame = tmpGame;
                            break;
                        }
                    }
                    returnGame = tmpGame;
                }
            } else {
                // else we insert player1 in game available
                Log.e(TAG, "Game available");
                if (availableGame.getPlayer1().getId().equals("")){
                    db.insertPlayer1InAvailableGame(currentPlayer, availableGame);
                    Log.e(TAG, "Game Player1 inserted");
                } else {
                    db.insertPlayer2InAvailableGame(currentPlayer, availableGame);
                    Log.e(TAG, "Game Player2 inserted");
                }

                while(true){
                    returnGame = db.getAvailableGame(availableGame);
                    if (returnGame != null && returnGame.getPlayer1() != null && returnGame.getPlayer2() != null) {
                        if ((!returnGame.getPlayer1().getId().equals("")) && (!returnGame.getPlayer2().getId().equals(""))) {
                            break;
                        }
                    }
                }
                currentGame = returnGame;
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return returnGame;
    }


    @Override
    protected void onPostExecute(Game game) {
        delegate.onFinishTask(game);
    }

    private static String getRandomId(){
        return UUID.randomUUID().toString();
    }


}
