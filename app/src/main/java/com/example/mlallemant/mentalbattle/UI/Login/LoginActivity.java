package com.example.mlallemant.mentalbattle.UI.Login;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Lobby.PlayAsGuest;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Created by m.lallemant on 25/10/2017.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "LoginActivity";

    private EditText et_email;
    private EditText et_password;
    private Button btn_login;
    private TextView tv_signin;
    private TextView tv_playasguest;
    private Button btn_loginFB;
    private Button btn_loginGoogle;
    private ProgressBar pb_login;
    private ProgressBar pb_loginFB;
    private ProgressBar pb_loginGoogle;

    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private LoginButton loginButtonFB;

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private boolean isConnectedWithGoogle = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_normal_fb_google_activity);
        initUI();
        initListener();
        initLoginButtonFB();
        initLoginGoogle();

        mAuth = FirebaseAuth.getInstance();
        signOut();

    }


    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            makeToast("Welcome back " + currentUser.getDisplayName());
            launchLobbyActivity(currentUser);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                makeToast("Sign in with Google failed");
            }
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        makeToast("Google Play Services error.");
    }

    private void initUI(){
        et_email = (EditText) findViewById(R.id.login_et_email);
        et_password = (EditText) findViewById(R.id.login_et_password);
        btn_login = (Button) findViewById(R.id.login_btn_login);
        tv_signin = (TextView) findViewById(R.id.login_tv_signin);
        tv_playasguest = (TextView) findViewById(R.id.login_tv_playasguest);
        btn_loginFB = (Button) findViewById(R.id.login_btn_loginFB);
        btn_loginGoogle = (Button) findViewById(R.id.login_btn_loginGoogle);
        pb_login = (ProgressBar) findViewById(R.id.login_pb_btn_login);
        pb_loginFB = (ProgressBar) findViewById(R.id.login_pb_btn_loginFB);
        pb_loginGoogle = (ProgressBar) findViewById(R.id.login_pb_btn_loginGoogle);

        pb_login.setVisibility(View.GONE);
        pb_loginFB.setVisibility(View.GONE);
        pb_loginGoogle.setVisibility(View.GONE);
    }

    private void initListener(){

        //PLAY AS GUEST
        tv_playasguest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPlayAsGuestActivity();
            }
        });

        //SIGN IN
        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSignInActivity();
            }
        });

        //LOGIN
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //GET USERNAME/PASSWORD
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                if (isValidPassword(password) && isValidEmail(email)){
                    hideKeyboard();
                    pb_login.setVisibility(View.VISIBLE);
                    loginUser(email, password);
                }else{
                    makeToast("Invalid Email/Password");
                }
            }
        });

        btn_loginFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb_loginFB.setVisibility(View.VISIBLE);
                loginUserFB();
            }
        });


        btn_loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb_loginGoogle.setVisibility(View.VISIBLE);
                loginUserGoogle();
            }
        });
    }


    private void loginUser(String email, String password){
        try{
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                makeToast(user.getDisplayName() + " connected !");
                                pb_login.setVisibility(View.GONE);
                                Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_ACCOUNT;
                                //Launch LOBBY ACTIVITY


                            } else {
                                //Error when connecting
                                makeToast("Invalid Email/Password");
                                pb_login.setVisibility(View.GONE);
                            }

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loginUserFB(){
        loginButtonFB.performClick();
    }

    private void loginUserGoogle(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void initLoginGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,  this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void initLoginButtonFB()
    {
        mCallbackManager = CallbackManager.Factory.create();
        loginButtonFB = new LoginButton(this);
        loginButtonFB.setReadPermissions("email","public_profile");
        loginButtonFB.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook:onError", error);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            makeToast("Welcome " + user.getDisplayName());
                            pb_loginFB.setVisibility(View.GONE);
                            Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_FB;
                            launchLobbyActivity(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            makeToast("Authentication failed");
                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            makeToast("Welcome " + user.getDisplayName());
                            isConnectedWithGoogle = true;
                            pb_loginGoogle.setVisibility(View.GONE);
                            Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_GOOGLE;
                            launchLobbyActivity(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            makeToast("Authentication failed.");
                        }
                    }
                });
    }


    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }


    private static boolean isValidEmail(String target){
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private static boolean isValidPassword(String target){
        boolean isValid = false;
        if (target.length() > 5 && target.length() < 15) isValid = true;
        return isValid;
    }

    private void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void signOut(){
        mAuth.signOut();
        LoginManager.getInstance().logOut();

        if (isConnectedWithGoogle) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            makeToast("Google sign out");
                        }
                    }
            );

            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            makeToast("Google sign out");
                        }
                    });
        }

    }

    private void launchLobbyActivity(FirebaseUser user){

        //finish();
    }

    private void launchPlayAsGuestActivity(){
        Intent intent = new Intent(LoginActivity.this, PlayAsGuest.class);
        startActivity(intent);
        finish();
    }

    private void launchSignInActivity(){
        Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
        startActivity(intent);
    }

}
