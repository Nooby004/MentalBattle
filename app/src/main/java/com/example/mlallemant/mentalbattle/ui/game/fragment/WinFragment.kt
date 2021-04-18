package com.example.mlallemant.mentalbattle.ui.game.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.WinFragmentBinding
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_LOOSER_NAME
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_LOOSER_SCORE
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_RESULT_GAME
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_WINNER_NAME
import com.example.mlallemant.mentalbattle.ui.game.GameActivity.Companion.BUNDLE_EXTRA_WINNER_SCORE

/**
 * Created by m.lallemant on 20/10/2017.
 */
class WinFragment : Fragment() {

    private var _binding: WinFragmentBinding? = null
    private val binding get() = _binding!!

    var mCallBack: OnNextGame? = null

    interface OnNextGame {
        fun launchNextGame()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = WinFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val winnerName = it.getString(BUNDLE_EXTRA_WINNER_NAME)
            val looserName = it.getString(BUNDLE_EXTRA_LOOSER_NAME)
            val winnerScore = it.getString(BUNDLE_EXTRA_WINNER_SCORE)
            val looserScore = it.getString(BUNDLE_EXTRA_LOOSER_SCORE)
            val resultGame = it.getString(BUNDLE_EXTRA_RESULT_GAME)

            binding.winTvWinnerPlayer.text = extractPlayerResult(winnerName, winnerScore)
            binding.winTvLooserPlayer.text = extractPlayerResult(looserName, looserScore)
            with(binding.winTvWinLose) {
                if (resultGame == getString(R.string.result_you_loose)) {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.whiteGrayColor))
                } else {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.orangeColor))
                }
                text = resultGame
            }
        }
        binding.winBtnNext.setOnClickListener { mCallBack!!.launchNextGame() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBack = if (context is OnNextGame) {
            context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun extractPlayerResult(name: String?, score: String?) =
        when (name != null && score != null) {
            true -> name.split(" ".toRegex()).toTypedArray()[0] + " - " + score
            else -> ""
    }
}