package com.example.mlallemant.mentalbattle.ui.session.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.TransitionFragmentBinding
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.ui.session.SessionActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Session
import com.example.mlallemant.mentalbattle.utils.Utils
import java.util.*

/**
 * Created by m.lallemant on 17/11/2017.
 */
class TransitionFragment : Fragment() {
    //UI
    private var _binding: TransitionFragmentBinding? = null
    private val binding get() = _binding!!


    //UTILS
    private var session: Session? = null
    private var currentRoundSessionNumber = 0
    private var currentPlayer: Player? = null
    private var isCreator = false
    private var db: DatabaseManager? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TransitionFragmentBinding.inflate(inflater, container, false)
        val bundle = arguments
        session = bundle!!.getParcelable("session")
        currentPlayer = bundle.getParcelable("currentPlayer")
        currentRoundSessionNumber = bundle.getInt("currentRoundSessionNumber")
        isCreator = (activity as SessionActivity?)!!.isCreator
        db = DatabaseManager.getInstance()
        db?.initListenerCurrentSession(session)
        initUI()
        initListener()
        return binding.root
    }

    private fun initUI() {
        var text = "Ranking - Round $currentRoundSessionNumber/$MAX_ROUND"

        binding.sessionTransitionTvRoundNumber.text = text
        val playerModel = ArrayList<Player?>()
        val adapter = RankingPlayerAdapter(playerModel, activity!!)
        binding.sessionTransitionLvRanking.adapter = adapter
        adapter.clear()
        session?.playerList?.sortBy { it?.score }
        val players = session!!.playerList
        adapter.addAll(players!!)
        if (currentRoundSessionNumber >= MAX_ROUND) {
            binding.sessionTransitionTvRoundNumber.text = getString(R.string.final_ranking)
            binding.sessionTransitionBtnNextRound.isEnabled = true
            text = "Return menu"
            binding.sessionTransitionBtnNextRound.text = text
        } else {
            if (isCreator) {
                binding.sessionTransitionBtnNextRound.isEnabled = true
                binding.sessionTransitionBtnNextRound.text = getString(R.string.next_round)
            } else {
                binding.sessionTransitionBtnNextRound.isEnabled = false
                text = "Waiting creator for next round..."
                binding.sessionTransitionBtnNextRound.text = text
            }
        }
    }

    private fun initListener() {
        binding.sessionTransitionBtnNextRound.setOnClickListener {
            if (currentRoundSessionNumber >= MAX_ROUND) {
                (activity as SessionActivity?)!!.deleteSession()
                launchMenuActivity()
            } else {
                if (isCreator) {
                    db!!.updateStateSession(
                        session,
                        Utils.SESSION_STATE_LAUNCH_ROUND
                    )
                }
            }
        }
        db!!.setOnSessionUpdateListener { session ->
            if (session != null) {
                if (session.state == Utils.SESSION_STATE_LAUNCH_ROUND) {
                    launchRoundFragment()
                }
            }
        }
    }

    private fun launchRoundFragment() {
        db!!.removeListenerCurrentSession(session)
        db!!.updateStateSession(
            session,
            Utils.SESSION_STATE_LAUNCH_PARTY
        )
        val rf = RoundFragment()
        val args = Bundle()
        args.putInt("currentRoundSessionNumber", currentRoundSessionNumber)
        args.putParcelable("session", session)
        args.putParcelable("currentPlayer", currentPlayer)
        rf.arguments = args
        val fm = fragmentManager
        val ft = fm!!.beginTransaction()
        ft.replace(R.id.fl_session, rf)
        ft.commit()
    }

    private fun launchMenuActivity() {
        val intent = Intent(activity, MenuActivity::class.java)
        startActivity(intent)
        activity!!.finish()
    }

    companion object {
        private const val MAX_ROUND = 3
    }
}