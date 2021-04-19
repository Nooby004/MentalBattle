package com.example.mlallemant.mentalbattle.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.MentalBattleApplication.Companion.REMOTE_CONFIG_SIGNIN_WITH_FACEBOOK
import com.example.mlallemant.mentalbattle.MentalBattleApplication.Companion.REMOTE_CONFIG_SIGNIN_WITH_GOOGLE
import com.example.mlallemant.mentalbattle.MentalBattleApplication.Companion.REMOTE_CONFIG_TEST_VALUE
import com.example.mlallemant.mentalbattle.databinding.SplashActivityBinding
import com.example.mlallemant.mentalbattle.ui.login.LoginActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class SplashActivity : AppCompatActivity() {

    private var _binding: SplashActivityBinding? = null
    private val binding get() = _binding!!
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkRemoteConfig()

        compositeDisposable.add(Observable.timer(SPLASH_TIME_OUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        compositeDisposable.dispose()
    }

    private fun checkRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val testValue = remoteConfig.getString(REMOTE_CONFIG_TEST_VALUE)
        val signinWithGoogle = remoteConfig.getBoolean(REMOTE_CONFIG_SIGNIN_WITH_GOOGLE)
        val signinWithFacebook = remoteConfig.getBoolean(REMOTE_CONFIG_SIGNIN_WITH_FACEBOOK)

        Log.i(javaClass.simpleName, REMOTE_CONFIG_TEST_VALUE + testValue)
        Log.i(javaClass.simpleName, REMOTE_CONFIG_SIGNIN_WITH_GOOGLE + signinWithGoogle)
        Log.i(javaClass.simpleName, REMOTE_CONFIG_SIGNIN_WITH_FACEBOOK + signinWithFacebook)

    }

    companion object {
        const val SPLASH_TIME_OUT = 1000L
    }
}