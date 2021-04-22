package com.example.mlallemant.mentalbattle.ui.session.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.RoundFragmentBinding
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.Companion.BUNDLE_EXTRA_SESSION
import com.example.mlallemant.mentalbattle.utils.*
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.checkLengthText
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.removeLastChar
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.updateButtonBackspaceResult
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.updateButtonMinusResult
import com.example.mlallemant.mentalbattle.utils.GameUtils.Companion.updateEditTextResult
import java.util.*

/**
 * Created by m.lallemant on 17/11/2017.
 */
class RoundFragment : Fragment() {
    //UI
    private var _binding: RoundFragmentBinding? = null
    private val binding get() = _binding!!


    //Utils
    private var countDownTimer: CountDownTimer? = null
    private lateinit var db: DatabaseManager
    private var session: Session? = null
    private var currentPlayer: Player? = null
    private var counter = 0
    private var roundScore = 0
    private var totalScore = 0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = RoundFragmentBinding.inflate(inflater, container, false)

        val bundle = arguments
        session = bundle!!.getParcelable(BUNDLE_EXTRA_SESSION)
        currentPlayer = bundle.getParcelable(BUNDLE_EXTRA_CURRENT_PLAYER)
        db = DatabaseManager.getInstance()
        db.initListenerCurrentSession(session)
        initUI()
        initListener()

        return binding.root
    }

    private fun initUI() {

        binding.playTvPlayer1Name.text = getString(R.string.round_score)
        binding.playTvPlayer1Score.text = roundScore.toString()
        binding.playTvPlayer2Name.text = getString(R.string.total_score)

        db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_NO)
        initListenerButton()
        launchCountDown()
        binding.playTvCalculation.text = session?.calculationList?.get(counter)?.calculText ?: ""
    }

    private fun initListener() {
        db.setOnSessionUpdateListener { session_ ->
            session = session_
            for (player in session_.playerList) {
                if (player.id == currentPlayer?.id) {
                    totalScore = player.score?:0
                    binding.playTvPlayer2Score.text = totalScore.toString()
                }
            }
        }
        binding.playEtResult.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun afterTextChanged(editable: Editable) {
                val resultTextPlayer = editable.toString()
                if (resultTextPlayer == session?.calculationList?.get(counter)?.result.toString()) {
                    //Player found the correct result
                    counter++
                    binding.playTvCalculation.text = session?.calculationList?.get(counter)?.calculText
                    binding.playEtResult.setText("")
                    totalScore++
                    roundScore++
                    db.updateScoreCurrentPlayerInSession(session, currentPlayer, totalScore)
                    binding.playTvPlayer1Score.text = roundScore.toString()
                }
            }
        })
    }

    private fun launchCountDown() {
        countDownTimer = object :
            CountDownTimer(Utils.COUNTDOWN_PLAY.toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val remainingTime = "" + millisUntilFinished / 1000
                binding.playTvCounter.text = remainingTime
            }

            override fun onFinish() {
                //GAME FINISHED
                binding.playTvCounter.text = "0"
                countDownTimer?.cancel()
                db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_YES)
                launchLoadingFragment()
            }
        }.start()
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

    private fun launchLoadingFragment() {
        for (player in session!!.playerList) {
            if (player.new_ == Utils.SESSION_CREATOR) {
                if (player.id == currentPlayer!!.id) {
                    db.updateCalculationListInSession(session, generateCalculationList())
                }
            }
        }
        db.removeListenerCurrentSession(session)
        val lf = LoadingFragment()
        val args = Bundle().apply {
            putParcelable(BUNDLE_EXTRA_SESSION, session)
            putParcelable(BUNDLE_EXTRA_CURRENT_PLAYER, currentPlayer)
        }
        lf.arguments = args
        val fm = fragmentManager
        val ft = fm!!.beginTransaction()
        ft.replace(R.id.fl_session, lf)
        ft.commit()
    }

    private fun generateCalculationList(): List<Calculation> {
        val calculationList_: MutableList<Calculation> = ArrayList()
        for (i in 0..49) {
            calculationList_.add(Calculation())
        }
        return calculationList_
    }
}