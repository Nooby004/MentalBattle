package com.example.mlallemant.mentalbattle.ui.session

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.SessionActivityBinding
import com.example.mlallemant.mentalbattle.ui.session.fragment.RoundFragment
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Session

/**
 * Created by m.lallemant on 17/11/2017.
 */
class SessionActivity : AppCompatActivity() {
    //UI
    private var _binding: SessionActivityBinding? = null
    private val binding get() = _binding!!

    //Utils
    private var session: Session? = null
    private lateinit var db: DatabaseManager
    private var currentPlayer: Player? = null
    var currentRoundSessionNumber = 0
        private set
    var isCreator = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = SessionActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = intent.getParcelableExtra("session")
        currentPlayer = intent.getParcelableExtra("currentPlayer")
        isCreator = intent.getBooleanExtra("isCreator", false)
        db = DatabaseManager.getInstance()
        launchRoundFragment()
    }

    override fun onBackPressed() {
        if (isCreator) {
            db.deleteSession(session)
        } else {
            db.removePlayerInSession(session, currentPlayer)
        }
    }

    fun deleteSession() {
        db.deleteSession(session)
    }

    fun addCurrentRoundSessionNumber() {
        currentRoundSessionNumber++
    }

    private fun launchRoundFragment() {
        db.removeListenerCurrentSession(session)
        val roundFragment = RoundFragment()

        roundFragment.arguments = Bundle().apply {
            putParcelable("session", session)
            putParcelable("currentPlayer", currentPlayer)
        }

        supportFragmentManager.beginTransaction().replace(R.id.fl_session, roundFragment).commit()
    }
}