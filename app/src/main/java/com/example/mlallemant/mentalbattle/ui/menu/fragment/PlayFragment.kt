package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuPlayFragmentBinding
import com.example.mlallemant.mentalbattle.ui.extention.setImage
import com.example.mlallemant.mentalbattle.ui.extention.toast
import com.example.mlallemant.mentalbattle.ui.game.GameActivity
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER_ID
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_GAME_ID
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Game
import com.example.mlallemant.mentalbattle.utils.Game.Companion.generateCalculationList
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Utils
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

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
    private lateinit var db: DatabaseManager

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        compositeDisposable.dispose()
        isSearchingGame = false
        with(binding) {
            selectPlayTvInfo.text = getString(R.string.click_to_play)
            selectPlayIvPlay.setImage(R.drawable.ic_select_play, requireContext())
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
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, selectorFragment)?.commit()
    }

    private fun launchSearchingGameTask() {
        isSearchingGame = true
        with(binding) {
            selectPlayTvInfo.text = getString(R.string.searching)
            selectPlayIvPlay.setImage(R.drawable.ic_select_cancel, requireContext())
            selectPlayPgPlay.visibility = View.VISIBLE
        }
        currentGame = null
        searchGameTask()
    }

    private fun launchGameActivity(game: Game) {
        val intent = Intent(activity, GameActivity::class.java).apply {
            putExtra(BUNDLE_EXTRA_GAME_ID, game.id)
            putExtra(BUNDLE_EXTRA_CURRENT_PLAYER_ID, currentPlayer?.id)
        }
        startActivity(intent)
        activity?.finish()
    }

    private fun updateUI(game: Game?) {
        currentGame = game
        if (game != null) {
            if (game.player1?.id != "" && game.player2?.id != "") {
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

    private fun searchGameTask() {
        compositeDisposable.add(
            Single.defer { Single.just(searchGame()) }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .timeout(Utils.SEARCH_TIME.toLong(), TimeUnit.MILLISECONDS)
                .subscribe(
                    { game -> updateUI(game) },
                    { error -> processError(error) }
                )
        )
    }

    private fun searchGame() : Game {
        var returnGame: Game
        val availableGame = db.findAvailableGame()
        if (availableGame == null) {
            // if no game available, we create one
            Log.e(TAG, "no game available")
            val tmpPlayer = Player("", "", 0, 0, 0, 0)
            val id = UUID.randomUUID().toString()
            val game = Game(id, currentPlayer, tmpPlayer, generateCalculationList())
            db.insertAvailableGame(game)
            returnGame = db.getAvailableGame(game)
        } else {
            // else we insert player1 in game available
            Log.e(TAG, "Game available")
            if (availableGame.player1?.id == "") {
                db.insertPlayer1InAvailableGame(currentPlayer, availableGame)
                Log.e(TAG, "Game Player1 inserted")
            } else {
                db.insertPlayer2InAvailableGame(currentPlayer, availableGame)
                Log.e(TAG, "Game Player2 inserted")
            }
            while (true) {
                returnGame = db.getAvailableGame(availableGame)
                if (returnGame?.player1 != null && returnGame.player2 != null) {
                    if (returnGame.player1?.id != "" && returnGame.player2?.id != "") {
                        break
                    }
                }
            }
        }
        return returnGame
    }

    private fun processError(error: Throwable) {
        if (error is TimeoutException) {
            db.deleteAvailableGame(currentGame)
        } else {
            toast(getString(R.string.serach_game_timeout))
            cancelSearch()
            Log.e(javaClass.simpleName, error.message, error)
        }
    }

    companion object {
        private const val TAG = "searchGameTask"
    }
}