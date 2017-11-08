package com.example.mlallemant.mentalbattle.UI.Lobby;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by m.lallemant on 27/10/2017.
 */

public class PlayAsRegistered extends AppCompatActivity {

    private final static String TAG = "PlayAsRegistered";

    private CircleImageView iv_profile_user;
    private TextView tv_nb_win_loses;
    private TextView tv_username;
    private ImageView iv_logout;
    private TextView tv_current_rank;
    private TextView tv_current_level;
    private TextView tv_current_xp;
    private ProgressBar pb_progress_xp;
    private TextView tv_next_rank;


    private Button btn_play;
    private ProgressBar pb_btn_play;
    private ListView lv_friends;
    private ImageView iv_add_friend;

    public SearchGameTask searchGameTask;
    private Player currentPlayer;
    private Game currentGame;
    private FirebaseAuth mAuth;
    private DatabaseManager db;
    private FirebaseStorage storage;

    private AlertDialog ad_friend_wants_to_play;
    private AlertDialog ad_request_friend_to_play;

    private Player playerFound;
    private boolean isFirstReqToPlay = false;
    private boolean isSearchingGame = false;

    //Friends
    ArrayList<DataModel> dataModels;
    private static CustomAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().equals("")) {
                db = DatabaseManager.getInstance();
                storage = FirebaseStorage.getInstance();


                //get data for the current user --> launch listener once to request base
                db.getCurrentUserDataById(user.getUid());
                db.setOnDataUserUpdateListener(new DatabaseManager.OnDataUserUpdateListener() {
                    @Override
                    public void updateDataUserUI(Player player_) {
                        currentPlayer = player_;
                        String splitName = currentPlayer.getName().split(" ")[0];
                        Player player = new Player(currentPlayer.getId(), splitName, 0, currentPlayer.getNb_win(), currentPlayer.getNb_lose(), currentPlayer.getXp());
                        loadProfilePicture(player);

                        db.insertPlayerInLobby(player);
                        db.initFriendList();

                        setContentView(R.layout.play_lobby_activity);
                        loadProfilePicture(player);

                        initUI();
                        initListener();
                    }
                });
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
            db.deletePlayerInLobby(currentPlayer);
        } else{
            signOut();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (currentPlayer != null) {
            db.insertPlayerInLobby(currentPlayer);
        }
    }

    private void initUI(){
        iv_profile_user = (CircleImageView) findViewById(R.id.register_iv_profile);
        tv_nb_win_loses = (TextView) findViewById(R.id.register_tv_nb_win_loses);
        tv_username = (TextView) findViewById(R.id.register_tv_username);
        iv_logout = (ImageView) findViewById(R.id.register_iv_logout);
        tv_current_rank = (TextView) findViewById(R.id.register_tv_current_rank);
        tv_current_level = (TextView) findViewById(R.id.register_tv_current_level);
        tv_current_xp = (TextView) findViewById(R.id.register_tv_current_xp);
        pb_progress_xp = (ProgressBar) findViewById(R.id.register_pb_progress_xp);
        tv_next_rank = (TextView) findViewById(R.id.register_tv_next_rank);


        btn_play = (Button) findViewById(R.id.register_btn_play);
        pb_btn_play = (ProgressBar) findViewById(R.id.register_pb_btn_play);
        pb_btn_play.setVisibility(View.GONE);
        lv_friends = (ListView) findViewById(R.id.register_lv_friends);
        iv_add_friend = (ImageView) findViewById(R.id.register_iv_add_friend);


        //SET NAME CURRENT PLAYER
        String text = currentPlayer.getName();
        tv_username.setText(text);

        //Win / Lose
        text = "<font color=#60c375>" + currentPlayer.getNb_win() +" W </font><font color=#FFFFFF>/</font><font color=#FF0000> " + currentPlayer.getNb_lose() +" L";
        tv_nb_win_loses.setText(Html.fromHtml(text));

        //LEVEL
        int level = getLevelByXp(currentPlayer.getXp());
        text = "LEVEL " + level;
        tv_current_level.setText(text);

        //CURRENT RANK
        text = getRankByLevel(level);
        tv_current_rank.setText(text);

        //NEXT RANK
        text = getNextRankByLevel(level);
        tv_next_rank.setText(text);

        //CURRENT XP + RANGE
        int[] range = getRangeLevelByLevel(level);
        text = currentPlayer.getXp() + "/" + range[1] + " XP";
        tv_current_xp.setText(text);

        pb_progress_xp.setMax(range[1]-range[0]);
        int progress = ( currentPlayer.getXp() * (range[1] - range[0]) ) / range[1];
        pb_progress_xp.setProgress(progress);


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
        iv_logout.setOnClickListener(new View.OnClickListener() {
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

                for (int i = 0; i< friendList.size(); i++){
                    final DataModel friend = friendList.get(i);

                    if (friend.getPlayReq().equals(Utils.PLAY_REQUEST_RECEIVED) && !isFirstReqToPlay){
                        isFirstReqToPlay = true;
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlayAsRegistered.this, R.style.myDialogTheme);
                        alertDialogBuilder
                                .setMessage(friend.getPlayer().getName() + " wants to play with you !" )
                                .setCancelable(false)
                                .setPositiveButton("PLAY", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String idGame = friend.getPlayer().getId() + currentPlayer.getId();
                                        db.getAvailableGameById(idGame);
                                        db.acceptToPlayWith(currentPlayer, friend.getPlayer());
                                        isFirstReqToPlay = false;
                                    }
                                })
                                .setNegativeButton("DECLINE", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        db.declineToPlayWith(currentPlayer, friend.getPlayer());
                                        String idGame = friend.getPlayer().getId() + currentPlayer.getId();
                                        db.getAvailableGameById(idGame);
                                        if (currentGame != null) db.deleteAvailableGame(currentGame);
                                        currentGame = null;
                                        dialog.cancel();
                                        isFirstReqToPlay = false;
                                    }
                                });
                        ad_friend_wants_to_play = alertDialogBuilder.create();
                        ad_friend_wants_to_play.show();
                        ad_friend_wants_to_play.getButton(ad_friend_wants_to_play.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));
                        ad_friend_wants_to_play.getButton(ad_friend_wants_to_play.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));
                    } else if (friend.getPlayReq().equals(Utils.PLAY_OK)) {
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
                        db.declineToPlayWith(currentPlayer, friend.getPlayer());
                    } else if (friend.getPlayReq().equals(Utils.PLAY_CANCEL)) {
                        if (ad_friend_wants_to_play != null){
                            if (ad_friend_wants_to_play.isShowing()){
                                ad_friend_wants_to_play.cancel();
                            }
                        }
                        if (ad_request_friend_to_play != null){
                            if (ad_request_friend_to_play.isShowing()){
                                ad_request_friend_to_play.cancel();
                            }
                        }
                        isFirstReqToPlay = false;
                        db.resetToPlayWith(currentPlayer, friend.getPlayer());
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
                    AlertDialog dialog = alertDialogBuilder.create();
                    dialog.show();
                    dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));

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
                        ad_request_friend_to_play = alert.create();
                        ad_request_friend_to_play.show();
                        ad_request_friend_to_play.getButton(ad_request_friend_to_play.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(PlayAsRegistered.this, R.color.whiteColor));


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


   private void loadProfilePicture(Player player){
       StorageReference storageRef = storage.getReference();
       String text = "profilePictures/" + player.getId()  + ".png";
       StorageReference imagesRef = storageRef.child(text);

       final long ONE_MEGABYTE = 1024 * 1024;
       imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
           @Override
           public void onSuccess(byte[] bytes) {
               Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
               iv_profile_user.setImageBitmap(bm);
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


    private String getRankByLevel(int level){

        String rank = "Unknown";
        if (level > 0 && level <= 5) rank = "Brainless";
        if (level > 5 && level <= 10) rank = "Little Head";
        if (level > 10 && level <= 20) rank = "Genius";
        if (level > 20 && level <= 35) rank = "Brain Master";
        if (level > 35 && level <= 60) rank = "Super Calculator";
        if (level > 60 && level <= 100) rank = "God";
        if (level > 100) rank = "Chuck Norris";
        return rank;
    }

    private String getNextRankByLevel(int level) {
        String nextRank = "Do you have a life ?";
        if (level > 0 && level <= 5) nextRank = "Next rank : Little Head";
        if (level > 5 && level <= 10) nextRank = "Next rank : Genius";
        if (level > 10 && level <= 20) nextRank = "Next rank : Brain Master";
        if (level > 20 && level <= 35) nextRank = "Next rank : Super calculator";
        if (level > 35 && level <= 60) nextRank = "Next rank : God";
        if (level > 60 && level <= 100) nextRank = "Next rank : Chuck Norris";
        return nextRank;
    }

    private int getLevelByXp(int XP){
        int level;
        level = (int) Math.round ((Math.sqrt(100 * (2 * XP + 25) + 50) / 100));
        return level;
    }

    private int[] getRangeLevelByLevel(int level){
        int[] range = new int[2];

        range[0] = ((level * level + level)/2) *100 - (level * 100);
        range[1] = (((level+1) * (level+1) + (level+1))/2) *100 - ((level+1) * 100);

        return range;
    }

}
