package com.example.mlallemant.mentalbattle.UI.Login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

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
    private DatabaseManager db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        mAuth = FirebaseAuth.getInstance();
        db = DatabaseManager.getInstance();

        final FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            setContentView(R.layout.loading_activity);

            db.getCurrentUserDataById(currentUser.getUid());
            db.setOnDataUserUpdateListener(new DatabaseManager.OnDataUserUpdateListener() {
                @Override
                public void updateDataUserUI(Player player) {
                    if (player == null) {
                        Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_GUEST;
                    } else {
                        Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_ACCOUNT;
                    }
                    launchMenuActivity(currentUser);
                }
            });
        } else {
            setContentView(R.layout.login_normal_fb_google_activity);
            initUI();
            initListener();
            initLoginButtonFB();
            initLoginGoogle();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        signOut();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with FireBase
                GoogleSignInAccount account = result.getSignInAccount();
                fireBaseAuthWithGoogle(account);
            } else {
                makeToast("Sign in with Google failed");
            }
        }

        if(requestCode == 1) {
            if ( resultCode == 10) {
                String email = data.getStringExtra("email");
                String password = data.getStringExtra("password");

                et_email.setText(email);
                et_password.setText(password);
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

        btn_loginGoogle.setVisibility(View.GONE);

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
                                //makeToast(user.getDisplayName() + " connected !");
                                pb_login.setVisibility(View.GONE);
                                Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_ACCOUNT;
                                launchMenuActivity(user);

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
                pb_loginFB.setVisibility(View.INVISIBLE);
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
                            final FirebaseUser user = mAuth.getCurrentUser();
                            //Download picture

                            String facebookUserId = "";
                            for(UserInfo profile : user.getProviderData()) {
                                // check if the provider id matches "facebook.com"
                                if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                                    facebookUserId = profile.getUid();
                                }
                            }

                            String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=200";
                            new DownloadImage().execute(photoUrl);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            makeToast("Authentication failed");
                        }
                    }
                });
    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount acct){
        Log.d(TAG, "fireBaseAuthWithGoogle:" + acct.getId());

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
                            launchMenuActivity(user);

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

    private void launchMenuActivity(FirebaseUser user){
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        this.overridePendingTransition(0, 0);
        finish();
    }

    private void launchPlayAsGuestActivity(){
        Intent intent = new Intent(LoginActivity.this, PlayAsGuest.class);
        startActivity(intent);
        finish();
    }

    private void launchSignInActivity(){
        Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
        startActivityForResult(intent, 1);
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            final FirebaseUser user = mAuth.getCurrentUser();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            String text = "profilePictures/" + user.getUid() + ".png";
            StorageReference imagesRef = storageRef.child(text);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imagesRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    makeToast("Error while uploading picture profile");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    db.getCurrentUserDataById(user.getUid());
                    db.setOnDataUserUpdateListener(new DatabaseManager.OnDataUserUpdateListener() {
                        @Override
                        public void updateDataUserUI(Player player) {
                            if (player == null) {
                                db.insertRegisteredPlayer(new Player(mAuth.getUid(), user.getDisplayName(), 0, 0 , 0, 0));
                            }
                            pb_loginFB.setVisibility(View.GONE);
                            Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_FB;
                            launchMenuActivity(user);
                        }
                    });


                }
            });
        }
    }

}
