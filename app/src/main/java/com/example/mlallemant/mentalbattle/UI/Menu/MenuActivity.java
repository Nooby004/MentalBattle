package com.example.mlallemant.mentalbattle.UI.Menu;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Friends.DataModel;
import com.example.mlallemant.mentalbattle.UI.Game.GameActivity;
import com.example.mlallemant.mentalbattle.UI.Login.LoginActivity;
import com.example.mlallemant.mentalbattle.UI.Menu.Fragment.SelectorFragment;
import com.example.mlallemant.mentalbattle.Utils.CustomDialog;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by m.lallemant on 09/11/2017.
 */

public class MenuActivity extends AppCompatActivity {

    private final static String TAG = "MenuActivity";

    //UI
    private CircleImageView iv_profile_user;
    private TextView tv_nb_win_loses;
    private TextView tv_username;
    private ImageView iv_logout;
    private TextView tv_current_rank;
    private TextView tv_current_level;
    private TextView tv_current_xp;
    private ProgressBar pb_progress_xp;
    private TextView tv_next_rank;

    //FireBase
    private FirebaseAuth mAuth;
    private DatabaseManager db;
    private FirebaseStorage storage;

    //Utils
    private Player currentPlayer;
    private boolean isFirstReqToPlay = false;
    private Game currentGame;
    private CustomDialog cdRequestReceived;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);


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
                        launchSelectorFragment();

                        db.initFriendList();

                        String splitName = currentPlayer.getName().split(" ")[0];
                        Player player = new Player(currentPlayer.getId(), splitName, 0, currentPlayer.getNb_win(), currentPlayer.getNb_lose(), currentPlayer.getXp());
                        loadProfilePicture(player);

                        db.insertPlayerInLobby(player);
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
        if (currentPlayer != null) db.deletePlayerInLobby(currentPlayer);
    }
    
    
    private void initUI(){
        iv_profile_user = (CircleImageView) findViewById(R.id.menu_iv_profile);
        tv_nb_win_loses = (TextView) findViewById(R.id.menu_tv_nb_win_loses);
        tv_username = (TextView) findViewById(R.id.menu_tv_username);
        iv_logout = (ImageView) findViewById(R.id.menu_iv_logout);
        tv_current_rank = (TextView) findViewById(R.id.menu_tv_current_rank);
        tv_current_level = (TextView) findViewById(R.id.menu_tv_current_level);
        tv_current_xp = (TextView) findViewById(R.id.menu_tv_current_xp);
        pb_progress_xp = (ProgressBar) findViewById(R.id.menu_pb_progress_xp);
        tv_next_rank = (TextView) findViewById(R.id.menu_tv_next_rank);

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
    }

    private void initListener(){
        //LOG OFF
        iv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.notifyFriendsYouAreDisconnected(currentPlayer);
                signOut();
                launchLoginActivity();
            }
        });

        db.setOnFriendChangeListener(new DatabaseManager.OnFriendChangeListener() {
            @Override
            public void updateFriendListUI(List<DataModel> friendList) {
                handleNotification(friendList);
            }
        });
    }

    private void handleNotification(List<DataModel> friendList){

        for (int i = 0; i< friendList.size(); i++){
            final DataModel friend = friendList.get(i);

            if (friend.getPlayReq().equals(Utils.PLAY_REQUEST_RECEIVED) && !isFirstReqToPlay){
                isFirstReqToPlay = true;
                cdRequestReceived = new CustomDialog(MenuActivity.this,
                        friend.getPlayer().getId(),
                        friend.getPlayer().getName() + " wants to play with you !",
                        "PLAY", R.color.greenColor,
                        "DECLINE", R.color.redColor);
                cdRequestReceived.create();
                cdRequestReceived.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {
                    @Override
                    public void onClickBtn1() {
                        String idGame = friend.getPlayer().getId() + currentPlayer.getId();
                        db.getAvailableGameById(idGame);
                        db.acceptToPlayWith(currentPlayer, friend.getPlayer());
                        isFirstReqToPlay = false;
                    }

                    @Override
                    public void onClickBtn2() {
                        db.declineToPlayWith(currentPlayer, friend.getPlayer());
                        String idGame = friend.getPlayer().getId() + currentPlayer.getId();
                        db.getAvailableGameById(idGame);
                        if (currentGame != null) db.deleteAvailableGame(currentGame);
                        currentGame = null;
                        cdRequestReceived.dismiss();
                        isFirstReqToPlay = false;
                    }
                });

            } else if (friend.getPlayReq().equals(Utils.PLAY_OK)) {
                currentGame = db.getCurrentGame();

                db.insertInProgressGame(currentGame);
                db.initListenerCurrentGame(currentGame);
                db.deleteAvailableGame(currentGame);

                //Launch game
                launchGameActivity(currentGame);
                db.declineToPlayWith(currentPlayer, friend.getPlayer());

            } else if (friend.getPlayReq().equals(Utils.PLAY_CANCEL)) {
               if (cdRequestReceived != null){
                    if (cdRequestReceived.isShowing()){
                        cdRequestReceived.dismiss();
                    }
                }
                /*if (ad_request_friend_to_play != null){
                    if (ad_request_friend_to_play.isShowing()){
                        ad_request_friend_to_play.cancel();
                    }
                }*/
                isFirstReqToPlay = false;
                db.resetToPlayWith(currentPlayer, friend.getPlayer());
            }

        }

    }

    private void launchGameActivity(Game game){
        Intent intent = new Intent(MenuActivity.this, GameActivity.class);
        intent.putExtra("idGame", game.getId());
        intent.putExtra("currentPlayerId", currentPlayer.getId());
        startActivity(intent);
        this.finish();
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

    private void launchLoginActivity(){
        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void launchSelectorFragment(){
        SelectorFragment sf = new SelectorFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.menu_fl_select, sf);
        ft.commit();
    }

    public Player getCurrentPlayer(){
        return currentPlayer;
    }

    private void signOut(){
        if (currentPlayer != null) db.notifyFriendsYouAreDisconnected(currentPlayer);
        if (db != null) db.initFriendList();
        mAuth.signOut();
        LoginManager.getInstance().logOut();
    }
}
