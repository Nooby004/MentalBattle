package com.example.mlallemant.mentalbattle.Utils;

import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class DatabaseManager {

    private OnScoreChangeListener onScoreChangeListener;

    public interface OnScoreChangeListener{
        void updateScoreUI(Integer score, String playerID);
    }

    private final static String TAG = "DatabaseManager";

    private static DatabaseManager instance = null;
    
    private FirebaseDatabase database;
    private List<Player> playerList;
    private List<Game> gameList;

    private DatabaseManager(){
        database = FirebaseDatabase.getInstance();
        playerList = new ArrayList<>();
        gameList = new ArrayList<>();
        initListenerPlayers();
        initListenerGames();

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


    /**
     * PLAYER
     */
    private void initListenerPlayers(){
        DatabaseReference reference = database.getReference("players");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                playerList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String id = child.getKey();
                    String name = child.child("name").getValue().toString();
                    Boolean inGame = (Boolean) child.child("inGame").getValue();
                    Boolean isConnected = (Boolean) child.child("isConnected").getValue();
                    Boolean isSearchingGame = (Boolean) child.child("isSearchingGame").getValue();
                    Long score_ = (Long) child.child("score").getValue();

                    Integer score = score_ != null ? score_.intValue() : null;

                    Player player = new Player(id,name,inGame,isConnected,isSearchingGame,score);
                    playerList.add(player);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }


    public void insertPlayer(Player player) {
        DatabaseReference reference = database.getReference("players");

        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("inGame").setValue(player.getInGame());
        reference.child(player.getId()).child("isConnected").setValue(player.getConnected());
        reference.child(player.getId()).child("isSearchingGame").setValue(player.getSearchingGame());
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


    public void setIsConnectedByPlayer(Boolean isConnected, Player player){
        DatabaseReference reference = database.getReference("players");
        reference.child(player.getId()).child("isConnected").setValue(isConnected);
    }

    public void setInGameByPlayer(Boolean inGame, Player player){
        DatabaseReference reference = database.getReference("players");
        reference.child(player.getId()).child("inGame").setValue(inGame);
    }

    public void setIsSearchingGameByPlayer(Boolean isSearchingPlayer, Player player){
        DatabaseReference reference = database.getReference("players");
        reference.child(player.getId()).child("isSearchingGame").setValue(isSearchingPlayer);
    }

    private List<Player> getPlayerInGame(){
        List<Player> playersInGame = new ArrayList<>();
        for (int i = 0; i< playerList.size();i++){
            if(playerList.get(i).getInGame() == Utils.INGAME && playerList.get(i).getConnected() == Utils.CONNECTED){
                playersInGame.add(playerList.get(i));
            }
        }
        return playersInGame;
    }

    private List<Player> getPlayerNotInGame(Player player1){
        List<Player> playersNotInGame = new ArrayList<>();
        for (int i = 0; i< playerList.size();i++){
            if((!playerList.get(i).getInGame() == Utils.INGAME) && playerList.get(i).getConnected() == Utils.CONNECTED && playerList.get(i).getSearchingGame() == Utils.SEARCHINGGAME
                    && !playerList.get(i).getId().equals(player1.getId())){
                playersNotInGame.add(playerList.get(i));
            }
        }
        return playersNotInGame;

    }

    public void deleteUserById(String id){
        DatabaseReference reference = database.getReference("players");
        reference.child(id).removeValue();
    }

    public List<Player> getAllUser() {
        return playerList;
    }

    public Player findPlayer(Player player1){
        Player player2 = null;

        //search players not In Game
        List<Player> playersSearchingGame = getPlayerNotInGame(player1);

        if (!playersSearchingGame.isEmpty()) {
            Random r = new Random();
            int ind = r.nextInt(playersSearchingGame.size());
            player2 = playersSearchingGame.get(ind);
        }

        return player2;
    }


    /**
     * GAME
     */

    private void initListenerGames(){
        DatabaseReference reference = database.getReference("games");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gameList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String id = child.getKey();
                    Player player1 = child.child("player1").getValue(Player.class);
                    Player player2 = child.child("player2").getValue(Player.class);

                    GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>(){};
                    List<Calculation> calculationList = child.child("calculationList").getValue(genericType);

                    Log.e(TAG, "listenerGame");
                    Game game = new Game(id, player1,player2, calculationList);
                    gameList.add(game);

                    if (onScoreChangeListener!=null && player1!=null && player2!=null) {
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
        Log.e(TAG, "gameList.size : "+gameList.size());
        return game;
    }

    public void deleteGame(Game game){
        DatabaseReference reference = database.getReference("games");
        reference.child(game.getId()).removeValue();
    }


}
