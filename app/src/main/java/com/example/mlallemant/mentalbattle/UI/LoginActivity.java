package com.example.mlallemant.mentalbattle.UI;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private final static String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private String mName = "";
    private DatabaseManager db;
    private Player currentPlayer;
    private Game currentGame;
    private Boolean loginSuccess = false;
    private Boolean appGoesToBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance();
        db = DatabaseManager.getInstance();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (!loginSuccess && !appGoesToBackground){   //si !loginSucess && comefrombackground
            db.deletePlayer(currentPlayer);
            if (currentGame !=  null) db.deleteGame(currentGame);
            mAuth.getCurrentUser().delete();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!loginSuccess) {
            db.deletePlayer(currentPlayer);
            if (currentGame != null) db.deleteGame(currentGame);
            mAuth.getCurrentUser().delete();
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


    private void signInAnonymously(final String name){

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Player player = new Player(user.getUid(), name,0);
                            DatabaseManager db = DatabaseManager.getInstance();
                            db.insertPlayer(player);
                            currentPlayer = player;

                            /** FOR TEST */
                            //db.addPlayerForTest();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void onClickLogin(View view){
        EditText editTextLogin = (EditText)findViewById (R.id.et_login);
        mName = editTextLogin.getText().toString();

        if (mName.equals("")){
            Toast.makeText(LoginActivity.this, "Choose a name before!", Toast.LENGTH_SHORT).show();
        }
        else{
            //SIGN IN
            signInAnonymously(mName);

            //EDIT UI
            editTextLogin.setEnabled(false);
            editTextLogin.getBackground().clearColorFilter();
            view.setEnabled(false);
            ProgressBar pg = (ProgressBar) findViewById(R.id.pg_load);
            pg.setVisibility(View.VISIBLE);
            TextView tv_searchingStatus = (TextView) findViewById(R.id.tv_searchingStatus);
            tv_searchingStatus.setVisibility(View.VISIBLE);
            tv_searchingStatus.setText(R.string.status_searching);


            //Search Game after 2s waiting time
            Handler handler = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    //SearchGame
                    new searchPlayer().execute("");
                }
            };
            handler.postDelayed(r,2000);
        }
    }



    private class searchPlayer extends AsyncTask<String, Void, Game> {
        @Override
        protected Game doInBackground(String... urls) {

            Game returnGame = null;
            try{

                long start_time = System.currentTimeMillis();
                long wait_time = Utils.SEARCH_TIME;
                long end_time = start_time + wait_time;

                Game availableGame = db.findAvailableGame();

                if (availableGame == null){
                    // if no game available, we create one
                    Log.d(TAG, "no game available");

                    Player tmpPlayer = new Player("","",0);
                    String id = getRandomId();
                    Game game = new Game(id, currentPlayer, tmpPlayer);
                    db.insertGame(game);

                    currentGame = game;

                    Thread.sleep(50);

                    // and we wait 20s max
                    while(System.currentTimeMillis() < end_time){

                        Game tmpGame = db.getGameById(id);
                        if (tmpGame != null) {
                            if ((!tmpGame.getPlayer1().getId().equals("")) && (!tmpGame.getPlayer2().getId().equals(""))) {
                                returnGame = tmpGame;
                                break;
                            }
                            Thread.sleep(50);
                        }
                        returnGame = tmpGame;
                    }


                }else{
                    // else we insert player1 in game available
                    Log.d(TAG, "Game available");

                    if (availableGame.getPlayer1().getId().equals("")){
                        db.insertPlayer1InGameById(currentPlayer, availableGame.getId());
                    } else {
                        db.insertPlayer2InGameById(currentPlayer, availableGame.getId());
                    }

                    Thread.sleep(50);
                    returnGame = db.getGameById(availableGame.getId());
                }

            }catch (Exception e){
                e.printStackTrace();
            }

            return returnGame;
        }

        @Override
        protected void onPostExecute(Game game) {
            ProgressBar pg = (ProgressBar) findViewById(R.id.pg_load);
            pg.setVisibility(View.INVISIBLE);
            TextView tv_searchingStatus = (TextView) findViewById(R.id.tv_searchingStatus);


            if (game != null) {
                if ((!game.getPlayer1().getId().equals("")) && (!game.getPlayer2().getId().equals(""))) {
                    String text = "Player found !";
                    tv_searchingStatus.setText(text);

                    //Launch game
                    Intent intent = new Intent(LoginActivity.this, GameActivity.class);
                    intent.putExtra("idGame", game.getId());
                    intent.putExtra("currentPlayerId", currentPlayer.getId());
                    startActivity(intent);
                    loginSuccess = true;
                    finish();
                } else {
                    db.deleteGame(game);
                    String text = "No player found, try again";
                    tv_searchingStatus.setText(text);
                    Button b_login = (Button) findViewById(R.id.b_login);
                    b_login.setEnabled(true);
                }
            }else {
                db.deleteGame(game);
                new searchPlayer().execute("");
            }
        }
    }


    private String getRandomId(){
        return UUID.randomUUID().toString();
    }
}
