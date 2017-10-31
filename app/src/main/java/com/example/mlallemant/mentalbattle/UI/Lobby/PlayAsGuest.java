package com.example.mlallemant.mentalbattle.UI.Lobby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Game.GameActivity;
import com.example.mlallemant.mentalbattle.UI.Login.LoginActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.SearchGameTask;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

/**
 * Created by m.lallemant on 26/10/2017.
 */

public class PlayAsGuest extends AppCompatActivity {

    private final static String TAG = "PlayAsGuest";

    private EditText et_guest_name;
    private Button btn_play;
    private ProgressBar pb_btn_play;
    private TextView tv_cancel;
    private TextView tv_log;

    private FirebaseAuth mAuth;
    private Player currentPlayer;
    private Game currentGame;
    public SearchGameTask searchGameTask;
    private DatabaseManager db;

    private Boolean appGoesToBackground = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_as_guest_activity);
        initUI();
        initListener();

        mAuth = FirebaseAuth.getInstance();
        db = DatabaseManager.getInstance();
    }

    @Override
    public void onStop(){
        super.onStop();

        if (!appGoesToBackground) {
            if (currentPlayer != null )  db.deletePlayerSearchingPlayer(currentPlayer);
            if (currentGame != null) db.deleteAvailableGame(currentGame);
            searchGameTask.cancel(true);
            searchGameTask = null;
            mAuth.signOut();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        appGoesToBackground = true;
    }

    @Override
    public void onResume(){
        super.onResume();
        appGoesToBackground = false;
    }


    private void initUI(){

        et_guest_name = (EditText) findViewById(R.id.guest_et_guest_name);
        btn_play = (Button) findViewById(R.id.guest_btn_play);
        pb_btn_play = (ProgressBar) findViewById(R.id.guest_pb_btn_play);
        tv_cancel = (TextView) findViewById(R.id.guest_tv_cancel);
        tv_log = (TextView) findViewById(R.id.guest_tv_log);

        tv_log.setText("");
        pb_btn_play.setVisibility(View.GONE);
    }

    private void initListener(){

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.deletePlayerSearchingPlayer(currentPlayer);
                if (currentGame != null) db.deleteAvailableGame(currentGame);
                mAuth.signOut();
                launchLoginActivity();
            }
        });


        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                String username = et_guest_name.getText().toString();
                if (username.length() > 0 && username.length() < 15){

                    pb_btn_play.setVisibility(View.VISIBLE);
                    signInAnonymously(username);
                    Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_GUEST;

                    et_guest_name.setEnabled(false);
                    btn_play.setEnabled(false);
                    tv_log.setText("Searching game...");
                } else {
                    makeToast("Bad username");
                }
            }
        });

    }

    private void signInAnonymously(final String username){

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Player player = new Player(user.getUid(), username, 0);
                            db.insertPlayerSearchingGame(player);
                            currentPlayer = player;

                            launchSearchingGameTask();
                        } else {
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            makeToast("Authentification failed");
                        }
                    }
                });
    }


    private void launchSearchingGameTask(){
        searchGameTask = new SearchGameTask(new SearchGameTask.AsyncResponse() {
            @Override
            public void onFinishTask(Game game) {
                updateUI(game);}
        });
        searchGameTask.setParams(currentPlayer, currentGame);
        searchGameTask.execute("");
    }


    private void updateUI(Game game){
        currentGame = searchGameTask.getCurrentGame();
        currentPlayer = searchGameTask.getCurrentPlayer();

        if (game != null) {
            if ((!game.getPlayer1().getId().equals("")) && (!game.getPlayer2().getId().equals(""))) {
                String text = "Player found !";
                tv_log.setText(text);

                //We insert the currentGame in inProgress
                db.insertInProgressGame(game);
                //We get the current, so we can listen only this game now
                db.initListenerCurrentGame(game);
                //And also delete the availableGame
                db.deleteAvailableGame(game);

                //Launch game
                Intent intent = new Intent(PlayAsGuest.this, GameActivity.class);
                intent.putExtra("idGame", game.getId());
                intent.putExtra("currentPlayerId", currentPlayer.getId());
                startActivity(intent);
                finish();

            } else {
                db.deleteAvailableGame(game);
                db.deletePlayerSearchingPlayer(currentPlayer);
                String text = "No player found, try again";
                tv_log.setText(text);
                et_guest_name.setEnabled(true);
                btn_play.setEnabled(true);
            }
            pb_btn_play.setVisibility(View.GONE);
        } else {
            launchSearchingGameTask();
        }
    }


    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }

    private void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private static String getRandomId(){
        return UUID.randomUUID().toString();
    }

    private void launchLoginActivity(){
        Intent intent = new Intent(PlayAsGuest.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}

