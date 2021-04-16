package com.example.mlallemant.mentalbattle.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Utils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Created by m.lallemant on 25/10/2017.
 */
class LoginActivity : AppCompatActivity(), OnConnectionFailedListener {
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var tvSignin: TextView? = null
    private var tvPlayAsGuest: TextView? = null
    private var btnLoginFB: Button? = null
    private var btnLoginGoogle: Button? = null
    private var pbLogin: ProgressBar? = null
    private var pbLoginFB: ProgressBar? = null
    private var pbLoginGoogle: ProgressBar? = null
    private var mAuth: FirebaseAuth? = null
    private var mCallbackManager: CallbackManager? = null
    private var loginButtonFB: LoginButton? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var isConnectedWithGoogle = false
    private var db: DatabaseManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_activity)
        mAuth = FirebaseAuth.getInstance()
        db = DatabaseManager.getInstance()
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            setContentView(R.layout.loading_activity)
            db?.getCurrentUserDataById(currentUser.uid)
            db?.setOnDataUserUpdateListener { player ->
                if (player == null) {
                    Utils.AUTHENTIFICATION_TYPE =
                        Utils.AUTHENTIFICATION_GUEST
                } else {
                    Utils.AUTHENTIFICATION_TYPE =
                        Utils.AUTHENTIFICATION_ACCOUNT
                }
                launchMenuActivity(currentUser)
            }
        } else {
            setContentView(R.layout.login_normal_fb_google_activity)
            initUI()
            initListener()
            initLoginButtonFB()
            initLoginGoogle()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        signOut()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result!!.isSuccess) {
                // Google Sign In was successful, authenticate with FireBase
                val account = result.signInAccount
                fireBaseAuthWithGoogle(account)
            } else {
                makeToast("Sign in with Google failed")
            }
        }
        if (requestCode == 1) {
            if (resultCode == 10) {
                val email = data!!.getStringExtra("email")
                val password = data.getStringExtra("password")
                etEmail!!.setText(email)
                etPassword!!.setText(password)
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:$connectionResult")
        makeToast("Google Play Services error.")
    }

    private fun initUI() {
        etEmail = findViewById<View>(R.id.login_et_email) as EditText
        etPassword = findViewById<View>(R.id.login_et_password) as EditText
        btnLogin = findViewById<View>(R.id.login_btn_login) as Button
        tvSignin = findViewById<View>(R.id.login_tv_signin) as TextView
        tvPlayAsGuest = findViewById<View>(R.id.login_tv_playasguest) as TextView
        btnLoginFB =
            findViewById<View>(R.id.login_btn_loginFB) as Button
        btnLoginGoogle =
            findViewById<View>(R.id.login_btn_loginGoogle) as Button
        pbLogin = findViewById<View>(R.id.login_pb_btn_login) as ProgressBar
        pbLoginFB = findViewById<View>(R.id.login_pb_btn_loginFB) as ProgressBar
        pbLoginGoogle =
            findViewById<View>(R.id.login_pb_btn_loginGoogle) as ProgressBar
        pbLogin!!.visibility = View.GONE
        pbLoginFB!!.visibility = View.GONE
        pbLoginGoogle!!.visibility = View.GONE
    }

    private fun initListener() {

        //PLAY AS GUEST
        tvPlayAsGuest!!.setOnClickListener { launchPlayAsGuestActivity() }

        //SIGN IN
        tvSignin!!.setOnClickListener { launchSignInActivity() }

        //LOGIN
        btnLogin!!.setOnClickListener { //GET USERNAME/PASSWORD
            val email = etEmail!!.text.toString()
            val password = etPassword!!.text.toString()
            if (isValidPassword(password) && isValidEmail(
                    email
                )
            ) {
                hideKeyboard()
                pbLogin!!.visibility = View.VISIBLE
                loginUser(email, password)
            } else {
                makeToast("Invalid Email/Password")
            }
        }
        btnLoginFB!!.setOnClickListener {
            pbLoginFB!!.visibility = View.VISIBLE
            loginUserFB()
        }
        btnLoginGoogle!!.setOnClickListener {
            pbLoginGoogle!!.visibility = View.VISIBLE
            loginUserGoogle()
        }
    }

    private fun loginUser(email: String, password: String) {
        try {
            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth!!.currentUser
                        //makeToast(user.getDisplayName() + " connected !");
                        pbLogin!!.visibility = View.GONE
                        Utils.AUTHENTIFICATION_TYPE =
                            Utils.AUTHENTIFICATION_ACCOUNT
                        launchMenuActivity(user)
                    } else {
                        //Error when connecting
                        makeToast("Invalid Email/Password")
                        pbLogin!!.visibility = View.GONE
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loginUserFB() {
        loginButtonFB!!.performClick()
    }

    private fun loginUserGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun initLoginGoogle() {
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun initLoginButtonFB() {
        mCallbackManager = CallbackManager.Factory.create()
        loginButtonFB = LoginButton(this)
        loginButtonFB!!.setReadPermissions("email", "public_profile")
        loginButtonFB!!.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.e(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.e(TAG, "facebook:onCancel")
                pbLoginFB!!.visibility = View.INVISIBLE
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "facebook:onError", error)
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(
                        TAG,
                        "signInWithCredential:success"
                    )
                    val user = mAuth!!.currentUser
                    //Download picture
                    var facebookUserId = ""
                    for (profile in user!!.providerData) {
                        // check if the provider id matches "facebook.com"
                        if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                            facebookUserId = profile.uid
                        }
                    }
                    val photoUrl =
                        "https://graph.facebook.com/$facebookUserId/picture?height=200"
                    //DownloadImage().execute(photoUrl)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        TAG,
                        "signInWithCredential:failure",
                        task.exception
                    )
                    makeToast("Authentication failed")
                }
            }
    }

    private fun fireBaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        Log.d(TAG, "fireBaseAuthWithGoogle:" + acct!!.id)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(
                        TAG,
                        "signInWithCredential:success"
                    )
                    val user = mAuth!!.currentUser
                    makeToast("Welcome " + user!!.displayName)
                    isConnectedWithGoogle = true
                    pbLoginGoogle!!.visibility = View.GONE
                    Utils.AUTHENTIFICATION_TYPE =
                        Utils.AUTHENTIFICATION_GOOGLE
                    launchMenuActivity(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(
                        TAG,
                        "signInWithCredential:failure",
                        task.exception
                    )
                    makeToast("Authentication failed.")
                }
            }
    }

    private fun makeToast(text: String) {
        Toast.makeText(
            applicationContext, text,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun hideKeyboard() {
        val inputManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager?.hideSoftInputFromWindow(
            currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun signOut() {
        mAuth!!.signOut()
        LoginManager.getInstance().logOut()
        if (isConnectedWithGoogle) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback { makeToast("Google sign out") }
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient)
                .setResultCallback { makeToast("Google sign out") }
        }
    }

    private fun launchMenuActivity(user: FirebaseUser?) {
        val intent = Intent(this@LoginActivity, MenuActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun launchPlayAsGuestActivity() {
        val intent = Intent(this@LoginActivity, PlayAsGuest::class.java)
        startActivity(intent)
        finish()
    }

    private fun launchSignInActivity() {
        val intent = Intent(this@LoginActivity, SigninActivity::class.java)
        startActivityForResult(intent, 1)
    }


    // TODO use rxJava
    /* private inner class DownloadImage :
         AsyncTask<String?, Void?, Bitmap?>() {
         override fun onPreExecute() {
             super.onPreExecute()
         }

         protected override fun doInBackground(vararg URL: String): Bitmap? {
             val imageURL = URL[0]
             var bitmap: Bitmap? = null
             try {
                 // Download Image from URL
                 val input = URL(imageURL).openStream()
                 // Decode Bitmap
                 bitmap = BitmapFactory.decodeStream(input)
             } catch (e: Exception) {
                 e.printStackTrace()
             }
             return bitmap
         }

         override fun onPostExecute(result: Bitmap?) {
             val user = mAuth!!.currentUser
             val storage = FirebaseStorage.getInstance()
             val storageRef = storage.reference
             val text = "profilePictures/" + user!!.uid + ".png"
             val imagesRef = storageRef.child(text)
             val baos = ByteArrayOutputStream()
             result!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
             val data = baos.toByteArray()
             val uploadTask = imagesRef.putBytes(data)
             uploadTask.addOnFailureListener { makeToast("Error while uploading picture profile") }
                 .addOnSuccessListener {
                     launchMenuActivity(user)
                     db!!.getCurrentUserDataById(user.uid)
                     db!!.setOnDataUserUpdateListener { player ->
                         if (player == null) {
                             db!!.insertRegisteredPlayer(
                                 Player(
                                     mAuth!!.uid,
                                     user.displayName,
                                     0,
                                     0,
                                     0,
                                     0
                                 )
                             )
                         }
                         pbLoginFB!!.visibility = View.GONE
                         Utils.AUTHENTIFICATION_TYPE =
                             Utils.AUTHENTIFICATION_FB
                         launchMenuActivity(user)
                     }
                 }
         }
     }*/

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_SIGN_IN = 9001
        private fun isValidEmail(target: String): Boolean {
            return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target)
                .matches()
        }

        private fun isValidPassword(target: String): Boolean {
            var isValid = false
            if (target.length in 6..14) isValid = true
            return isValid
        }
    }
}