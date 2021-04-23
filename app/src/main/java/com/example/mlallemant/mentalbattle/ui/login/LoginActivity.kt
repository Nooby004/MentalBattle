package com.example.mlallemant.mentalbattle.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.MentalBattleApplication.Companion.REMOTE_CONFIG_SIGNIN_WITH_FACEBOOK
import com.example.mlallemant.mentalbattle.MentalBattleApplication.Companion.REMOTE_CONFIG_SIGNIN_WITH_GOOGLE
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.LoginActivityBinding
import com.example.mlallemant.mentalbattle.ui.extention.changeVisibility
import com.example.mlallemant.mentalbattle.ui.extention.toast
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Utils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.net.URL

/**
 * Created by m.lallemant on 25/10/2017.
 */
class LoginActivity : AppCompatActivity(), OnConnectionFailedListener {

    private var mAuth: FirebaseAuth? = null
    private var mCallbackManager: CallbackManager? = null
    private var loginButtonFB: LoginButton? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var isConnectedWithGoogle = false
    private lateinit var db: DatabaseManager

    private var _binding: LoginActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = DatabaseManager.getInstance()
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            binding.loadingLayout.root.changeVisibility(true)
            db.getCurrentUserDataById(currentUser.uid)
            db.setOnDataUserUpdateListener { player ->
                if (player == null) {
                    Utils.AUTHENTIFICATION_TYPE =
                        Utils.AUTHENTIFICATION_GUEST
                } else {
                    Utils.AUTHENTIFICATION_TYPE =
                        Utils.AUTHENTIFICATION_ACCOUNT
                }
                launchMenuActivity()
            }
        } else {
            binding.loadingLayout.root.changeVisibility(false)
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

    public override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = result.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                toast(getString(R.string.sign_with_google_failed))
            }
        }
        if (requestCode == 1) {
            if (resultCode == 10) {
                val email = data!!.getStringExtra("email")
                val password = data.getStringExtra("password")
                binding.loginEtEmail.setText(email)
                binding.loginEtPassword.setText(password)
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:$connectionResult")
        toast(getString(R.string.google_play_services_error))
    }

    private fun initUI() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        with(binding) {
            loginPbBtnLogin.changeVisibility(false)
            loginPbBtnLoginFB.changeVisibility(false)
            loginPbBtnLoginGoogle.changeVisibility(false)
            loginBtnLoginFB.changeVisibility(
                remoteConfig.getBoolean(
                    REMOTE_CONFIG_SIGNIN_WITH_FACEBOOK
                )
            )
            loginBtnLoginGoogle.changeVisibility(
                remoteConfig.getBoolean(
                    REMOTE_CONFIG_SIGNIN_WITH_GOOGLE
                )
            )
        }
    }

    private fun initListener() {

        //PLAY AS GUEST
        binding.loginTvPlayasguest.setOnClickListener { launchPlayAsGuestActivity() }

        //SIGN IN
        binding.loginTvSignin.setOnClickListener { launchSignInActivity() }

        //LOGIN
        binding.loginBtnLogin.setOnClickListener { //GET USERNAME/PASSWORD
            val email = binding.loginEtEmail.text.toString()
            val password = binding.loginEtPassword.text.toString()
            if (isValidPassword(password) && isValidEmail(email)) {
                hideKeyboard()
                binding.loginPbBtnLogin.changeVisibility(true)
                loginUser(email, password)
            } else {
                toast(getString(R.string.invalid_email_or_pswd))
            }
        }

        binding.loginBtnLoginFB.setOnClickListener {
            binding.loginPbBtnLoginFB.changeVisibility(true)
            loginUserFB()
        }

        binding.loginBtnLoginGoogle.setOnClickListener {
            binding.loginPbBtnLoginGoogle.changeVisibility(true)
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
                        mAuth!!.currentUser
                        binding.loginPbBtnLogin.changeVisibility(false)
                        Utils.AUTHENTIFICATION_TYPE =
                            Utils.AUTHENTIFICATION_ACCOUNT
                        launchMenuActivity()
                    } else {
                        //Error when connecting
                        toast(getString(R.string.invalid_email_or_pswd))
                        binding.loginPbBtnLogin.changeVisibility(false)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loginUserFB() {
        loginButtonFB?.performClick()
    }

    private fun loginUserGoogle() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun initLoginGoogle() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun initLoginButtonFB() {
        mCallbackManager = CallbackManager.Factory.create()

        loginButtonFB = LoginButton(this)
        loginButtonFB?.setReadPermissions("email", "public_profile")
        loginButtonFB?.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.e(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.e(TAG, "facebook:onCancel")
                binding.loginPbBtnLoginFB.visibility = View.INVISIBLE
            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, "facebook:onError", error)
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                mAuth?.currentUser?.let {
                    //Download picture
                    var facebookUserId = ""
                    for (profile in it.providerData) {
                        // check if the provider id matches "facebook.com"
                        if (FacebookAuthProvider.PROVIDER_ID == profile.providerId) {
                            facebookUserId = profile.uid
                        }
                    }
                    val photoUrl = "https://graph.facebook.com/$facebookUserId/picture?height=200"
                    downloadImage(photoUrl)
                }
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                toast(getString(R.string.auth_failed))
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        Log.d(TAG, "token : $idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth?.currentUser
                    toast(getString(R.string.welcome) + user?.displayName)
                    isConnectedWithGoogle = true

                    binding.loginPbBtnLoginGoogle.changeVisibility(false)
                    Utils.AUTHENTIFICATION_TYPE = Utils.AUTHENTIFICATION_GOOGLE
                    launchMenuActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    toast(getString(R.string.auth_failed))
                }
            }
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun signOut() {
        mAuth?.signOut()
        LoginManager.getInstance().logOut()
    }

    private fun launchMenuActivity() {
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

    private fun downloadImage(url: String) {
        Observable.fromCallable {
            var bitmap: Bitmap? = null
            try {
                // Download Image from URL
                val input = URL(url).openStream()
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val user = mAuth!!.currentUser
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val text = "profilePictures/" + user!!.uid + ".png"
            val imagesRef = storageRef.child(text)
            val baos = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = imagesRef.putBytes(data)
            uploadTask.addOnFailureListener { toast("Error while uploading picture profile") }
                .addOnSuccessListener {

                    db.getCurrentUserDataById(user.uid)
                    db.setOnDataUserUpdateListener { player ->
                        if (player == null) {
                            db.insertRegisteredPlayer(
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
                        onUploadSuccess()
                    }
                }
            // RxJava does not accept null return value. Null will be treated as a failure.
            // So just make it return true.
            true
        } // Execute in IO thread, i.e. background thread.
            .subscribeOn(Schedulers.io()) // report or post the result to main thread.
            .subscribe()
    }

    private fun onUploadSuccess() {
        Observable.fromCallable {
            binding.loginPbBtnLoginFB.changeVisibility(false)
            Utils.AUTHENTIFICATION_TYPE =
                Utils.AUTHENTIFICATION_FB
            launchMenuActivity()

        }.subscribeOn(AndroidSchedulers.mainThread())
            .subscribe()

    }

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