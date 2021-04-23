package com.example.mlallemant.mentalbattle.ui.game.fragment

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.databinding.PlayFragmentBinding
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER_ID
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_OTHER_PLAYER
import com.example.mlallemant.mentalbattle.utils.Calculation
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Game
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.updateButtonBackspaceResult
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.updateButtonMinusResult
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.updateEditTextResult
import com.example.mlallemant.mentalbattle.utils.Utils

/**
 * Created by m.lallemant on 19/10/2017.
 */
class PlayFragment : Fragment() {

    private var _binding: PlayFragmentBinding? = null
    private val binding get() = _binding!!

    var mCallBack: OnGameFinish? = null

    interface OnGameFinish {
        fun displayWinScreen(
            winnerName: String?,
            looserName: String?,
            winnerScore: Int,
            looserScore: Int,
            resultGame: String?
        )
    }

    private var counter = 0
    private var game: Game? = null
    private var currentPlayerID: String? = null
    private lateinit var db: DatabaseManager
    private var scoreCurrentPlayer = 0
    private var scoreOtherPlayer = 0
    private var calculationList: List<Calculation>? = null
    private var currentPlayerName: String? = null
    private var otherPlayerName: String? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            currentPlayerName = it.getString(BUNDLE_EXTRA_CURRENT_PLAYER)
            otherPlayerName = it.getString(BUNDLE_EXTRA_OTHER_PLAYER)
            currentPlayerID = it.getString(BUNDLE_EXTRA_CURRENT_PLAYER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //init parameters
        initPlayersNames()
        initListenerButton()
        db = DatabaseManager.getInstance()
        game = db.currentGame
        launchCountDown(view, Utils.COUNTDOWN_PLAY)
        db.setScoreChangeListener { score, playerID ->
            if (score != null && playerID != null) {
                if (playerID == currentPlayerID) {
                    binding.playTvPlayer1Score.text = score.toString()
                    scoreCurrentPlayer = score
                } else {
                    binding.playTvPlayer2Score.text = score.toString()
                    scoreOtherPlayer = score
                }
            }
        }

        calculationList = game?.calculationList

        with(binding) {
            calculationList?.let {
                playTvCalculation.text = it[counter].calculText
                playEtResult.addTextChangedListener(object : TextWatcher {

                    override fun beforeTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence,
                        i: Int,
                        i1: Int,
                        i2: Int
                    ) {
                    }

                    override fun afterTextChanged(editable: Editable) {

                        val resultTextPlayer = editable.toString()
                        if (resultTextPlayer == it[counter].result.toString()) {
                            //Player found the correct calculation
                            counter++
                            playTvCalculation.text = it[counter].calculText
                            playEtResult.setText("")
                            scoreCurrentPlayer++
                            if (game?.player1?.id == currentPlayerID) {
                                db.setScorePlayer1ByIdGame(scoreCurrentPlayer, game?.id)
                            } else {
                                db.setScorePlayer2ByIdGame(scoreCurrentPlayer, game?.id)
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBack = if (context is OnGameFinish) {
            context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun launchCountDown(v: View, countdownPlay: Int) {
        countDownTimer = object : CountDownTimer(countdownPlay.toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val remainingTime = "" + millisUntilFinished / 1000
                binding.playTvCounter.text = remainingTime
            }

            override fun onFinish() {
                //GAME FINISHED
                binding.playTvCounter.text = "0"
                countDownTimer?.cancel()
                val win = isCurrentPlayerWining()
                val winnerName: String?
                val looserName: String?
                val resultGame: String
                val winnerScore: Int
                val looserScore: Int
                if (win != null) {
                    if (win) {
                        winnerName = currentPlayerName
                        looserName = otherPlayerName
                        winnerScore = scoreCurrentPlayer
                        looserScore = scoreOtherPlayer
                        resultGame = "YOU WIN !"
                    } else {
                        winnerName = otherPlayerName
                        looserName = currentPlayerName
                        winnerScore = scoreOtherPlayer
                        looserScore = scoreCurrentPlayer
                        resultGame = "YOU LOSE !"
                    }
                    mCallBack?.displayWinScreen(
                        winnerName,
                        looserName,
                        winnerScore,
                        looserScore,
                        resultGame
                    )
                } else {
                    launchCountDown(v, 20000)
                }
            }
        }.start()
    }

    private fun isCurrentPlayerWining(): Boolean? {
        var isWining: Boolean? = false
        val currentScore = binding.playTvPlayer1Score.text.toString().toInt()
        val otherScore = binding.playTvPlayer2Score.text.toString().toInt()
        if (currentScore > otherScore) isWining = true
        if (currentScore == otherScore) isWining = null
        return isWining
    }

    private fun initListenerButton() {
        with(binding) {
            playBtn0.setOnClickListener { updateEditTextResult(0, playEtResult) }
            playBtn1.setOnClickListener { updateEditTextResult(1, playEtResult) }
            playBtn2.setOnClickListener { updateEditTextResult(2, playEtResult) }
            playBtn3.setOnClickListener { updateEditTextResult(3, playEtResult) }
            playBtn4.setOnClickListener { updateEditTextResult(4, playEtResult) }
            playBtn5.setOnClickListener { updateEditTextResult(5, playEtResult) }
            playBtn6.setOnClickListener { updateEditTextResult(6, playEtResult) }
            playBtn7.setOnClickListener { updateEditTextResult(7, playEtResult) }
            playBtn8.setOnClickListener { updateEditTextResult(8, playEtResult) }
            playBtn9.setOnClickListener { updateEditTextResult(9, playEtResult) }
            playBtnMinus.setOnClickListener { updateButtonMinusResult(playEtResult) }
            playBtnBackspace.setOnClickListener { updateButtonBackspaceResult(playEtResult) }
        }

    }

    private fun initPlayersNames() {
        currentPlayerName?.let {
            binding.playTvPlayer1Name.text = it.split(" ".toRegex()).toTypedArray()[0] + " (you)"
        }
        otherPlayerName?.let {
            binding.playTvPlayer2Name.text = it.split(" ".toRegex()).toTypedArray()[0]
        }
    }
}