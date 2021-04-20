package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuSessionFragmentBinding
import com.example.mlallemant.mentalbattle.ui.extention.toast
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_CREATOR
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_IS_CREATOR
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_NAME
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_PASSWORD
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_SESSION
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.OnBackPressedListener
import com.example.mlallemant.mentalbattle.ui.session.SessionActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Session
import com.example.mlallemant.mentalbattle.utils.Utils
import java.util.*

/**
 * Created by m.lallemant on 15/11/2017.
 */
class SessionLauncherFragment : Fragment() {

    private var _binding: MenuSessionFragmentBinding? = null
    private val binding get() = _binding!!

    //Utils
    private var currentPlayer: Player? = null
    private lateinit var db: DatabaseManager
    private var isCreator = false
    private var name: String? = null
    private var password: String? = null
    private var session: Session? = null
    private var nbPlayer = 0
    private var isReady = false
    private var adapter: PlayersAdapter? = null
    private var isEverybodyReady = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MenuSessionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuActivity = activity as MenuActivity?
        currentPlayer = menuActivity?.currentPlayer
        isCreator = arguments?.getBoolean(BUNDLE_EXTRA_CREATOR)?:false
        name = arguments?.getString(BUNDLE_EXTRA_NAME)
        password = arguments?.getString(BUNDLE_EXTRA_PASSWORD)
        session = Session(name, password, Utils.SESSION_STATE_WAITING)
        db = DatabaseManager.getInstance()
        db.initListenerCurrentSession(session)
        initUI()
        initListener()
        menuActivity?.setOnBackPressedListener(object : OnBackPressedListener {
            override fun doBack() {
                quitSession()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initUI() {
        with(binding) {
            lvSessionPlayers.visibility = View.VISIBLE
            ivSessionListView.visibility = View.INVISIBLE
            adapter = PlayersAdapter(ArrayList(), session, isCreator, requireContext())
            lvSessionPlayers.adapter = adapter
            adapter?.clear()
            if (isCreator) {
                btnSessionLaunchReady.text = getString(R.string.launch)
            } else {
                btnSessionLaunchReady.text = getString(R.string.ready_question)
            }
            nbPlayer++
            tvSessionNbPlayer.text = nbPlayer.toString()
        }
    }

    private fun initListener() {
        with(binding) {
            ivSessionBack.setOnClickListener {
                onLongClick()
            }
            btnSessionLaunchReady.setOnClickListener {
                onClick()
            }
            db.setOnSessionUpdateListener { session_ ->
                if (session_ != null) {
                    session = session_
                    val players = session_.playerList
                    adapter?.clear()
                    adapter?.addAll(players)
                    tvSessionNbPlayer.text = players.size.toString()
                    var currentPlayerIsInList = false
                    var isEverybodyReadyTmp = true
                    for (player in players) {
                        if (player.id == currentPlayer?.id) {
                            currentPlayerIsInList = true
                        }
                        if (player.ready != Utils.SESSION_RDY_YES) {
                            isEverybodyReadyTmp = false
                        }
                    }
                    if (session_.state == Utils.SESSION_STATE_LAUNCH_PARTY) {
                        launchCountDown()
                    }
                    isEverybodyReady = isEverybodyReadyTmp
                    if (isCreator) {
                        btnSessionLaunchReady.background = when(isEverybodyReady) {
                            true -> ContextCompat.getDrawable(activity!!, R.drawable.shape_green)
                            else -> ContextCompat.getDrawable(activity!!, R.drawable.shape_orange)

                        }
                    }
                    if (!currentPlayerIsInList) {
                        displayAlertSession(getString(R.string.removed_from_session))
                        quitSession()
                    }
                } else {
                    displayAlertSession(getString(R.string.session_does_nor_exist))
                    quitSession()
                }
            }
        }
    }

    private fun onClick() {
        with(binding) {
            if (isCreator) {
                if (isEverybodyReady) {
                    db.updateStateSession(session, Utils.SESSION_STATE_LAUNCH_PARTY)
                } else {
                    toast(getString(R.string.players_not_ready))
                }
            } else {
                btnSessionLaunchReady.apply {
                    if (!isReady) {
                        isReady = true
                        text = getString(R.string.ready_exclamation)
                        background = ContextCompat.getDrawable(activity!!, R.drawable.shape_green)
                        db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_YES)
                    } else {
                        isReady = false
                        text = getString(R.string.ready_question)
                        background = ContextCompat.getDrawable(activity!!, R.drawable.shape_orange)
                        db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_NO)
                    }
                }
            }
        }
    }

    private fun onLongClick() {
        db.removeListenerCurrentSession(session)
        db.insertPlayerInLobby(currentPlayer)
        if (isCreator) {
            db.deleteSession(session)
            returnSelectorFragment()
        } else {
            quitSession()
            db.insertPlayerInLobby(currentPlayer)
        }
    }

    private fun returnSelectorFragment() {
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, SelectorFragment())?.commit()
    }

    private fun quitSession() {
        db.updatePlayerNew(session, currentPlayer, Utils.SESSION_LEFT)
        db.removeListenerCurrentSession(session)
        db.removePlayerInSession(session, currentPlayer)
        returnSelectorFragment()
    }

    private fun launchSessionActivity() {
        db.removeListenerCurrentSession(session)
        val intent = Intent(activity, SessionActivity::class.java).apply {
            putExtra(BUNDLE_EXTRA_IS_CREATOR, isCreator)
            putExtra(BUNDLE_EXTRA_SESSION, session)
            putExtra(BUNDLE_EXTRA_CURRENT_PLAYER, currentPlayer)
        }
        startActivity(intent)
        activity?.finish()
    }

    private fun displayAlertSession(message: String) {
        AlertDialog.Builder(requireContext()).create().apply {
            setTitle(getString(R.string.alert_session_title))
            setMessage(message)
            setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_session_btn_ok)) {
                    dialog, _ -> dialog.dismiss()
            }
        }.show()
    }

    private fun launchCountDown() {
        with(binding) {
            tvSessionNbPlayerTitle.text = getString(R.string.launch_count_down_text)
            tvSessionNbPlayer.text = "5"
            object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tvSessionNbPlayer.text = (millisUntilFinished.toInt() / 1000).toString()
                }

                override fun onFinish() {
                    tvSessionNbPlayer.text = "0"
                    launchSessionActivity()
                }
            }.start()
        }
    }
}