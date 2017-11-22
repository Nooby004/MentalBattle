package com.example.mlallemant.mentalbattle.Utils;

import android.util.Log;

import com.example.mlallemant.mentalbattle.UI.Friends.FriendModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class DatabaseManager {

    private OnScoreChangeListener onScoreChangeListener;
    private OnRageQuitListener onRageQuitListener;
    private OnFriendFoundListener onFriendFoundListener;
    private OnFriendChangeListener onFriendChangeListener;
    private OnDataUserUpdateListener onDataUserUpdateListener;
    private OnSessionExistListener onSessionExistListener;
    private OnSessionUpdateListener onSessionUpdateListener;

    public interface OnScoreChangeListener{
        void updateScoreUI(Integer score, String playerID);
    }

    public interface OnRageQuitListener{
        void alertUserPlayerRageQuit();
    }

    public interface OnFriendFoundListener{
        void updateFriendFoundUI(List<Player> players);
    }

    public interface OnFriendChangeListener{
        void updateFriendListUI(List<FriendModel> friendList);
    }

    public interface OnDataUserUpdateListener{
        void updateDataUserUI(Player player);
    }

    public interface OnSessionExistListener{
        void notifyUser(boolean isExist);
    }

    public interface OnSessionUpdateListener{
        void updateSessionUI(Session session);
    }

    private final static String TAG = "DatabaseManager";

    private static DatabaseManager instance = null;
    
    private FirebaseDatabase database;

    private List<Player> playerSearchingGameList;
    private List<Player> playerInLobbyList;
    private List<FriendModel> friendList;

    private List<Game> availableGameList;
    private Game currentGame;


    private ValueEventListener gamesListener;
    private ValueEventListener currentGameListener;
    private ValueEventListener friendListener;
    private ValueEventListener sessionListener;

    private DatabaseManager(){
        database = FirebaseDatabase.getInstance();
        playerSearchingGameList = new ArrayList<>();
        playerInLobbyList = new ArrayList<>();
        availableGameList = new ArrayList<>();
        friendList = new ArrayList<>();

        initListenerGames();

        this.onRageQuitListener = null;
        this.onScoreChangeListener = null;
        this.onFriendFoundListener = null;
        this.onFriendChangeListener = null;
        this.onDataUserUpdateListener = null;
        this.onSessionExistListener = null;
        this.onSessionUpdateListener = null;
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

    public void setOnFriendFoundListener(OnFriendFoundListener listener){
        this.onFriendFoundListener = listener;
    }

    public void setOnFriendChangeListener(OnFriendChangeListener listener) {
        this.onFriendChangeListener = listener;
    }

    public void setOnDataUserUpdateListener(OnDataUserUpdateListener listener) {
        this.onDataUserUpdateListener = listener;
    }

    public void setOnSessionExistListener(OnSessionExistListener listener){
        this.onSessionExistListener = listener;
    }

    public void setOnSessionUpdateListener(OnSessionUpdateListener listener) {
        this.onSessionUpdateListener = listener;
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
        if (player != null) {
            DatabaseReference reference = database.getReference("players").child("searchingGame");
            reference.child(player.getId()).removeValue();
            playerSearchingGameList.remove(player);
        }
    }

    /**
     * PLAYER IN LOBBY
     */

    public void insertPlayerInLobby(Player player) {
        DatabaseReference reference = database.getReference("players").child("inLobby");
        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("nbLose").setValue(player.getNb_lose());
        reference.child(player.getId()).child("nbWin").setValue(player.getNb_win());
        reference.child(player.getId()).child("xp").setValue(player.getXp());
        initListenerFriend(player);
    }


    public void deletePlayerInLobby(Player player){
        notifyFriendsYouAreDisconnected(player);
        DatabaseReference reference = database.getReference("players").child("inLobby");
        reference.child(player.getId()).removeValue();
    }


    /**
     * REGISTERED USER
     */

    public void insertRegisteredPlayer(Player player) {
        DatabaseReference reference = database.getReference("players").child("registered");
        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("nbLose").setValue(player.getNb_lose().toString());
        reference.child(player.getId()).child("nbWin").setValue(player.getNb_win().toString());
        reference.child(player.getId()).child("xp").setValue(player.getXp().toString());
    }


    public void getCurrentUserDataById(String id) {
        DatabaseReference reference = database.getReference("players").child("registered").child(id);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String id = dataSnapshot.getKey();
                    String name = (String) dataSnapshot.child("name").getValue();
                    String nb_win = (String)  dataSnapshot.child("nbWin").getValue();
                    String nb_lose = (String)  dataSnapshot.child("nbLose").getValue();
                    String xp = (String)  dataSnapshot.child("xp").getValue();

                    Player currentPlayer = new Player(id, name, 0, Integer.parseInt(nb_win), Integer.parseInt(nb_lose),Integer.parseInt(xp));

                    onDataUserUpdateListener.updateDataUserUI(currentPlayer);
                } else {
                    onDataUserUpdateListener.updateDataUserUI(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void setCurrentPlayerXp(Player currentPlayer, int xp){
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId());
        reference.child("xp").setValue(String.valueOf(xp));
    }

    public void setNbWinLoseByPlayer(Player currentPlayer, int nbWin, int nbLose){
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId());
        reference.child("nbLose").setValue(String.valueOf(nbLose));
        reference.child("nbWin").setValue(String.valueOf(nbWin));
    }

    /**
     * FRIEND
     */

    public void insertFriend(Player currentPlayer, Player friend){
        DatabaseReference reference = database.getReference("players").child("registered");
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).child("name").setValue(friend.getName());
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).child("ack").setValue(Utils.ACK_REQUEST_SENT);
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).child("connected").setValue(false);
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).child("playReq").setValue(Utils.PLAY_KO);
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).child("xp").setValue(friend.getXp().toString());
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).child("nbWin").setValue(friend.getNb_win().toString());
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).child("nbLose").setValue(friend.getNb_lose().toString());

        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).child("name").setValue(currentPlayer.getName());
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).child("ack").setValue(Utils.ACK_REQUEST_RECEIVED);
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).child("connected").setValue(true);
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).child("playReq").setValue(Utils.PLAY_KO);
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).child("xp").setValue(currentPlayer.getXp().toString());
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).child("nbWin").setValue(currentPlayer.getNb_win().toString());
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).child("nbLose").setValue(currentPlayer.getNb_lose().toString());

    }


    public void initFriendList(){
        if (friendList != null) friendList.clear();
    }

    public void deleteFriend(Player currentPlayer, Player friend){
        DatabaseReference reference = database.getReference("players").child("registered");
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).removeValue();
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).removeValue();
    }

    public void initListenerFriend(final Player currentPlayer){
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends");

        friendListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendList.clear();
                if (dataSnapshot.exists()){

                    for (DataSnapshot result : dataSnapshot.getChildren()){
                        String id = result.getKey();
                        String name = (String) result.child("name").getValue();
                        Boolean isConnected = (Boolean) result.child("connected").getValue();
                        String friendAcq = (String) result.child("ack").getValue();
                        String playReq = (String) result.child("playReq").getValue();
                        String xp = (String) result.child("xp").getValue();
                        String nbWin = (String) result.child("nbWin").getValue();
                        String nbLose = (String) result.child("nbLose").getValue();

                        if (id != null && name != null && isConnected != null && friendAcq != null && playReq != null && xp != null && nbWin != null && nbLose != null) {
                            FriendModel friendModel = new FriendModel(new Player(id, name, 0,Integer.parseInt(nbWin) ,Integer.parseInt(nbLose) ,Integer.parseInt(xp)), isConnected, friendAcq, playReq, xp, null);
                            if (checkFriendIsOkToAdd(friendModel)){
                                friendList.add(friendModel);
                            }
                        }
                    }

                    //SEND LIST BY INTERFACE TO UI
                    if (onFriendChangeListener != null) {
                        onFriendChangeListener.updateFriendListUI(friendList);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public List<FriendModel> getFriendList(){
        return friendList;
    }

    private boolean checkFriendIsOkToAdd(FriendModel friendModel){
        boolean isOk = false;

        if (friendModel.getConnected() != null && friendModel.getPlayReq() != null && friendModel.getFriendAcq() != null && friendModel.getPlayer().getName() != null){
            if (!friendModel.getPlayReq().equals("") && !friendModel.getFriendAcq().equals("") && !friendModel.getPlayer().getName().equals("")){
                isOk = true;
            }
        }

        return isOk;
    }

    public void notifyFriendYouAreConnected(Player currentPlayer, Player friend) {
        DatabaseReference ref = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        ref.child("connected").setValue(true);
    }

    public void notifyFriendYourProgress(Player currentPlayer, Player friend) {
        DatabaseReference ref = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        ref.child("xp").setValue(currentPlayer.getXp().toString());
        ref.child("nbWin").setValue(currentPlayer.getNb_win().toString());
        ref.child("nbLose").setValue(currentPlayer.getNb_lose().toString());

    }

    private void notifyFriendsYouAreConnected(Player currentPlayer){
        //NOTIFY YOUR FRIEND YOU ARE CONNECTED

        if (!friendList.isEmpty()){
            for (int i = 0; i<friendList.size(); i++){
                DatabaseReference ref = database.getReference("players").child("registered").child(friendList.get(i).getPlayer().getId()).child("friends").child(currentPlayer.getId());
                ref.child("connected").setValue(true);
            }
        }
    }

    public void notifyFriendsYouAreDisconnected(Player currentPlayer){
        //NOTIFY YOUR FRIEND YOU ARE DISCONNECTED
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends");
        reference.removeEventListener(friendListener);

        if (!friendList.isEmpty()){
            for (int i = 0; i<friendList.size(); i++){
                DatabaseReference ref = database.getReference("players").child("registered").child(friendList.get(i).getPlayer().getId()).child("friends").child(currentPlayer.getId());
                ref.child("connected").setValue(false);
            }
        }
    }

    public void ackFriend(Player currentPlayer, Player friend){
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("ack").setValue(Utils.ACK_OK);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("ack").setValue(Utils.ACK_OK);

        notifyFriendsYouAreConnected(currentPlayer);
    }

    public void askToPlayWith(Player currentPlayer, Player friend){
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_REQUEST_SENT);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_REQUEST_RECEIVED);
    }

    public void acceptToPlayWith(Player currentPlayer, Player friend){
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_OK);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_OK);
    }

    public void declineToPlayWith(Player currentPlayer, Player friend){
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_CANCEL);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_CANCEL);
    }

    public void resetToPlayWith(Player currentPlayer, Player friend) {
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_KO);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_KO);
    }


    public void findFriend(String username){
        DatabaseReference reference = database.getReference("players").child("registered");
        Query query = reference.orderByChild("name").startAt(username)
                .endAt(username+"\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0

                    List<Player> players = new ArrayList<>();
                    for (DataSnapshot result : dataSnapshot.getChildren()) {
                        if (onFriendFoundListener != null) {

                            String id = result.getKey();
                            String username = (String) result.child("name").getValue();
                            String nb_win = (String) result.child("nbWin").getValue();
                            String nb_lose = (String) result.child("nbLose").getValue();
                            String xp = (String) result.child("xp").getValue();

                            Player player = new Player(id, username, 0, Integer.parseInt(nb_win), Integer.parseInt(nb_lose),Integer.parseInt(xp));
                            if (checkFriendFoundIsOk(player)) {
                                players.add(player);
                            }
                        }
                    }

                    if (onFriendFoundListener != null){
                        onFriendFoundListener.updateFriendFoundUI(players);
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public boolean checkFriendFoundIsOk(Player player){
        boolean isOK = false;

        for (int i = 0; i < friendList.size(); i++){
            if (!friendList.get(i).getPlayer().getId().equals(player.getId())){
               isOK = true;
            }
        }
        if (friendList.isEmpty()) isOK = true;
        return isOK;
    }

    /**
     * GAME
     */

    public void initListenerGames(){
        DatabaseReference reference = database.getReference("games").child("availableGame");

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
                    addAndUpdateAvailableGame(game);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    public void initListenerCurrentGame(final Game game){

        final DatabaseReference reference = database.getReference("games").child("inProgressGame");
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

                        addAndUpdateCurrentGame(currentGame);
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

    private void addAndUpdateAvailableGame(Game game){
        Boolean isInList = false;

        for (int i = 0; i<availableGameList.size(); i++){
            if (availableGameList.get(i).getId().equals(game.getId())){
                availableGameList.set(i, game);
                isInList = true;
            }
        }
        if (!isInList){
            availableGameList.add(game);
        }
    }

    public void getAvailableGameById(final String idGame){
        DatabaseReference reference = database.getReference("games").child("availableGame").child(idGame);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Player player1 = dataSnapshot.child("player1").getValue(Player.class);
                    Player player2 = dataSnapshot.child("player2").getValue(Player.class);

                    GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {};
                    List<Calculation> calculationList = dataSnapshot.child("calculationList").getValue(genericType);

                    Game game = new Game(idGame, player1, player2, calculationList);
                    currentGame = game;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void addAndUpdateCurrentGame(Game game){
        currentGame = game;
    }

    public Game getCurrentGame(){
        return currentGame;
    }


    public void insertAvailableGame(Game game) {
        DatabaseReference reference = database.getReference("games").child("availableGame");

        reference.child(game.getId()).child("player1").setValue(game.getPlayer1());
        reference.child(game.getId()).child("player2").setValue(game.getPlayer2());
        for (int i=0; i<game.getCalculationList().size(); i++) {
            reference.child(game.getId()).child("calculationList").child(String.valueOf(i)).setValue(game.getCalculationList().get(i));
        }
    }

    public void insertInProgressGame(Game game) {
        DatabaseReference reference = database.getReference("games").child("inProgressGame");

        reference.child(game.getId()).child("player1").setValue(game.getPlayer1());
        reference.child(game.getId()).child("player2").setValue(game.getPlayer2());
        for (int i=0; i<game.getCalculationList().size(); i++) {
            reference.child(game.getId()).child("calculationList").child(String.valueOf(i)).setValue(game.getCalculationList().get(i));
        }
        currentGame = game;
    }


    public Game getAvailableGame(Game availableGame){
        Game game;

        for (int i=0; i<availableGameList.size();i++){
            if (availableGameList.get(i).getId().equals(availableGame.getId())) {
                game = availableGameList.get(i);
                if (game.getPlayer1() != null && game.getPlayer2() != null && game.getCalculationList() != null){
                    return game;
                }
            }
        }
        return null;
    }


    public void setScorePlayer1ByIdGame(Integer score, String id){
        DatabaseReference reference = database.getReference("games").child("inProgressGame");
        reference.child(id).child("player1").child("score").setValue(score);
    }

    public void setScorePlayer2ByIdGame(Integer score, String id){
        DatabaseReference reference = database.getReference("games").child("inProgressGame");
        reference.child(id).child("player2").child("score").setValue(score);
    }



    public void insertPlayer1InAvailableGame(Player player1, Game game){
        DatabaseReference reference = database.getReference("games").child("availableGame");
        reference.child(game.getId()).child("player1").setValue(player1);
    }

    public void insertPlayer2InAvailableGame(Player player2, Game game){
        DatabaseReference reference = database.getReference("games").child("availableGame");
        reference.child(game.getId()).child("player2").setValue(player2);
    }



    public Game findAvailableGame(){
        Game game = null;

        for (int i = 0; i< availableGameList.size(); i++){
            if (availableGameList.get(i).getPlayer1().getId().equals("") || availableGameList.get(i).getPlayer2().getId().equals("")){
                game = availableGameList.get(i);
            }
        }
        Log.e(TAG, "availableGameList.size : " + availableGameList.size());
        return game;
    }


    public void deleteAvailableGame(Game game){
        Log.e(TAG, game.getId());
        DatabaseReference reference = database.getReference("games").child("availableGame");
        reference.child(game.getId()).removeValue();
        deleteAvailableGameById(game.getId());
    }

    private void deleteAvailableGameById(String idGame){
        for (int i = 0 ; i<availableGameList.size(); i++) {
            if (availableGameList.get(i).getId().equals(idGame)){
                availableGameList.remove(i);
            }
        }
    }

    public void deleteCurrentGame(Game game){
        DatabaseReference reference = database.getReference("games").child("inProgressGame");
        reference.child(game.getId()).removeValue();
        currentGame = null;
    }


    /**
     * SESSION
     */

    public void insertSession(Session session) {
        DatabaseReference reference = database.getReference("sessions");
        reference.child(session.getIdSession()).child("name").setValue(session.getName());
        reference.child(session.getIdSession()).child("password").setValue(session.getPassword());
        reference.child(session.getIdSession()).child("state").setValue(session.getState());
        for (int i=0; i<session.getCalculationList().size(); i++) {
            reference.child(session.getIdSession()).child("calculationList").child(String.valueOf(i)).setValue(session.getCalculationList().get(i));
        }
    }

    public void updateCalculationListInSession(Session session, List<Calculation> calculationList){
        DatabaseReference reference = database.getReference("sessions");
        for (int i=0; i<calculationList.size(); i++) {
            reference.child(session.getIdSession()).child("calculationList").child(String.valueOf(i)).setValue(calculationList.get(i));
        }
    }


    public void insertPlayerInSession(Session session, Player player) {
        DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).setValue(player);
        deletePlayerInLobby(player);
    }

    public void removePlayerInSession(Session session, Player player) {
        DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).removeValue();
    }

    public void deleteSession(Session session) {
        DatabaseReference reference = database.getReference("sessions");
        reference.child(session.getIdSession()).removeValue();
    }

    public void updatePlayerReady(Session session, Player player, String rdy){
        DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).child("ready").setValue(rdy);
    }

    public void updatePlayerNew(Session session, Player player, String new_){
        DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).child("new_").setValue(new_);
    }

    public void updateStateSession(Session session, String state){
        DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("state");
        reference.setValue(state);
    }

    public void updateScoreCurrentPlayerInSession(Session session, Player player, int score){
        DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).child("score").setValue(score);

    }

    public void initCheckSessionExist(final Session session) {
        DatabaseReference reference = database.getReference("sessions");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(session.getIdSession()).exists()) {

                    String state = (String) dataSnapshot.child(session.getIdSession()).child("state").getValue();
                    if (state.equals(Utils.SESSION_STATE_WAITING)) {
                        onSessionExistListener.notifyUser(true);
                    }else {
                        onSessionExistListener.notifyUser(false);
                    }
                } else {
                    onSessionExistListener.notifyUser(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initListenerCurrentSession(Session session){
        DatabaseReference reference = database.getReference("sessions").child(session.getIdSession());
        sessionListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String name = (String) dataSnapshot.child("name").getValue();
                    String password = (String) dataSnapshot.child("password").getValue();
                    String state = (String) dataSnapshot.child("state").getValue();

                    GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {};
                    List<Calculation> calculationList = dataSnapshot.child("calculationList").getValue(genericType);

                    Session session_ = new Session(name, password, state, calculationList);
                    for (DataSnapshot playersSnapshot : dataSnapshot.child("players").getChildren()) {
                        Player player = playersSnapshot.getValue(Player.class);
                        if (player.getId() != null && player.getNew_() != null && player.getReady() != null && player.getName() != null){
                            session_.addPlayerToSession(player);
                        }
                    }
                    if (onSessionUpdateListener != null) {
                        onSessionUpdateListener.updateSessionUI(session_);
                    }
                } else {
                    onSessionUpdateListener.updateSessionUI(null);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeListenerCurrentSession(Session session){
        if (sessionListener != null) {
            DatabaseReference reference = database.getReference("sessions").child(session.getIdSession());
            reference.removeEventListener(sessionListener);
        }
    }

}
