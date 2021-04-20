package com.example.mlallemant.mentalbattle.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

/**
 * Created by m.lallemant on 26/10/2017.
 */
class PlayAsGuest : AppCompatActivity() {
    private var etGuestName: EditText? = null
    private var btnPlay: Button? = null
    private var pbBtnPlay: ProgressBar? = null
    private var tvCancel: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var currentPlayer: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_as_guest_activity)
        initUI()
        initListener()
        mAuth = FirebaseAuth.getInstance()
        val db = DatabaseManager.getInstance()
        db.initFriendList()
    }

    public override fun onStop() {
        super.onStop()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
    }

    private fun initUI() {
        etGuestName = findViewById<View>(R.id.guest_et_guest_name) as EditText
        btnPlay = findViewById<View>(R.id.guest_btn_play) as Button
        pbBtnPlay = findViewById<View>(R.id.guest_pb_btn_play) as ProgressBar
        tvCancel = findViewById<View>(R.id.guest_tv_cancel) as TextView
        val tv_log = findViewById<View>(R.id.guest_tv_log) as TextView
        btnPlay!!.text = "CONTINUE AS GUEST"
        tv_log.text = ""
        pbBtnPlay!!.visibility = View.GONE
    }

    private fun initListener() {
        tvCancel!!.setOnClickListener { launchLoginActivity() }
        btnPlay!!.setOnClickListener {
            val username = etGuestName!!.text.toString()
            if (username.length in 4..11) {
                pbBtnPlay!!.visibility = View.VISIBLE
                signInAnonymously(etGuestName!!.text.toString())
                btnPlay!!.isEnabled = false
            } else {
                makeToast("Choose a correct name (between 3 and 13 characters)")
            }
        }
    }

    private fun signInAnonymously(username: String) {
        Utils.AUTHENTIFICATION_TYPE =
            Utils.AUTHENTIFICATION_GUEST
        mAuth!!.signInAnonymously()
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG,
                        "signInAnonymously:success"
                    )
                    val user = mAuth!!.currentUser
                    if (user != null) {
                        val profileUpdates =
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(username).build()
                        user.updateProfile(profileUpdates)
                        currentPlayer = Player(user.uid, username, 0, 0, 0, 0)
                        Utils.AUTHENTIFICATION_TYPE =
                            Utils.AUTHENTIFICATION_GUEST
                        launchMenuActivity(currentPlayer!!)
                    }
                } else {
                    Log.w(
                        TAG,
                        "signInAnonymously:failure",
                        task.exception
                    )
                    makeToast("Authentification failed")
                    pbBtnPlay!!.visibility = View.INVISIBLE
                    btnPlay!!.isEnabled = true
                }
            }
    }

    private fun launchMenuActivity(currentPlayer: Player) {
        val intent = Intent(this@PlayAsGuest, MenuActivity::class.java)
        intent.putExtra("currentPlayer", currentPlayer)
        startActivity(intent)
        finish()
    }

    private fun makeToast(text: String) {
        Toast.makeText(
            applicationContext, text,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun launchLoginActivity() {
        val intent = Intent(this@PlayAsGuest, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "PlayAsGuest"
    }
}