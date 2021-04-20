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
import com.example.mlallemant.mentalbattle.utils.*
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
    private var db: DatabaseManager? = null
    private var session: Session? = null
    private var currentPlayer: Player? = null
    private var COUNTER = 0
    private var roundScore = 0
    private var totalScore = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = RoundFragmentBinding.inflate(inflater, container, false)

        val bundle = arguments
        session = bundle!!.getParcelable("session")
        currentPlayer = bundle.getParcelable("currentPlayer")
        db = DatabaseManager.getInstance()
        db?.initListenerCurrentSession(session)
        initUI()
        initListener()

        return binding.root
    }

    private fun initUI() {

        binding.playTvPlayer1Name.text = "Round score"
        binding.playTvPlayer1Score.text = roundScore.toString()
        binding.playTvPlayer2Name.text = "Total score"

        db!!.updatePlayerReady(
            session,
            currentPlayer,
            Utils.SESSION_RDY_NO
        )
        initListenerButton()
        launchCountDown()
        binding.playTvCalculation.text = session!!.calculationList[COUNTER].calculText
    }

    private fun initListener() {
        db!!.setOnSessionUpdateListener { session_ ->
            session = session_
            for (player in session_.playerList) {
                if (player.id == currentPlayer!!.id) {
                    totalScore = player.score
                    binding.playTvPlayer2Score.text = totalScore.toString()
                }
            }
        }
        binding.playEtResult.addTextChangedListener(object : TextWatcher {
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
                if (resultTextPlayer == session!!.calculationList[COUNTER].result
                        .toString()
                ) {
                    //Player found the correct result
                    COUNTER++
                    binding.playTvCalculation.text = session!!.calculationList[COUNTER].calculText
                    binding.playEtResult.setText("")
                    totalScore++
                    roundScore++
                    db!!.updateScoreCurrentPlayerInSession(session, currentPlayer, totalScore)
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
                countDownTimer!!.cancel()
                db!!.updatePlayerReady(
                    session,
                    currentPlayer,
                    Utils.SESSION_RDY_YES
                )
                launchLoadingFragment()
            }
        }.start()
    }

    private fun initListenerButton() {

        binding.playBtn0.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 0
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn1.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 1
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn2.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 2
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn3.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 3
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn4.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 4
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn5.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 5
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn6.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 6
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn7.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 7
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn8.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 8
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtn9.setOnClickListener {
            val text = binding.playEtResult.text.toString() + 9
            if (checkLengthText(text)) binding.playEtResult.setText(text)
        }
        binding.playBtnMinus.setOnClickListener {
            if (!binding.playEtResult.text.toString().contains("-")) {
                val text = "-" + binding.playEtResult.text.toString()
                if (checkLengthText(text)) binding.playEtResult.setText(text)
            }
        }
        binding.playBtnBackspace.setOnClickListener {
            if (binding.playEtResult.text.toString().isNotEmpty()) {
                val text = removeLastChar(binding.playEtResult.text.toString())
                if (checkLengthText(text)) binding.playEtResult.setText(text)
            }
        }
    }

    private fun checkLengthText(text: String): Boolean {
        var success = false
        if (text.length <= Utils.MAX_LENGTH_RESULT) success =
            true
        return success
    }

    private fun removeLastChar(str: String): String {
        return str.substring(0, str.length - 1)
    }

    private fun launchLoadingFragment() {
        for (player in session!!.playerList) {
            if (player.new_ == Utils.SESSION_CREATOR) {
                if (player.id == currentPlayer!!.id) {
                    db!!.updateCalculationListInSession(session, generateCalculationList())
                }
            }
        }
        db!!.removeListenerCurrentSession(session)
        val lf = LoadingFragment()
        val args = Bundle()
        args.putParcelable("session", session)
        args.putParcelable("currentPlayer", currentPlayer)
        lf.arguments = args
        val fm = fragmentManager
        val ft = fm!!.beginTransaction()
        ft.replace(R.id.fl_session, lf)
        ft.commit()
    }

    private fun generateCalculationList(): List<Calculation> {
        val calculationList_: MutableList<Calculation> =
            ArrayList()
        for (i in 0..49) {
            calculationList_.add(Calculation())
        }
        return calculationList_
    }
}