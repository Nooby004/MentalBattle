package com.example.mlallemant.mentalbattle.utils;

import android.util.Log;

import com.example.mlallemant.mentalbattle.ui.friends.FriendModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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

    public interface OnScoreChangeListener {
        void updateScoreUI(Integer score, String playerID);
    }

    public interface OnRageQuitListener {
        void alertUserPlayerRageQuit();
    }

    public interface OnFriendFoundListener {
        void updateFriendFoundUI(List<Player> players);
    }

    public interface OnFriendChangeListener {
        void updateFriendListUI(List<FriendModel> friendList);
    }

    public interface OnDataUserUpdateListener {
        void updateDataUserUI(Player player);
    }

    public interface OnSessionExistListener {
        void notifyUser(boolean isExist);
    }

    public interface OnSessionUpdateListener {
        void updateSessionUI(Session session);
    }

    private final static String TAG = "DatabaseManager";

    private static DatabaseManager instance = null;

    private final FirebaseDatabase database;

    private final List<Player> playerSearchingGameList;
    private final List<Player> playerInLobbyList;
    private final List<FriendModel> friendList;

    private final List<Game> availableGameList;
    private Game currentGame;


    private ValueEventListener gamesListener;
    private ValueEventListener currentGameListener;
    private ValueEventListener friendListener;
    private ValueEventListener sessionListener;

    private DatabaseManager() {
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

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void setScoreChangeListener(final OnScoreChangeListener listener) {
        this.onScoreChangeListener = listener;
    }

    public void setOnRageQuitListener(final OnRageQuitListener listener) {
        this.onRageQuitListener = listener;
    }

    public void setOnFriendFoundListener(final OnFriendFoundListener listener) {
        this.onFriendFoundListener = listener;
    }

    public void setOnFriendChangeListener(final OnFriendChangeListener listener) {
        this.onFriendChangeListener = listener;
    }

    public void setOnDataUserUpdateListener(final OnDataUserUpdateListener listener) {
        this.onDataUserUpdateListener = listener;
    }

    public void setOnSessionExistListener(final OnSessionExistListener listener) {
        this.onSessionExistListener = listener;
    }

    public void setOnSessionUpdateListener(final OnSessionUpdateListener listener) {
        this.onSessionUpdateListener = listener;
    }

    /**
     * PLAYER SEARCHING GAME
     */

    public void insertPlayerSearchingGame(final Player player) {
        final DatabaseReference reference = database.getReference("players").child("searchingGame");
        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("score").setValue(player.getScore());
    }

    public void deletePlayerSearchingPlayer(final Player player) {
        if (player != null) {
            final DatabaseReference reference = database.getReference("players").child("searchingGame");
            reference.child(player.getId()).removeValue();
            playerSearchingGameList.remove(player);
        }
    }

    /**
     * PLAYER IN LOBBY
     */

    public void insertPlayerInLobby(final Player player) {
        final DatabaseReference reference = database.getReference("players").child("inLobby");
        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("nbLose").setValue(player.getNb_lose());
        reference.child(player.getId()).child("nbWin").setValue(player.getNb_win());
        reference.child(player.getId()).child("xp").setValue(player.getXp());
        initListenerFriend(player);
    }


    public void deletePlayerInLobby(final Player player) {
        notifyFriendsYouAreDisconnected(player);
        final DatabaseReference reference = database.getReference("players").child("inLobby");
        reference.child(player.getId()).removeValue();
    }


    /**
     * REGISTERED USER
     */

    public void insertRegisteredPlayer(final Player player) {
        final DatabaseReference reference = database.getReference("players").child("registered");
        reference.child(player.getId()).child("name").setValue(player.getName());
        reference.child(player.getId()).child("nbLose").setValue(player.getNb_lose().toString());
        reference.child(player.getId()).child("nbWin").setValue(player.getNb_win().toString());
        reference.child(player.getId()).child("xp").setValue(player.getXp().toString());
    }


    public void getCurrentUserDataById(final String id) {
        final DatabaseReference reference = database.getReference("players").child("registered").child(id);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String id = dataSnapshot.getKey();
                    final String name = (String) dataSnapshot.child("name").getValue();
                    final String nb_win = (String) dataSnapshot.child("nbWin").getValue();
                    final String nb_lose = (String) dataSnapshot.child("nbLose").getValue();
                    final String xp = (String) dataSnapshot.child("xp").getValue();

                    final Player currentPlayer = new Player(id, name, 0, Integer.parseInt(nb_win), Integer.parseInt(nb_lose), Integer.parseInt(xp));

                    onDataUserUpdateListener.updateDataUserUI(currentPlayer);
                } else {
                    onDataUserUpdateListener.updateDataUserUI(null);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }


    public void setCurrentPlayerXp(final Player currentPlayer, final int xp) {
        final DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId());
        reference.child("xp").setValue(String.valueOf(xp));
    }

    public void setNbWinLoseByPlayer(final Player currentPlayer, final int nbWin, final int nbLose) {
        final DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId());
        reference.child("nbLose").setValue(String.valueOf(nbLose));
        reference.child("nbWin").setValue(String.valueOf(nbWin));
    }

    /**
     * FRIEND
     */

    public void insertFriend(final Player currentPlayer, final Player friend) {
        final DatabaseReference reference = database.getReference("players").child("registered");
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


    public void initFriendList() {
        if (friendList != null) friendList.clear();
    }

    public void deleteFriend(final Player currentPlayer, final Player friend) {
        final DatabaseReference reference = database.getReference("players").child("registered");
        reference.child(currentPlayer.getId()).child("friends").child(friend.getId()).removeValue();
        reference.child(friend.getId()).child("friends").child(currentPlayer.getId()).removeValue();
    }

    public void initListenerFriend(final Player currentPlayer) {
        final DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends");

        friendListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                friendList.clear();
                if (dataSnapshot.exists()) {

                    for (final DataSnapshot result : dataSnapshot.getChildren()) {
                        final String id = result.getKey();
                        final String name = (String) result.child("name").getValue();
                        final Boolean isConnected = (Boolean) result.child("connected").getValue();
                        final String friendAcq = (String) result.child("ack").getValue();
                        final String playReq = (String) result.child("playReq").getValue();
                        final String xp = (String) result.child("xp").getValue();
                        final String nbWin = (String) result.child("nbWin").getValue();
                        final String nbLose = (String) result.child("nbLose").getValue();

                        if (id != null && name != null && isConnected != null && friendAcq != null && playReq != null && xp != null && nbWin != null && nbLose != null) {
                            final FriendModel friendModel = new FriendModel(new Player(id, name, 0, Integer.parseInt(nbWin), Integer.parseInt(nbLose), Integer.parseInt(xp)), isConnected, friendAcq, playReq, xp, null);
                            if (checkFriendIsOkToAdd(friendModel)) {
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
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }

    public List<FriendModel> getFriendList() {
        return friendList;
    }

    private boolean checkFriendIsOkToAdd(final FriendModel friendModel) {
        boolean isOk = false;

        if (friendModel.getConnected() != null && friendModel.getPlayReq() != null && friendModel.getFriendAcq() != null && friendModel.getPlayer().getName() != null) {
            if (!friendModel.getPlayReq().equals("") && !friendModel.getFriendAcq().equals("") && !friendModel.getPlayer().getName().equals("")) {
                isOk = true;
            }
        }

        return isOk;
    }

    public void notifyFriendYouAreConnected(final Player currentPlayer, final Player friend) {
        final DatabaseReference ref = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        ref.child("connected").setValue(true);
    }

    public void notifyFriendYourProgress(final Player currentPlayer, final Player friend) {
        final DatabaseReference ref = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        ref.child("xp").setValue(currentPlayer.getXp().toString());
        ref.child("nbWin").setValue(currentPlayer.getNb_win().toString());
        ref.child("nbLose").setValue(currentPlayer.getNb_lose().toString());

    }

    private void notifyFriendsYouAreConnected(final Player currentPlayer) {
        //NOTIFY YOUR FRIEND YOU ARE CONNECTED

        if (!friendList.isEmpty()) {
            for (int i = 0; i < friendList.size(); i++) {
                final DatabaseReference ref = database.getReference("players").child("registered").child(friendList.get(i).getPlayer().getId()).child("friends").child(currentPlayer.getId());
                ref.child("connected").setValue(true);
            }
        }
    }

    public void notifyFriendsYouAreDisconnected(final Player currentPlayer) {
        //NOTIFY YOUR FRIEND YOU ARE DISCONNECTED
        final DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends");
        reference.removeEventListener(friendListener);

        if (!friendList.isEmpty()) {
            for (int i = 0; i < friendList.size(); i++) {
                final DatabaseReference ref = database.getReference("players").child("registered").child(friendList.get(i).getPlayer().getId()).child("friends").child(currentPlayer.getId());
                ref.child("connected").setValue(false);
            }
        }
    }

    public void ackFriend(final Player currentPlayer, final Player friend) {
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("ack").setValue(Utils.ACK_OK);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("ack").setValue(Utils.ACK_OK);

        notifyFriendsYouAreConnected(currentPlayer);
    }

    public void askToPlayWith(final Player currentPlayer, final Player friend) {
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_REQUEST_SENT);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_REQUEST_RECEIVED);
    }

    public void acceptToPlayWith(final Player currentPlayer, final Player friend) {
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_OK);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_OK);
    }

    public void declineToPlayWith(final Player currentPlayer, final Player friend) {
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_CANCEL);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_CANCEL);
    }

    public void resetToPlayWith(final Player currentPlayer, final Player friend) {
        DatabaseReference reference = database.getReference("players").child("registered").child(currentPlayer.getId()).child("friends").child(friend.getId());
        reference.child("playReq").setValue(Utils.PLAY_KO);

        reference = database.getReference("players").child("registered").child(friend.getId()).child("friends").child(currentPlayer.getId());
        reference.child("playReq").setValue(Utils.PLAY_KO);
    }


    public void findFriend(final String username) {
        final DatabaseReference reference = database.getReference("players").child("registered");
        final Query query = reference.orderByChild("name").startAt(username)
                .endAt(username + "\uf8ff");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0

                    final List<Player> players = new ArrayList<>();
                    for (final DataSnapshot result : dataSnapshot.getChildren()) {
                        if (onFriendFoundListener != null) {

                            final String id = result.getKey();
                            final String username = (String) result.child("name").getValue();
                            final String nb_win = (String) result.child("nbWin").getValue();
                            final String nb_lose = (String) result.child("nbLose").getValue();
                            final String xp = (String) result.child("xp").getValue();

                            final Player player = new Player(id, username, 0, Integer.parseInt(nb_win), Integer.parseInt(nb_lose), Integer.parseInt(xp));
                            if (checkFriendFoundIsOk(player)) {
                                players.add(player);
                            }
                        }
                    }

                    if (onFriendFoundListener != null) {
                        onFriendFoundListener.updateFriendFoundUI(players);
                    }


                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }


    public boolean checkFriendFoundIsOk(final Player player) {
        boolean isOK = false;

        for (int i = 0; i < friendList.size(); i++) {
            if (!friendList.get(i).getPlayer().getId().equals(player.getId())) {
                isOK = true;
            }
        }
        if (friendList.isEmpty()) isOK = true;
        return isOK;
    }

    /**
     * GAME
     */

    public void initListenerGames() {
        final DatabaseReference reference = database.getReference("games").child("availableGame");

        gamesListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                for (final DataSnapshot child : dataSnapshot.getChildren()) {

                    final String id = child.getKey();
                    final Player player1 = child.child("player1").getValue(Player.class);
                    final Player player2 = child.child("player2").getValue(Player.class);
                    final GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {
                    };
                    final List<Calculation> calculationList = child.child("calculationList").getValue(genericType);
                    //Log.e(TAG, "listenerGame");
                    final Game game = new Game(id, player1, player2, calculationList);
                    addAndUpdateAvailableGame(game);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    public void initListenerCurrentGame(final Game game) {

        final DatabaseReference reference = database.getReference("games").child("inProgressGame");
        reference.removeEventListener(gamesListener);

        currentGameListener = reference.child(game.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("player1") && dataSnapshot.hasChild("player2") && dataSnapshot.hasChild("calculationList")) {

                        final Player player1 = dataSnapshot.child("player1").getValue(Player.class);
                        final Player player2 = dataSnapshot.child("player2").getValue(Player.class);

                        final GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {
                        };
                        final List<Calculation> calculationList = dataSnapshot.child("calculationList").getValue(genericType);

                        final Game currentGame = new Game(game.getId(), player1, player2, calculationList);

                        addAndUpdateCurrentGame(currentGame);
                        if (onScoreChangeListener != null && player1 != null && player2 != null) {
                            onScoreChangeListener.updateScoreUI(player1.getScore(), player1.getId());
                            onScoreChangeListener.updateScoreUI(player2.getScore(), player2.getId());
                        }
                    } else {
                        Log.e(TAG, "Structure Database in error");
                        //Display "Error with database, try again"
                    }
                } else {
                    Log.e(TAG, "GAME NOT EXIST");
                    reference.child(game.getId()).removeEventListener(currentGameListener);
                    initListenerGames();

                    if (onRageQuitListener != null) {
                        onRageQuitListener.alertUserPlayerRageQuit();
                    }
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    private void addAndUpdateAvailableGame(final Game game) {
        Boolean isInList = false;

        for (int i = 0; i < availableGameList.size(); i++) {
            if (availableGameList.get(i).getId().equals(game.getId())) {
                availableGameList.set(i, game);
                isInList = true;
            }
        }
        if (!isInList) {
            availableGameList.add(game);
        }
    }

    public void getAvailableGameById(final String idGame) {
        final DatabaseReference reference = database.getReference("games").child("availableGame").child(idGame);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Player player1 = dataSnapshot.child("player1").getValue(Player.class);
                    final Player player2 = dataSnapshot.child("player2").getValue(Player.class);

                    final GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {
                    };
                    final List<Calculation> calculationList = dataSnapshot.child("calculationList").getValue(genericType);

                    final Game game = new Game(idGame, player1, player2, calculationList);
                    currentGame = game;
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }


    private void addAndUpdateCurrentGame(final Game game) {
        currentGame = game;
    }

    public Game getCurrentGame() {
        return currentGame;
    }


    public void insertAvailableGame(final Game game) {
        final DatabaseReference reference = database.getReference("games").child("availableGame");

        reference.child(game.getId()).child("player1").setValue(game.getPlayer1());
        reference.child(game.getId()).child("player2").setValue(game.getPlayer2());
        for (int i = 0; i < game.getCalculationList().size(); i++) {
            reference.child(game.getId()).child("calculationList").child(String.valueOf(i)).setValue(game.getCalculationList().get(i));
        }
    }

    public void insertInProgressGame(final Game game) {
        final DatabaseReference reference = database.getReference("games").child("inProgressGame");

        reference.child(game.getId()).child("player1").setValue(game.getPlayer1());
        reference.child(game.getId()).child("player2").setValue(game.getPlayer2());
        for (int i = 0; i < game.getCalculationList().size(); i++) {
            reference.child(game.getId()).child("calculationList").child(String.valueOf(i)).setValue(game.getCalculationList().get(i));
        }
        currentGame = game;
    }


    public Game getAvailableGame(final Game availableGame) {
        Game game;

        for (int i = 0; i < availableGameList.size(); i++) {
            if (availableGameList.get(i).getId().equals(availableGame.getId())) {
                game = availableGameList.get(i);
                if (game.getPlayer1() != null && game.getPlayer2() != null && game.getCalculationList() != null) {
                    return game;
                }
            }
        }
        return null;
    }


    public void setScorePlayer1ByIdGame(final Integer score, final String id) {
        final DatabaseReference reference = database.getReference("games").child("inProgressGame");
        reference.child(id).child("player1").child("score").setValue(score);
    }

    public void setScorePlayer2ByIdGame(final Integer score, final String id) {
        final DatabaseReference reference = database.getReference("games").child("inProgressGame");
        reference.child(id).child("player2").child("score").setValue(score);
    }


    public void insertPlayer1InAvailableGame(final Player player1, final Game game) {
        final DatabaseReference reference = database.getReference("games").child("availableGame");
        reference.child(game.getId()).child("player1").setValue(player1);
    }

    public void insertPlayer2InAvailableGame(final Player player2, final Game game) {
        final DatabaseReference reference = database.getReference("games").child("availableGame");
        reference.child(game.getId()).child("player2").setValue(player2);
    }


    public Game findAvailableGame() {
        Game game = null;

        for (int i = 0; i < availableGameList.size(); i++) {
            if (availableGameList.get(i).getPlayer1().getId().equals("") || availableGameList.get(i).getPlayer2().getId().equals("")) {
                game = availableGameList.get(i);
            }
        }
        Log.e(TAG, "availableGameList.size : " + availableGameList.size());
        return game;
    }


    public void deleteAvailableGame(final Game game) {
        Log.e(TAG, game.getId());
        final DatabaseReference reference = database.getReference("games").child("availableGame");
        reference.child(game.getId()).removeValue();
        deleteAvailableGameById(game.getId());
    }

    private void deleteAvailableGameById(final String idGame) {
        for (int i = 0; i < availableGameList.size(); i++) {
            if (availableGameList.get(i).getId().equals(idGame)) {
                availableGameList.remove(i);
            }
        }
    }

    public void deleteCurrentGame(final Game game) {
        final DatabaseReference reference = database.getReference("games").child("inProgressGame");
        reference.child(game.getId()).removeValue();
        currentGame = null;
    }


    /**
     * SESSION
     */

    public void insertSession(final Session session) {
        final DatabaseReference reference = database.getReference("sessions");
        reference.child(session.getIdSession()).child("name").setValue(session.getName());
        reference.child(session.getIdSession()).child("password").setValue(session.getPassword());
        reference.child(session.getIdSession()).child("state").setValue(session.getState());
        for (int i = 0; i < session.getCalculationList().size(); i++) {
            reference.child(session.getIdSession()).child("calculationList").child(String.valueOf(i)).setValue(session.getCalculationList().get(i));
        }
    }

    public void updateCalculationListInSession(final Session session, final List<Calculation> calculationList) {
        final DatabaseReference reference = database.getReference("sessions");
        for (int i = 0; i < calculationList.size(); i++) {
            reference.child(session.getIdSession()).child("calculationList").child(String.valueOf(i)).setValue(calculationList.get(i));
        }
    }


    public void insertPlayerInSession(final Session session, final Player player) {
        final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).setValue(player);
        deletePlayerInLobby(player);
    }

    public void removePlayerInSession(final Session session, final Player player) {
        final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).removeValue();
    }

    public void deleteSession(final Session session) {
        final DatabaseReference reference = database.getReference("sessions");
        reference.child(session.getIdSession()).removeValue();
    }

    public void updatePlayerReady(final Session session, final Player player, final String rdy) {
        final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).child("ready").setValue(rdy);
    }

    public void updatePlayerNew(final Session session, final Player player, final String new_) {
        final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).child("new_").setValue(new_);
    }

    public void updateStateSession(final Session session, final String state) {
        final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("state");
        reference.setValue(state);
    }

    public void updateScoreCurrentPlayerInSession(final Session session, final Player player, final int score) {
        final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession()).child("players");
        reference.child(player.getId()).child("score").setValue(score);

    }

    public void initCheckSessionExist(final Session session) {
        final DatabaseReference reference = database.getReference("sessions");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(session.getIdSession()).exists()) {

                    final String state = (String) dataSnapshot.child(session.getIdSession()).child("state").getValue();
                    if (state.equals(Utils.SESSION_STATE_WAITING)) {
                        onSessionExistListener.notifyUser(true);
                    } else {
                        onSessionExistListener.notifyUser(false);
                    }
                } else {
                    onSessionExistListener.notifyUser(false);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }

    public void initListenerCurrentSession(final Session session) {
        final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession());
        sessionListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String name = (String) dataSnapshot.child("name").getValue();
                    final String password = (String) dataSnapshot.child("password").getValue();
                    final String state = (String) dataSnapshot.child("state").getValue();

                    final GenericTypeIndicator<List<Calculation>> genericType = new GenericTypeIndicator<List<Calculation>>() {
                    };
                    final List<Calculation> calculationList = dataSnapshot.child("calculationList").getValue(genericType);

                    final Session session_ = new Session(name, password, state, calculationList);
                    for (final DataSnapshot playersSnapshot : dataSnapshot.child("players").getChildren()) {
                        final Player player = playersSnapshot.getValue(Player.class);
                        if (player.getId() != null && player.getNew_() != null && player.getReady() != null && player.getName() != null) {
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
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }

    public void removeListenerCurrentSession(final Session session) {
        if (sessionListener != null) {
            final DatabaseReference reference = database.getReference("sessions").child(session.getIdSession());
            reference.removeEventListener(sessionListener);
        }
    }

}
