package com.example.mlallemant.mentalbattle.ui.game

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.GameActivityBinding
import com.example.mlallemant.mentalbattle.ui.game.fragment.PlayFragment
import com.example.mlallemant.mentalbattle.ui.game.fragment.PlayFragment.OnGameFinish
import com.example.mlallemant.mentalbattle.ui.game.fragment.PlayerFindFragment
import com.example.mlallemant.mentalbattle.ui.game.fragment.PlayerFindFragment.OnCountdownFinish
import com.example.mlallemant.mentalbattle.ui.game.fragment.WinFragment
import com.example.mlallemant.mentalbattle.ui.game.fragment.WinFragment.OnNextGame
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.utils.*
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by m.lallemant on 16/10/2017.
 */
class GameActivity : AppCompatActivity(), OnCountdownFinish, OnGameFinish, OnNextGame {

    companion object {
        const val BUNDLE_EXTRA_CURRENT_PLAYER = "currentPlayer"
        const val BUNDLE_EXTRA_OTHER_PLAYER = "otherPlayer"
        const val BUNDLE_EXTRA_CURRENT_PLAYER_ID = "currentPlayerID"
        const val BUNDLE_EXTRA_OTHER_PLAYER_ID = "otherPlayerID"
        const val BUNDLE_EXTRA_GAME_ID = "gameID"
        const val BUNDLE_EXTRA_WINNER_NAME = "winnerName"
        const val BUNDLE_EXTRA_LOOSER_NAME = "looserName"
        const val BUNDLE_EXTRA_WINNER_SCORE = "winnerScore"
        const val BUNDLE_EXTRA_LOOSER_SCORE = "looserScore"
        const val BUNDLE_EXTRA_RESULT_GAME = "resultGame"
    }

    private var _binding: GameActivityBinding? = null
    private val binding get() = _binding!!

    private var gameIsFinished = false
    private var game: Game? = null
    private lateinit var db: DatabaseManager
    private var currentPlayer: Player? = null
    private var otherPlayer: Player? = null
    private var appGoesToBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = GameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentPlayerId: String? = intent.extras?.getString(BUNDLE_EXTRA_CURRENT_PLAYER_ID)

        db = DatabaseManager.getInstance()
        game = db.currentGame

        game?.let {
            if (it.player1?.id == currentPlayerId) {
                currentPlayer = it.player1
                otherPlayer = it.player2
            } else {
                currentPlayer = it.player2
                otherPlayer = it.player1
            }
        }
        db.deletePlayerSearchingPlayer(currentPlayer)

        //Create lobby fragment
        val lf = PlayerFindFragment()
        val args = Bundle().apply {
            currentPlayer?.let { putString(BUNDLE_EXTRA_CURRENT_PLAYER, it.name) }
            otherPlayer?.let { putString(BUNDLE_EXTRA_OTHER_PLAYER, it.name) }
        }
        lf.arguments = args

        supportFragmentManager.beginTransaction().add(R.id.fl_game, lf).commit()

        db.setOnRageQuitListener {
            if (!gameIsFinished) {
                displayWinScreen(
                    currentPlayer?.name ?: "",
                    otherPlayer?.name ?: "",
                    999,
                    0,
                    getString(R.string.result_game_ragequit)
                )
            }
        }
    }

    public override fun onStop() {
        super.onStop()
        if (!appGoesToBackground) {
            db.deleteCurrentGame(game)
            finish()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (!appGoesToBackground) {
            db.deleteCurrentGame(game)
            finish()
        }
        _binding = null
    }

    override fun onBackPressed() {
        gameIsFinished = true
        db.deleteCurrentGame(game)
        db.notifyFriendsYouAreDisconnected(currentPlayer)
        finish()
        super.onBackPressed()
    }

    public override fun onPause() {
        super.onPause()
        appGoesToBackground = true
    }

    public override fun onResume() {
        super.onResume()
        appGoesToBackground = false
    }

    override fun launchGame() {

        //create play fragment
        if (!gameIsFinished) {
            val playFragment = PlayFragment()
            val args = Bundle().apply {
                currentPlayer?.let {
                    putString(BUNDLE_EXTRA_CURRENT_PLAYER, it.name)
                    putString(BUNDLE_EXTRA_CURRENT_PLAYER_ID, it.id)
                }
                otherPlayer?.let {
                    putString(BUNDLE_EXTRA_OTHER_PLAYER, it.name)
                    putString(BUNDLE_EXTRA_OTHER_PLAYER_ID, it.id)
                }
                game?.let { putString(BUNDLE_EXTRA_GAME_ID, it.id) }
            }
            playFragment.arguments = args
            supportFragmentManager.beginTransaction().replace(R.id.fl_game, playFragment).commit()
        }
    }

    override fun displayWinScreen(
        winnerName: String?,
        looserName: String?,
        winnerScore: Int,
        looserScore: Int,
        resultGame: String?
    ) {
        gameIsFinished = true
        if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
            calculXpGain(winnerName ?: "", winnerScore, looserScore)
        }
        //create win fragment
        val winFragment = WinFragment()
        val args = Bundle().apply {
            putString(BUNDLE_EXTRA_WINNER_NAME, winnerName)
            putString(BUNDLE_EXTRA_LOOSER_NAME, looserName)
            putString(BUNDLE_EXTRA_WINNER_SCORE, winnerScore.toString())
            putString(BUNDLE_EXTRA_LOOSER_SCORE, looserScore.toString())
            putString(BUNDLE_EXTRA_RESULT_GAME, resultGame)
        }
        winFragment.arguments = args
        supportFragmentManager.beginTransaction().replace(R.id.fl_game, winFragment).commit()
    }

    override fun launchNextGame() {
        //Return on LoginActivity
        db.deleteCurrentGame(game)
        launchMenuActivity(currentPlayer)
    }

    private fun launchMenuActivity(currentPlayer: Player?) {
        val intent = Intent(this@GameActivity, MenuActivity::class.java)
        intent.putExtra(BUNDLE_EXTRA_CURRENT_PLAYER, currentPlayer)
        this.startActivity(intent)
        finish()
    }

    private fun calculXpGain(winnerName: String, winnerScore: Int, looserScore: Int) {
        var gainXP: Int
        var currentPlayerWin = false
        if (winnerName == currentPlayer!!.name) {
            currentPlayerWin = true
        }
        val r = Random()
        val rand = r.nextInt(4 - 1) + 1
        val base: Int
        if (currentPlayerWin) {
            currentPlayer?.let {
                db.setNbWinLoseByPlayer(it, it.nb_win?:0 + 1, it.nb_lose?:0)
                base = winnerScore * 5 + rand * RankComputer().getLevelByXp(currentPlayer?.xp?:0)
                gainXP = (base + 0.3 * base).roundToInt()
            }
        } else {
            currentPlayer?.let {
                db.setNbWinLoseByPlayer(currentPlayer, it.nb_win?:0, currentPlayer?.nb_lose?:0 + 1)
                base = looserScore * 5 + rand * RankComputer().getLevelByXp(currentPlayer?.xp?:0)
                gainXP = (base - 0.2 * base).roundToInt()
            }
        }
        if (winnerScore == 999) {
            gainXP = 0
            db.setNbWinLoseByPlayer(otherPlayer, otherPlayer?.nb_win?:0, otherPlayer?.nb_lose?:0 + 1)

            val xpToSet = currentPlayer?.xp?:0 + gainXP
            db.setCurrentPlayerXp(currentPlayer, xpToSet)
        }
        //SET SCORE CURRENT PLAYER
    }
}