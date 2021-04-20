package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuSelectorFragmentBinding
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_CREATOR
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Utils

/**
 * Created by m.lallemant on 09/11/2017.
 */
class SelectorFragment : Fragment() {

    private var _binding: MenuSelectorFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MenuSelectorFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentPlayer = (activity as? MenuActivity)?.currentPlayer
        val db = DatabaseManager.getInstance()
        db.insertPlayerInLobby(currentPlayer)
        initUI()
        initListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initUI() {
        if (Utils.AUTHENTIFICATION_TYPE == Utils.AUTHENTIFICATION_GUEST) {
            binding.selectorFriends.isEnabled = false
        }
    }

    private fun initListener() {
        with(binding) {
            selectorPlay.setOnClickListener { launchPlayFragment() }
            selectorJoinSession.setOnClickListener { launchJoinFragment() }
            selectorCreateSession.setOnClickListener { launchCreateFragment() }
            selectorFriends.setOnClickListener { launchFriendsFragment() }
        }
    }

    private fun launchPlayFragment() {
        val playFragment = PlayFragment()
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, playFragment)?.commit()
    }

    private fun launchCreateFragment() {
        val createJoinFragment = CreateJoinFragment()
        val bundle = Bundle().apply {
            putBoolean(BUNDLE_EXTRA_CREATOR, true)
        }
        createJoinFragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, createJoinFragment)?.commit()
    }

    private fun launchJoinFragment() {
        val createJoinFragment = CreateJoinFragment()
        val bundle = Bundle().apply {
            putBoolean(BUNDLE_EXTRA_CREATOR, false)
        }
        createJoinFragment.arguments = bundle
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, createJoinFragment)?.commit()
    }

    private fun launchFriendsFragment() {
        if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
            val friendsFragment = FriendsFragment()
            fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, friendsFragment)?.commit()
        }
    }
}