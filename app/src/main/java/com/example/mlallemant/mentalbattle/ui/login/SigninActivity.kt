package com.example.mlallemant.mentalbattle.ui.login

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

/**
 * Created by m.lallemant on 25/10/2017.
 */
class SigninActivity : AppCompatActivity() {
    private var etEmail: EditText? = null
    private var etUsername: EditText? = null
    private var etPassword: EditText? = null
    private var btnSignin: Button? = null
    private var pgSignin: ProgressBar? = null
    private var tvCancel: TextView? = null
    private var ivProfileUser: CircleImageView? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthStateListener: AuthStateListener? = null
    private var storage: FirebaseStorage? = null
    private var db: DatabaseManager? = null
    private var isValidCreation = false
    
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_activity)
        mAuth = FirebaseAuth.getInstance()
        mAuth?.signOut()
        db = DatabaseManager.getInstance()
        storage = FirebaseStorage.getInstance()
        initUI()
        initListener()
    }

    public override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthStateListener!!)
    }

    public override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener(mAuthStateListener!!)
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        //TODO fix image picker
//        List<Image> images = ImagePicker.getImages(data);
//        if (images != null && !images.isEmpty()) {
//            iv_profile_user.setImageBitmap(BitmapFactory.decodeFile(images.get(0).getPath()));
//        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initUI() {
        etUsername = findViewById<View>(R.id.signin_et_username) as EditText
        etEmail = findViewById<View>(R.id.signin_et_email) as EditText
        etPassword = findViewById<View>(R.id.signin_et_password) as EditText
        btnSignin =
            findViewById<View>(R.id.signin_btn_signin) as Button
        pgSignin = findViewById<View>(R.id.signin_pb_btn_signin) as ProgressBar
        tvCancel = findViewById<View>(R.id.signin_tv_cancel) as TextView
        ivProfileUser =
            findViewById<View>(R.id.signin_profile_user) as CircleImageView
        pgSignin!!.visibility = View.GONE
    }

    private fun initListener() {
        etEmail!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
            }

            override fun onTextChanged(
                charSequence: CharSequence,
                i: Int,
                i1: Int,
                i2: Int
            ) {
                if (isValidEmail(charSequence.toString())) {
                    etEmail!!.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.orangeColor
                        )
                    )
                } else {
                    etEmail!!.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.redColor
                        )
                    )
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        btnSignin!!.setOnClickListener {
            if (!isValidCreation) {
                val email = etEmail!!.text.toString()
                val password = etPassword!!.text.toString()
                val username = etPassword!!.text.toString()
                Log.e(
                    TAG,
                    "email " + isValidEmail(email)
                )
                Log.e(
                    TAG,
                    "password " + isValidPassword(password)
                )
                Log.e(
                    TAG,
                    "username " + isValidUsername(username)
                )
                if (isValidEmail(email) && isValidPassword(
                        password
                    ) && isValidUsername(username)
                ) {
                    pgSignin!!.visibility = View.VISIBLE
                    etUsername!!.isEnabled = false
                    etEmail!!.isEnabled = false
                    etPassword!!.isEnabled = false
                    createUser(email, password)
                } else {
                    makeToast("Email or password or username incorrect")
                }
            }
        }
        tvCancel!!.setOnClickListener { finish() }
        mAuthStateListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null && !isValidCreation) {
                // User is signed in
                val username = etUsername!!.text.toString()
                val profileChangeRequest =
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(username).build()
                user.updateProfile(profileChangeRequest)
                    .addOnCompleteListener { insertProfilePicture(user) }
            } else {
                pgSignin!!.visibility = View.GONE
                Log.e(TAG, "Error when adding Username")
            }
        }
        ivProfileUser!!.setOnClickListener {
            //TODO fix image picker
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
    }

    private fun makeToast(text: String) {
        Toast.makeText(
            applicationContext, text,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun insertProfilePicture(user: FirebaseUser) {
        val storageRef = storage!!.reference
        val text = "profilePictures/" + user.uid + ".png"
        val imagesRef = storageRef.child(text)
        ivProfileUser!!.isDrawingCacheEnabled = true
        ivProfileUser!!.buildDrawingCache()
        val bitmap = ivProfileUser!!.drawingCache
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnFailureListener { makeToast("Error while uploading picture profile") }
            .addOnSuccessListener {
                db!!.insertRegisteredPlayer(
                    Player(
                        mAuth!!.uid,
                        etUsername!!.text.toString(),
                        0,
                        0,
                        0,
                        0
                    )
                )
                isValidCreation = true
                pgSignin!!.visibility = View.GONE
                mAuth!!.signOut()
                val email = etEmail!!.text.toString()
                val password = etPassword!!.text.toString()
                val intent = Intent()
                intent.putExtra("email", email)
                intent.putExtra("password", password)
                setResult(10, intent)
                finish()
            }
    }

    private fun createUser(email: String, password: String) {
        try {
            mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth!!.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        makeToast("Error, maybe email is already used or password not correct")
                        etUsername!!.isEnabled = true
                        etEmail!!.isEnabled = true
                        etPassword!!.isEnabled = true
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            makeToast("Error")
        }
    }

    companion object {
        private const val TAG = "SigninActivity"
        fun isValidEmail(target: String?): Boolean {
            return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target)
                .matches()
        }

        fun isValidUsername(target: String): Boolean {
            var isValid = false
            if (target.length in 1..14) isValid = true
            return isValid
        }

        fun isValidPassword(target: String): Boolean {
            var isValid = false
            if (target.length in 6..14) isValid = true
            return isValid
        }
    }
}