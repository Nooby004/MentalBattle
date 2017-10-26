package com.example.mlallemant.mentalbattle.UI.Login;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by m.lallemant on 25/10/2017.
 */

public class SigninActivity extends AppCompatActivity {

    private EditText et_email;
    private EditText et_username;
    private EditText et_password;
    private Button btn_signin;
    private ProgressBar pg_signin;
    private TextView tv_cancel;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Boolean isValidEmail;
    private Boolean isValidCreation = false;

    private final static String TAG = "SigninActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        initUI();
        initListener();
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }



    private void initUI()
    {
        et_username = (EditText) findViewById(R.id.signin_et_username);
        et_email = (EditText) findViewById(R.id.signin_et_email);
        et_password = (EditText) findViewById(R.id.signin_et_password);
        btn_signin = (Button) findViewById(R.id.signin_btn_signin);
        pg_signin = (ProgressBar) findViewById(R.id.signin_pb_btn_signin);
        tv_cancel = (TextView) findViewById(R.id.signin_tv_cancel);

        pg_signin.setVisibility(View.GONE);
    }

    private void initListener(){

        et_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isValidEmail(charSequence.toString())){
                    et_email.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orangeColor));
                }else{
                    et_email.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.redColor));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isValidCreation) {

                    String email = et_email.getText().toString();
                    String password = et_password.getText().toString();
                    String username = et_password.getText().toString();

                    Log.e(TAG, "email " + isValidEmail(email));
                    Log.e(TAG, "password " + isValidPassword(password));
                    Log.e(TAG, "username " + isValidUsername(username));


                    if (isValidEmail(email) && isValidPassword(password)  && isValidUsername(username)) {
                        pg_signin.setVisibility(View.VISIBLE);
                        et_username.setEnabled(false);
                        et_email.setEnabled(false);
                        et_password.setEnabled(false);
                        createUser(email, password);

                    } else {
                        makeToast("Email or password or username incorrect");
                    }
                }

            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !isValidCreation){
                    // User is signed in
                    final String username = et_username.getText().toString();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username).build();

                    user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            makeToast("Welcome " + username + " !");
                            isValidCreation = true;
                            pg_signin.setVisibility(View.GONE);
                            finish();
                        }
                    });

                }else {
                    pg_signin.setVisibility(View.GONE);
                    Log.e(TAG, "Error when adding Username");
                }
            }
        };
    }

    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }


    private void createUser(String email, String password)
    {
        try {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                                makeToast("Error, maybe email is already used or password not correct");
                                et_username.setEnabled(true);
                                et_email.setEnabled(true);
                                et_password.setEnabled(true);
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
            makeToast("Error");
        }
    }

    public static boolean isValidEmail(String target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidUsername(String target){
        boolean isValid = false;
        if (target.length() > 0 && target.length() < 15) isValid = true;
        return isValid;
    }

    public static boolean isValidPassword(String target){
        boolean isValid = false;
        if (target.length() > 5 && target.length() < 15) isValid = true;
        return isValid;
    }



}
