package com.example.mlallemant.mentalbattle.Utils;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.example.mlallemant.mentalbattle.UI.LoginActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class DatabaseManager {

    private OnScoreChangeListener onScoreChangeListener;
    private OnRageQuitListener onRageQuitListener;

    public interface OnScoreChangeListener{
        void updateScoreUI(Integer score, String playerID);
    }

    public interface OnRageQuitListener{
        void alertUserPlayerRageQuit();
    }

    private final static String TAG = "DatabaseManager";

    private static DatabaseManager instance = null;
    
    private FirebaseDatabase database;
    private List<Player> playerList;
    private List<Game> gameList;

    private List<Player> playerSearchingGameList;
    private List<Player> playerInLobbyList;


    private ValueEventListener gamesListener;
    private ValueEventListener currentGameListener;

    private DatabaseManager(){
        database = FirebaseDatabase.getInstance();
        playerList = new ArrayList<>();
        gameList = new ArrayList<>();
        playerSearchingGameList = new ArrayList<>();
        playerInLobbyList = new ArrayList<>();


        initListenerGames();
        this.onRageQuitListener = null;
        this.onScoreChangeListener = null;
    }

    public static synchronized DatabaseManager getInstance(){
        if (instance == null){
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void setScoreChangeListener(OnScoreChangeListener listener){
        this.onScoreChangeListener = listener;
    }

    public void setOnRageQuitListener(OnRageQuitListener listener){
        this.onRageQuitListener = listener;
    }

    /**
     * PLAYER (OLD)
     */

    public void insertPlayer(Player player) {
        DatabaseReference reference = database.getReference("players");

        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("score").setValue(player.getScore());
    }

    public Player getPlayerById(String id){
        Player player;

        for (int i=0; i<playerList.size();i++){
            if (playerList.get(i).getId().equals(id)){
                player = playerList.get(i);
                return player;
            }
        }
        return null;
    }

    public void deletePlayer(Player player){
        DatabaseReference reference = database.getReference("players");
        reference.child(player.getId()).removeValue();
        playerList.remove(player);
    }

    public List<Player> getAllUser() {
        return playerList;
    }


    /**
     * PLAYER SEARCHING GAME
     */

    public void insertPlayerSearchingGame(Player player) {
        DatabaseReference reference = database.getReference("players").child("searchingGame");
        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("score").setValue(player.getScore());
    }

    public void deletePlayerSearchingPlayer(Player player){
        DatabaseReference reference = database.getReference("players").child("searchingGame");
        reference.child(player.getId()).removeValue();
        //playerList.remove(player);
    }

    /**
     * PLAYER IN LOBBY
     */

    public void insertPlayerInLobby(Player player) {
        DatabaseReference reference = database.getReference("players").child("inLobby");
        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("score").setValue(player.getScore());
    }

    public void deletePlayerInLobby(Player player){
        DatabaseReference reference = database.getReference("players").child("inLobby");
        reference.child(player.getId()).removeValue();
        //playerList.remove(player);
    }


    /**
     * GAME
     */

    public void initListenerGames(){
        DatabaseReference reference = database.getReference("games");

        gamesListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    String id = child.getKey();
                    Player player1 = child.child("player1").getValue(Player.class);
                    Player player2 = child.child("player2").getValue(Player.class);
                    GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {
                    };
                    List<Calculation> calculationList = child.child("calculationList").getValue(genericType);
                    //Log.e(TAG, "listenerGame");
                    Game game = new Game(id, player1, player2, calculationList);
                    addAndUpdateGame(game);
                    if (onScoreChangeListener != null && player1 != null && player2 != null) {
                        onScoreChangeListener.updateScoreUI(player1.getScore(), player1.getId());
                        onScoreChangeListener.updateScoreUI(player2.getScore(), player2.getId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    private Boolean checkIntegrityGameStructure(DataSnapshot child)
    {
        Boolean integrity = true;

        if (!child.child("player1").exists() && !child.child("player2").exists() && !child.child("calculationList").exists()){
            integrity = false;
            Log.e(TAG, "Game structure integrity wrong");
        }

        return integrity;
    }

    public void initListenerCurrentGame(final Game game){

        final DatabaseReference reference = database.getReference("games");
        reference.removeEventListener(gamesListener);

        currentGameListener = reference.child(game.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("player1") && dataSnapshot.hasChild("player2") && dataSnapshot.hasChild("calculationList")) {

                        Player player1 = dataSnapshot.child("player1").getValue(Player.class);
                        Player player2 = dataSnapshot.child("player2").getValue(Player.class);

                        GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {
                        };
                        List<Calculation> calculationList = dataSnapshot.child("calculationList").getValue(genericType);

                        Game currentGame = new Game(game.getId(), player1, player2, calculationList);

                        addAndUpdateGame(currentGame);
                        if (onScoreChangeListener != null && player1 != null && player2 != null) {
                            onScoreChangeListener.updateScoreUI(player1.getScore(), player1.getId());
                            onScoreChangeListener.updateScoreUI(player2.getScore(), player2.getId());
                        }
                    } else {
                        Log.e(TAG, "Structure Database in error");
                        //Display "Error with database, try again"
                    }
                }else{
                    Log.e(TAG, "GAME NOT EXIST");
                    reference.child(game.getId()).removeEventListener(currentGameListener);
                    initListenerGames();

                    if (onRageQuitListener != null) {
                        onRageQuitListener.alertUserPlayerRageQuit();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    private void addAndUpdateGame(Game game){
        Boolean isInList = false;

        for (int i = 0; i<gameList.size(); i++){
            if (gameList.get(i).getId().equals(game.getId())){
                gameList.set(i, game);
                isInList = true;
            }
        }

        if (!isInList){
            gameList.add(game);
        }
    }

    public void insertGame(Game game) {
        DatabaseReference reference = database.getReference("games");

        reference.child(game.getId()).child("player1").setValue(game.getPlayer1());
        reference.child(game.getId()).child("player2").setValue(game.getPlayer2());
        for (int i=0; i<game.getCalculationList().size(); i++) {
            reference.child(game.getId()).child("calculationList").child(String.valueOf(i)).setValue(game.getCalculationList().get(i));
        }
    }

    public List<Game> getAllGame() {
        return gameList;
    }

    public Game getGameById(String id){
        Game game;

        for (int i=0; i<gameList.size();i++){
            if (gameList.get(i).getId().equals(id)) {
                game = gameList.get(i);
                return game;
            }
        }
        return null;
    }

    public void setScorePlayer1ByIdGame(Integer score, String id){
        DatabaseReference reference = database.getReference("games");
        reference.child(id).child("player1").child("score").setValue(score);
    }

    public void setScorePlayer2ByIdGame(Integer score, String id){
        DatabaseReference reference = database.getReference("games");
        reference.child(id).child("player2").child("score").setValue(score);
    }

    public void insertPlayer1InGameById(Player player1, String id){
        DatabaseReference reference = database.getReference("games");
        reference.child(id).child("player1").setValue(player1);
    }

    public void insertPlayer2InGameById(Player player2, String id){
        DatabaseReference reference = database.getReference("games");
        reference.child(id).child("player2").setValue(player2);
    }

    public Game findAvailableGame(){
        Game game = null;

        for (int i = 0; i< gameList.size(); i++){
            if (gameList.get(i).getPlayer1().getId().equals("") || gameList.get(i).getPlayer2().getId().equals("")){
                game = gameList.get(i);
            }
        }
        Log.e(TAG, "gameList.size : " + gameList.size());
        return game;
    }

    public void deleteGame(Game game){
        DatabaseReference reference = database.getReference("games");
        reference.child(game.getId()).removeValue();
        gameList.remove(game);
    }



}
