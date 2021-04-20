package com.example.mlallemant.mentalbattle.ui.game.fragment

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.databinding.PlayerFindFragmentBinding
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_CURRENT_PLAYER
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_OTHER_PLAYER
import com.example.mlallemant.mentalbattle.utils.Utils

/**
 * Created by m.lallemant on 18/10/2017.
 */
class PlayerFindFragment : Fragment() {

    private var _binding: PlayerFindFragmentBinding? = null
    private val binding get() = _binding!!

    var mCallBack: OnCountdownFinish? = null

    interface OnCountdownFinish {
        fun launchGame()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
       _binding = PlayerFindFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentPlayerName = arguments?.getString(BUNDLE_EXTRA_CURRENT_PLAYER)
        val otherPlayerName = arguments?.getString(BUNDLE_EXTRA_OTHER_PLAYER)
        binding.tvPlayer1.text = currentPlayerName!!.split(" ".toRegex()).toTypedArray()[0]
        binding.tvPlayer2.text = otherPlayerName!!.split(" ".toRegex()).toTypedArray()[0]
        launchCountDown()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBack = if (context is OnCountdownFinish) {
            context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun launchCountDown() {
        object : CountDownTimer(Utils.COUNTDOWN_LOBBY.toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val remainingTime = "" + millisUntilFinished / 1000
                binding.tvCountdown.text = remainingTime
            }

            override fun onFinish() {
                mCallBack?.launchGame()
            }
        }.start()
    }
}