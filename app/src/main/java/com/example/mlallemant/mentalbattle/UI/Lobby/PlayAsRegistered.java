package com.example.mlallemant.mentalbattle.UI.Lobby;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Friends.CustomAdapter;
import com.example.mlallemant.mentalbattle.UI.Friends.DataModel;
import com.example.mlallemant.mentalbattle.UI.Game.GameActivity;
import com.example.mlallemant.mentalbattle.UI.Login.LoginActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.SearchGameTask;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by m.lallemant on 27/10/2017.
 */

public class PlayAsRegistered extends AppCompatActivity {

    private TextView tv_welcome;
    private Button btn_play;
    private ProgressBar pb_btn_play;
    private TextView tv_logoff;
    private ListView lv_friends;
    private ImageView iv_add_friend;

    public SearchGameTask searchGameTask;
    private Player currentPlayer;
    private Game currentGame;
    private FirebaseAuth mAuth;
    private DatabaseManager db;

    private AlertDialog ad_friend_wants_to_play;

    private Player playerFound;
    private boolean isFirstReqToPlay = false;
    private boolean isSearchingGame = false;

    //Friends
    ArrayList<DataModel> dataModels;
    private static CustomAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_lobby_activity);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().equals("")) {
                db = DatabaseManager.getInstance();
                db.initFriendList();
                String splitName = user.getDisplayName().split(" ")[0];
                currentPlayer = new Player(user.getUid(), splitName, 0);

                Player registeredPlayer = new Player(user.getUid(), user.getDisplayName(),0);
                db.insertRegisteredPlayer(registeredPlayer);
                db.initListenerFriend(currentPlayer);
                db.insertPlayerInLobby(new Player(user.getUid(), user.getDisplayName(), 0));
                initUI();
                initListener();
            } else {
                signOut();
                launchLoginActivity();
            }
        }else {
            signOut();
            launchLoginActivity();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(currentPlayer != null) {
            db.notifyFriendsYouAreDisconnected(currentPlayer);
            db.deletePlayerInLobby(currentPlayer);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        db.insertPlayerInLobby(currentPlayer);
    }

    private void initUI(){
        tv_welcome = (TextView) findViewById(R.id.register_tv_welcome);
        btn_play = (Button) findViewById(R.id.register_btn_play);
        pb_btn_play = (ProgressBar) findViewById(R.id.register_pb_btn_play);
        tv_logoff = (TextView) findViewById(R.id.register_tv_logoff);
        pb_btn_play.setVisibility(View.GONE);
        lv_friends = (ListView) findViewById(R.id.register_lv_friends);
        iv_add_friend = (ImageView) findViewById(R.id.register_iv_add_friend);

        String text = "Welcome " + currentPlayer.getName();
        tv_welcome.setText(text);

        //Friends
        dataModels = new ArrayList<>();

        adapter = new CustomAdapter(dataModels, getApplicationContext());
        lv_friends.setAdapter(adapter);

    }

    private void initListener(){

        //BUTTON PLAY
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUIOnClick();
            }
        });

        //LOG OFF
        tv_logoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.notifyFriendsYouAreDisconnected(currentPlayer);
                signOut();
                launchLoginActivity();
            }
        });


        /**
         * ADD FRIEND
         */

        iv_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerFound = null;

                final AlertDialog.Builder alert = new AlertDialog.Builder(PlayAsRegistered.this, R.style.myDialogTheme );
                alert.setTitle("Search Friend");
                View alertLayout = getLayoutInflater().inflate(R.layout.lobby_search_friend_dialog, null);
                final EditText et_username = (EditText) alertLayout.findViewById(R.id.lobby_et_username);
                final TextView tv_result = (TextView) alertLayout.findViewById(R.id.lobby_tv_result);
                final ProgressBar pg_search = (ProgressBar) alertLayout.findViewById(R.id.lobby_pg_search);
                final ImageView iv_search = (ImageView) alertLayout.findViewById(R.id.lobby_iv_search);
                tv_result.setText("");
                pg_search.setVisibility(View.GONE);

                iv_search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideKeyboard(et_username);
                        tv_result.setText("");
                        String username = et_username.getText().toString();
                        db.findFriend(username);
                        pg_search.setVisibility(View.VISIBLE);
                    }
                });


                //SEARCH RESULT CALLBACK
                db.setOnFriendFoundListener(new DatabaseManager.OnFriendFoundListener() {
                    @Override
                    public void updateFriendFoundUI(Player player) {
                        pg_search.setVisibility(View.GONE);
                        String username = player.getName();
                        String text = "Player found : " + username;
                        tv_result.setText(text);
                        playerFound = player;
                    }
                });

                alert.setView(alertLayout);
                alert.setCancelable(false);
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hideKeyboard(et_username);
                        dialog.cancel();
                    }
                });
                alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (playerFound != null) {
                            db.insertFriend(currentPlayer, playerFound);
                            makeToast("Friend request sent to " + playerFound.getName());
                        } else {
                            makeToast("No user to add");
                        }
                    }
                });
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));
            }
        });


        /**
         * FRIEND LISTENER
         */

        db.setOnFriendChangeListener(new DatabaseManager.OnFriendChangeListener() {
            @Override
            public void updateFriendListUI(final List<DataModel> friendList) {
                adapter.clear();
                adapter.addAll(friendList);

                for (int i = 0; i<friendList.size(); i++) {
                    final int pos = i;

                    if (friendList.get(i) != null){
                        if (friendList.get(i).getPlayReq() != null){

                            if (friendList.get(i).getPlayReq().equals(Utils.PLAY_REQUEST_RECEIVED) && !isFirstReqToPlay ) {
                                isFirstReqToPlay = true;

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlayAsRegistered.this, R.style.myDialogTheme);
                                alertDialogBuilder
                                        .setMessage(friendList.get(pos).getPlayer().getName() + " wants to play with you !" )
                                        .setCancelable(false)
                                        .setPositiveButton("PLAY", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                String idGame = friendList.get(pos).getPlayer().getId() + currentPlayer.getId();
                                                db.getAvailableGameById(idGame);
                                                db.acceptToPlayWith(currentPlayer, friendList.get(pos).getPlayer());
                                                isFirstReqToPlay = false;
                                            }
                                        })
                                        .setNegativeButton("DECLINE", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                db.declineToPlayWith(currentPlayer, friendList.get(pos).getPlayer());
                                                String idGame = friendList.get(pos).getPlayer().getId() + currentPlayer.getId();
                                                db.getAvailableGameById(idGame);
                                                if (currentGame != null) db.deleteAvailableGame(currentGame);
                                                currentGame = null;
                                                isFirstReqToPlay = false;
                                                dialog.cancel();
                                            }
                                        });
                                ad_friend_wants_to_play = alertDialogBuilder.create();
                                ad_friend_wants_to_play.show();
                                ad_friend_wants_to_play.getButton(ad_friend_wants_to_play.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));
                                ad_friend_wants_to_play.getButton(ad_friend_wants_to_play.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));


                            } else if (friendList.get(i).getPlayReq().equals(Utils.PLAY_OK)){
                                currentGame = db.getCurrentGame();
                                db.insertInProgressGame(currentGame);
                                db.initListenerCurrentGame(currentGame);
                                db.deleteAvailableGame(currentGame);

                                //Launch game
                                Intent intent = new Intent(PlayAsRegistered.this, GameActivity.class);
                                intent.putExtra("idGame", currentGame.getId());
                                intent.putExtra("currentPlayerId", currentPlayer.getId());
                                startActivity(intent);
                                finish();

                                db.declineToPlayWith(currentPlayer, friendList.get(pos).getPlayer());
                                isFirstReqToPlay = false;

                            } else if (friendList.get(i).getPlayReq().equals(Utils.PLAY_KO)) {
                                if (ad_friend_wants_to_play != null){
                                    if (ad_friend_wants_to_play.isShowing()){
                                        ad_friend_wants_to_play.cancel();
                                    }
                                }
                            }
                        }
                    }

                }
            }
        });


        lv_friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //ACK TRUE
                final DataModel friend = adapter.getItem(i);

                if (friend.getFriendAcq().equals(Utils.ACK_REQUEST_RECEIVED)) {
                    // if not ack, open dialog in order to accept him
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlayAsRegistered.this,R.style.myDialogTheme);
                    alertDialogBuilder.setTitle("Accept friend");
                    alertDialogBuilder
                            .setMessage("Do you want to accept this friend ?")
                            .setCancelable(false)
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    db.ackFriend(currentPlayer, friend.getPlayer());
                                }
                            })
                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    db.deleteFriend(currentPlayer, friend.getPlayer());
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));
                    alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));

                } else if (friend.getFriendAcq().equals(Utils.ACK_REQUEST_SENT)) {
                    makeToast("Friend request sent");

                } else {
                    //REQUEST FRIEND TO PLAY
                    if (friend.getConnected()) {
                        db.askToPlayWith(currentPlayer, friend.getPlayer());
                        final String idGame = currentPlayer.getId()+friend.getPlayer().getId();
                        Game game = new Game(idGame, currentPlayer, friend.getPlayer());
                        currentGame = game;
                        db.insertAvailableGame(currentGame);
                        db.getAvailableGameById(idGame);

                        final AlertDialog.Builder alert = new AlertDialog.Builder(PlayAsRegistered.this, R.style.myDialogTheme );
                        View alertLayout = getLayoutInflater().inflate(R.layout.lobby_request_play_dialog, null);
                        final TextView tv_loading_text = (TextView) alertLayout.findViewById(R.id.lobby_et_loading_text);
                        String text = "Waiting " + friend.getPlayer().getName() + " ...";
                        tv_loading_text.setText(text);
                        alert.setCancelable(false)
                              .setView(alertLayout)
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        db.declineToPlayWith(currentPlayer,friend.getPlayer());
                                        db.getAvailableGameById(idGame);
                                        db.deleteAvailableGame(currentGame);
                                        currentGame = null;
                                        dialogInterface.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alert.create();
                        alertDialog.show();
                        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));


                    }else {
                        makeToast("Your friend is not connected");
                    }
                }
            }
        });

        lv_friends.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final DataModel friend = adapter.getItem(i);
                final int position = i;
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlayAsRegistered.this,R.style.myDialogTheme);
                alertDialogBuilder.setTitle("Delete friend");
                alertDialogBuilder
                        .setMessage("Are you sure you want to delete this friend ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                adapter.remove(adapter.getItem(position));
                                db.deleteFriend(currentPlayer, friend.getPlayer());
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            }
        });

    }

    private void updateUIOnClick() {
        if (isSearchingGame){ //Cancel search
            isSearchingGame = false;
            searchGameTask.cancel(true);
            pb_btn_play.setVisibility(View.GONE);
            db.deletePlayerSearchingPlayer(currentPlayer);
            btn_play.setText("PLAY");

        } else { //Search player
            isSearchingGame = true;
            pb_btn_play.setVisibility(View.VISIBLE);
            btn_play.setText("CANCEL");
            launchSearchingGameTask();
        }
    }

    private void launchLoginActivity(){
        Intent intent = new Intent(PlayAsRegistered.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchSearchingGameTask(){
        currentGame = null;
        searchGameTask = new SearchGameTask(new SearchGameTask.AsyncResponse() {
            @Override
            public void onFinishTask(Game game) {
                updateUI(game);
            }
        });
        searchGameTask.setParams(currentPlayer, currentGame);
        searchGameTask.execute("");
    }

    private void updateUI(Game game){
        currentGame = searchGameTask.getCurrentGame();
        currentPlayer = searchGameTask.getCurrentPlayer();

        if (game != null) {
            if ((!game.getPlayer1().getId().equals("")) && (!game.getPlayer2().getId().equals(""))) {

                //We insert the currentGame in inProgress
                db.insertInProgressGame(game);
                //We get the current, so we can listen only this game now
                db.initListenerCurrentGame(game);
                //And also delete the availableGame
                db.deleteAvailableGame(game);

                //Launch game
                Intent intent = new Intent(PlayAsRegistered.this, GameActivity.class);
                intent.putExtra("idGame", game.getId());
                intent.putExtra("currentPlayerId", currentPlayer.getId());
                startActivity(intent);
                finish();
            } else {
                db.deleteAvailableGame(game);
                db.deletePlayerSearchingPlayer(currentPlayer);
                btn_play.setEnabled(true);
            }
            pb_btn_play.setVisibility(View.GONE);
        } else {
            launchSearchingGameTask();
        }
    }

    private void signOut(){
        if (currentPlayer != null) db.notifyFriendsYouAreDisconnected(currentPlayer);
        if (db != null) db.initFriendList();
        mAuth.signOut();
        LoginManager.getInstance().logOut();
    }

    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }

    private static String getRandomId(){
        return UUID.randomUUID().toString();
    }

    private void hideKeyboard(EditText et){
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

}
