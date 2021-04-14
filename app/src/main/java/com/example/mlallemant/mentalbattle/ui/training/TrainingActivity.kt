package com.example.mlallemant.mentalbattle.ui.training

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.databinding.TrainingActivityBinding
import com.example.mlallemant.mentalbattle.utils.Calculation
import com.example.mlallemant.mentalbattle.utils.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.training_activity.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by m.lallemant on 22/11/2017.
 */
class TrainingActivity : AppCompatActivity() {

    private var _binding: TrainingActivityBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    //Utils
    private var calculations: MutableList<AnimatedCalculation>? = null

    private var timeBetweenEachCalcul: Long = 10000

    private var score = 0
    private var mHandler: Handler? = null
    private var mRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = TrainingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        calculations = ArrayList()
        calculAndDisplaySpeed()
        initListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun initListener() {
        binding.trainingBtnStart.setOnClickListener {
            binding.trainingBtnStart.visibility = View.INVISIBLE
            launchTraining()
        }
        initListenerButton()
        binding.trainingEtResult.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val value = editable.toString()
                checkResultOk(value)
            }
        })
        binding.trainingSpeedUp.setOnClickListener {
            timeBetweenEachCalcul -= 100
            calculAndDisplaySpeed()
        }
        binding.trainingSpeedDown.setOnClickListener {
            timeBetweenEachCalcul += 100
            calculAndDisplaySpeed()
        }
    }

    private fun checkResultOk(value: String) {
        for (calculation in calculations!!) {
            if (calculation.calculation.result.toString() == value) {
                calculation.remove()
                binding.trainingEtResult.setText("")
                calculations!!.remove(calculation)
                score++
                binding.trainingTvScore.text = "" + score
                break
            }
        }
    }

    private fun removeAllAnimation() {
        for (calculation in calculations!!) {
            calculation.remove()
        }
        calculations!!.clear()
    }

    private fun launchTraining() {
        compositeDisposable.add(
        Observable.timer(100, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
                generateNewCalcul()
                timeBetweenEachCalcul -= 15
                calculAndDisplaySpeed()
            })
    }

    private fun calculAndDisplaySpeed() {
        val speed = 1 / java.lang.Long.valueOf(timeBetweenEachCalcul).toFloat() * 1000
        val text = roundFloat(speed, 3).toString() + "/s"
        binding.trainingTvSpeed.text = text
    }

    private fun generateNewCalcul() {
        val rl = RelativeLayout(this)
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rl.layoutParams = layoutParams
        val tv = TextView(this)
        val calculation = Calculation()
        tv.text = calculation.calculText
        tv.textSize = getRandomIntBetween(20, 30).toFloat()
        rl.addView(tv)
        binding.trainingRlParent.addView(rl, 0)
        val x = binding.trainingRlParent.width
        val y = binding.trainingRlParent.height
        val translateXAnimation = ObjectAnimator.ofFloat(
            rl,
            "translationX",
            getRandomIntBetween(0, java.lang.Double.valueOf(0.8 * x).toInt()).toFloat(),
            getRandomIntBetween(0, java.lang.Double.valueOf(0.8 * x).toInt()).toFloat()
        )
        val translateYAnimation = ObjectAnimator.ofFloat(rl, "translationY", -100f, y.toFloat())
        val set = AnimatorSet()
        set.duration = TIME_TO_FALL
        set.playTogether(translateXAnimation, translateYAnimation)
        set.start()
        translateYAnimation.addUpdateListener { valueAnimator ->
            val yPosition = valueAnimator.animatedValue as Float
            if (java.lang.Float.valueOf(yPosition).toInt() > y - 50) {
                removeAllAnimation()
                timeBetweenEachCalcul = 10000
                mHandler!!.removeCallbacks(mRunnable!!)
                score = 0
                binding.trainingBtnStart.visibility = View.VISIBLE
            }
        }
        calculations!!.add(AnimatedCalculation(rl, calculation, set))
    }

    private fun getRandomIntBetween(x1: Int, x2: Int): Int {
        val r = Random()
        return r.nextInt(x2 - x1) + x1
    }

    private inner class AnimatedCalculation(
        private val rl: RelativeLayout,
        val calculation: Calculation,
        private val animatorSet: AnimatorSet
    ) {
        fun remove() {
            animatorSet.cancel()
            rl.clearAnimation()
            binding.trainingRlParent.removeView(rl)
        }
    }

    private fun initListenerButton() {
        with(binding) {
            training_btn_0.setOnClickListener {
                val text = trainingEtResult.text.toString() + 0
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_1.setOnClickListener {
                val text = trainingEtResult.text.toString() + 1
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_2.setOnClickListener {
                val text = trainingEtResult.text.toString() + 2
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_3.setOnClickListener {
                val text = trainingEtResult.text.toString() + 3
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_4.setOnClickListener {
                val text = trainingEtResult.text.toString() + 4
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_5.setOnClickListener {
                val text = trainingEtResult.text.toString() + 5
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_6.setOnClickListener {
                val text = trainingEtResult.text.toString() + 6
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_7.setOnClickListener {
                val text = trainingEtResult.text.toString() + 7
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_8.setOnClickListener {
                val text = trainingEtResult.text.toString() + 8
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_9.setOnClickListener {
                val text = trainingEtResult.text.toString() + 9
                if (checkLengthText(text)) trainingEtResult.setText(text)
            }
            training_btn_minus.setOnClickListener {
                if (!trainingEtResult.text.toString().contains("-")) {
                    val text = "-" + trainingEtResult.text.toString()
                    if (checkLengthText(text)) trainingEtResult.setText(text)
                }
            }
            training_btn_backspace.setOnClickListener {
                if (trainingEtResult.text.toString().isNotEmpty()) {
                    val text = removeLastChar(trainingEtResult.text.toString())
                    if (checkLengthText(text)) trainingEtResult.setText(text)
                }
            }
        }
    }

    private fun checkLengthText(text: String): Boolean {
        var success = false
        if (text.length <= Utils.MAX_LENGTH_RESULT) success = true
        return success
    }

    private fun removeLastChar(str: String): String {
        return str.substring(0, str.length - 1)
    }

    companion object {

        private const val TIME_TO_FALL: Long = 50000

        fun roundFloat(number: Float, scale: Int): Float {
            var pow = 10
            for (i in 1 until scale) pow *= 10
            val tmp = number * pow
            return ((if (tmp - tmp.toInt() >= 0.5f) tmp + 1 else tmp).toInt()).toFloat() / pow
        }
    }
}