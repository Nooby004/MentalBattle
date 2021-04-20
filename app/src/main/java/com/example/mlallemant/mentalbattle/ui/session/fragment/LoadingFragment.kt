package com.example.mlallemant.mentalbattle.ui.session.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.ui.session.SessionActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Session
import com.example.mlallemant.mentalbattle.utils.Utils

/**
 * Created by m.lallemant on 17/11/2017.
 */
class LoadingFragment : Fragment() {

    //UTILS
    private var db: DatabaseManager? = null
    private var currentPlayer: Player? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.loading_activity, container, false)
        val bundle = arguments
        val session: Session =
            bundle!!.getParcelable("session")!!

        currentPlayer = bundle.getParcelable("currentPlayer")
        db = DatabaseManager.getInstance()
        db?.initListenerCurrentSession(session)
        initListener()
        return v
    }

    private fun initListener() {
        db!!.setOnSessionUpdateListener { session ->
            if (session != null) {
                var isEverybodyReady = true
                for (player in session.playerList) {
                    if (player.ready == Utils.SESSION_RDY_NO) {
                        isEverybodyReady = false
                        break
                    }
                }
                if (isEverybodyReady) {
                    launchTransitionFragment(session)
                }
            }
        }
    }

    private fun launchTransitionFragment(session: Session) {
        val sa = activity as SessionActivity?
        sa!!.addCurrentRoundSessionNumber()
        db!!.removeListenerCurrentSession(session)
        val tf = TransitionFragment()
        val args = Bundle()
        args.putInt("currentRoundSessionNumber", sa.currentRoundSessionNumber)
        args.putParcelable("session", session)
        args.putParcelable("currentPlayer", currentPlayer)
        tf.arguments = args
        val fm = fragmentManager
        val ft = fm!!.beginTransaction()
        ft.replace(R.id.fl_session, tf)
        ft.commit()
    }
}