package com.example.mlallemant.mentalbattle.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mlallemant.mentalbattle.databinding.SplashActivityBinding
import com.example.mlallemant.mentalbattle.ui.login.LoginActivity
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

    companion object {
        const val SPLASH_TIME_OUT = 1000L
    }
}