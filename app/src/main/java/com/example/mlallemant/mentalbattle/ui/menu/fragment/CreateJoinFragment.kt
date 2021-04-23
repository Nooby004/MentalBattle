package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuCreateFragmentBinding
import com.example.mlallemant.mentalbattle.ui.extention.toast
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_CREATOR
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_IS_CREATOR
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_NAME
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_PASSWORD
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Session
import com.example.mlallemant.mentalbattle.utils.Utils

/**
 * Created by m.lallemant on 15/11/2017.
 */
class CreateJoinFragment : Fragment() {

    private var _binding: MenuCreateFragmentBinding? = null
    private val binding get() = _binding!!

    //Utils
    private var currentPlayer: Player? = null
    private lateinit var db: DatabaseManager
    private var isCreator = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuCreateFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuActivity = activity as MenuActivity?
        currentPlayer = menuActivity?.currentPlayer
        isCreator = arguments?.getBoolean(BUNDLE_EXTRA_CREATOR) ?: false
        db = DatabaseManager.getInstance()
        db.deletePlayerInLobby(currentPlayer)
        initUI()
        initListener()
    }

    private fun initUI() {
        with(binding) {
            pbCreateSession.visibility = View.INVISIBLE
            btnCreateSession.text = when (isCreator) {
                true -> getString(R.string.create)
                else -> getString(R.string.join)
            }
            tvTitleSession.text = when (isCreator) {
                true -> getString(R.string.create_session)
                else -> getString(R.string.join_session)
            }
        }
    }

    private fun initListener() {
        with(binding) {
            ivCreateBack.setOnClickListener {
                returnSelectorFragment()
                db.insertPlayerInLobby(currentPlayer)
            }
            btnCreateSession.setOnClickListener {
                pbCreateSession.visibility = View.VISIBLE
                if (isCreator) {
                    createSession()
                } else {
                    joinSession()
                }
            }
        }
    }

    private fun createSession() {
        val sessionName = binding.etCreateSessionName.text.toString()
        val sessionPassword = binding.etCreateSessionPassword.text.toString()
        if (sessionName.length > 4 && sessionPassword.length > 5) {
            val session = Session(sessionName, sessionPassword, Utils.SESSION_STATE_WAITING)
            db.initCheckSessionExist(session)
            db.setOnSessionExistListener { isExist ->
                if (isExist) {
                    toast(getString(R.string.session_already_exist))
                } else {
                    db.insertSession(session)
                    val playerForSession = Player(
                        currentPlayer?.id,
                        currentPlayer?.name,
                        currentPlayer?.score,
                        null,
                        null,
                        null,
                        Utils.SESSION_RDY_YES,
                        Utils.SESSION_CREATOR
                    )
                    db.insertPlayerInSession(session, playerForSession)
                    launchSessionLauncherFragment(true)
                }
                binding.pbCreateSession.visibility = View.INVISIBLE
            }
        } else {
            toast(getString(R.string.name_or_pswd_not_long_enough))
        }
    }

    private fun joinSession() {
        val sessionName = binding.etCreateSessionName.text.toString()
        val sessionPassword = binding.etCreateSessionPassword.text.toString()
        if (sessionName.isNotEmpty() && sessionPassword.isNotEmpty()) {
            val session = Session(sessionName, sessionPassword, Utils.SESSION_STATE_WAITING)
            db.initCheckSessionExist(session)
            db.setOnSessionExistListener { isExist ->
                if (isExist) {
                    val playerForSession = Player(
                        currentPlayer?.id,
                        currentPlayer?.name,
                        currentPlayer?.score,
                        null,
                        null,
                        null,
                        Utils.SESSION_RDY_KO,
                        Utils.SESSION_NEW_YES
                    )
                    db.insertPlayerInSession(session, playerForSession)
                    launchSessionLauncherFragment(false)
                } else {
                    toast(getString(R.string.bad_name_or_pswd))
                }
                binding.pbCreateSession.visibility = View.INVISIBLE
            }
        } else {
            toast(getString(R.string.bad_name_or_pswd))
        }
    }

    private fun returnSelectorFragment() {
        val selectorFragment = SelectorFragment()
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, selectorFragment)
            ?.commit()
    }

    private fun launchSessionLauncherFragment(isCreator: Boolean) {
        val sessionLauncherFragment = SessionLauncherFragment()
        val bundle = Bundle().apply {
            putBoolean(BUNDLE_EXTRA_IS_CREATOR, isCreator)
            putString(BUNDLE_EXTRA_NAME, binding.etCreateSessionName.text.toString())
            putString(BUNDLE_EXTRA_PASSWORD, binding.etCreateSessionPassword.text.toString())
        }
        sessionLauncherFragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, sessionLauncherFragment)
            ?.commit()
    }
}