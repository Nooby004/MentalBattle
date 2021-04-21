package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuPlayFragmentBinding
import com.example.mlallemant.mentalbattle.ui.game.GameActivity
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER_ID
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_GAME_ID
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Game
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.SearchGameTask

/**
 * Created by m.lallemant on 10/11/2017.
 */
class PlayFragment : Fragment() {

    private var _binding: MenuPlayFragmentBinding? = null
    private val binding get() = _binding!!

    //Utils
    private var isSearchingGame = false
    private var currentGame: Game? = null
    private var currentPlayer: Player? = null
    private var searchGameTask: SearchGameTask? = null
    private lateinit var db: DatabaseManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuPlayFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuActivity = activity as MenuActivity?
        currentPlayer = menuActivity?.currentPlayer
        db = DatabaseManager.getInstance()
        initUI()
        initListener()
    }

    override fun onStop() {
        super.onStop()
        cancelSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun cancelSearch() {
        // TODO convert to RX
        if (searchGameTask != null) {
            searchGameTask?.cancel(true)
            searchGameTask = null
        }
        isSearchingGame = false
        with(binding) {
            selectPlayTvInfo.text = getString(R.string.click_to_play)
            selectPlayIvPlay.setImageDrawable(
                ContextCompat.getDrawable(
                    activity!!,
                    R.drawable.ic_select_play
                )
            )
            selectPlayPgPlay.visibility = View.INVISIBLE
        }
    }

    private fun initUI() {
        binding.selectPlayTvInfo.text = getString(R.string.click_onPlay_to_search)
    }

    private fun initListener() {
        binding.selectPlayIvPlay.setOnClickListener {
            if (!isSearchingGame) {
                launchSearchingGameTask()
            } else {
                cancelSearch()
            }
        }
        binding.selectPlayIvBack.setOnClickListener { returnSelectorFragment() }
    }

    private fun returnSelectorFragment() {
        val selectorFragment = SelectorFragment()
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, selectorFragment)
            ?.commit()
    }

    private fun launchSearchingGameTask() {
        isSearchingGame = true
        binding.selectPlayTvInfo.text = getString(R.string.searching)
        binding.selectPlayIvPlay.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_select_cancel
            )
        )
        binding.selectPlayPgPlay.visibility = View.VISIBLE
        currentGame = null
        searchGameTask = SearchGameTask { game -> updateUI(game) }
        searchGameTask?.setParams(currentPlayer, currentGame)
        // TODO convert to RX
        searchGameTask?.execute("")
    }

    private fun launchGameActivity(game: Game) {
        val intent = Intent(activity, GameActivity::class.java)
        intent.putExtra(BUNDLE_EXTRA_GAME_ID, game.id)
        intent.putExtra(BUNDLE_EXTRA_CURRENT_PLAYER_ID, currentPlayer!!.id)
        startActivity(intent)
        activity?.finish()
    }

    private fun updateUI(game: Game?) {
        currentGame = searchGameTask?.currentGame
        currentPlayer = searchGameTask?.currentPlayer
        if (game != null) {
            if (game.player1!!.id != "" && game.player2!!.id != "") {
                db.insertInProgressGame(game)
                db.initListenerCurrentGame(game)
                db.deleteAvailableGame(game)
                binding.selectPlayTvInfo.text = getString(R.string.game_found)
                //Launch game
                launchGameActivity(game)
            } else {
                db.deleteAvailableGame(game)
                db.deletePlayerSearchingPlayer(currentPlayer)
            }
        } else {
            launchSearchingGameTask()
        }
    }
}