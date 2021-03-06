package com.example.mlallemant.mentalbattle.UI.Login;

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
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.SearchGameTask;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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
    private DatabaseManager db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_as_guest_activity);

        initUI();
        initListener();

        mAuth = FirebaseAuth.getInstance();

        db = DatabaseManager.getInstance();
        db.initFriendList();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }


    private void initUI(){

        et_guest_name = (EditText) findViewById(R.id.guest_et_guest_name);
        btn_play = (Button) findViewById(R.id.guest_btn_play);
        pb_btn_play = (ProgressBar) findViewById(R.id.guest_pb_btn_play);
        tv_cancel = (TextView) findViewById(R.id.guest_tv_cancel);
        tv_log = (TextView) findViewById(R.id.guest_tv_log);

        btn_play.setText("CONTINUE AS GUEST");
        tv_log.setText("");
        pb_btn_play.setVisibility(View.GONE);
    }

    private void initListener(){

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchLoginActivity();
            }
        });

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = et_guest_name.getText().toString();

                if (username.length() > 3 && username.length() < 12) {
                    pb_btn_play.setVisibility(View.VISIBLE);
                    signInAnonymously(et_guest_name.getText().toString());
                    btn_play.setEnabled(false);
                } else {
                    makeToast("Choose a correct name (between 3 and 13 characters)");
                }
            }
        });
    }


    private void signInAnonymously(final String username){

        Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_GUEST;
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user!=null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(username).build();
                                user.updateProfile(profileUpdates);

                                Player player = new Player(user.getUid(), username, 0,0,0,0);
                                currentPlayer = player;

                                Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_GUEST;
                                launchMenuActivity(currentPlayer);
                            }

                        } else {
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            makeToast("Authentification failed");
                            pb_btn_play.setVisibility(View.INVISIBLE);
                            btn_play.setEnabled(true);
                        }
                    }
                });
    }




    private void launchMenuActivity(Player currentPlayer){
        Intent intent = new Intent(PlayAsGuest.this, MenuActivity.class);
        intent.putExtra("currentPlayer", currentPlayer);
        startActivity(intent);
        finish();
    }

    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }

    private void launchLoginActivity(){
        Intent intent = new Intent(PlayAsGuest.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void signOut() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
    }

}

