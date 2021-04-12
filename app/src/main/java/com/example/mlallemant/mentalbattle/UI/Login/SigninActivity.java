package com.example.mlallemant.mentalbattle.UI.Login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by m.lallemant on 25/10/2017.
 */

public class SigninActivity extends AppCompatActivity {

    private static final int RC_CODE_PICKER = 2000;

    private EditText et_email;
    private EditText et_username;
    private EditText et_password;
    private Button btn_signin;
    private ProgressBar pg_signin;
    private TextView tv_cancel;
    private CircleImageView iv_profile_user;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage storage;
    private DatabaseManager db;

    private Boolean isValidCreation = false;

    private final static String TAG = "SigninActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_activity);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        db = DatabaseManager.getInstance();
        storage = FirebaseStorage.getInstance();

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        List<Image> images = ImagePicker.getImages(data);
//        if (images != null && !images.isEmpty()) {
//            iv_profile_user.setImageBitmap(BitmapFactory.decodeFile(images.get(0).getPath()));
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void initUI()
    {
        et_username = (EditText) findViewById(R.id.signin_et_username);
        et_email = (EditText) findViewById(R.id.signin_et_email);
        et_password = (EditText) findViewById(R.id.signin_et_password);
        btn_signin = (Button) findViewById(R.id.signin_btn_signin);
        pg_signin = (ProgressBar) findViewById(R.id.signin_pb_btn_signin);
        tv_cancel = (TextView) findViewById(R.id.signin_tv_cancel);
        iv_profile_user = (CircleImageView) findViewById(R.id.signin_profile_user);

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
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !isValidCreation){
                    // User is signed in
                    final String username = et_username.getText().toString();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username).build();


                    user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            insertProfilePicture(user);
                        }
                    });

                }else {
                    pg_signin.setVisibility(View.GONE);
                    Log.e(TAG, "Error when adding Username");
                }
            }
        };

        iv_profile_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ImagePicker.create(SigninActivity.this)
//                        .returnAfterFirst(true) // set whether pick or camera action should return immediate result or not. For pick image only work on single mode
//                        .folderMode(true) // folder mode (false by default)
//                        .folderTitle("Folder") // folder selection title
//                        .imageTitle("Tap to select") // image selection title
//                        .single() // single mode
//                        .limit(1) // max images can be selected (99 by default)
//                        .showCamera(true) // show camera or not (true by default)
//                        .theme(R.style.myTheme)
//                        .imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
//                        .enableLog(true) // disabling log
//                        .start(RC_CODE_PICKER); // start image picker activity with request code
            }
        });

    }

    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }

    private void insertProfilePicture(FirebaseUser user){
        StorageReference storageRef = storage.getReference();
        String text = "profilePictures/" + user.getUid() + ".png";
        StorageReference imagesRef = storageRef.child(text);

        iv_profile_user.setDrawingCacheEnabled(true);
        iv_profile_user.buildDrawingCache();
        Bitmap bitmap = iv_profile_user.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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

                db.insertRegisteredPlayer(new Player(mAuth.getUid(), et_username.getText().toString(), 0, 0 , 0, 0));

                isValidCreation = true;
                pg_signin.setVisibility(View.GONE);
                mAuth.signOut();

                String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                setResult(10, intent);
                finish();
            }
        });
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
