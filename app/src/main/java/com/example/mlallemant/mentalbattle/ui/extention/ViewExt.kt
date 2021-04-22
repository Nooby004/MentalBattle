package com.example.mlallemant.mentalbattle.ui.extention

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun AppCompatActivity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, message, duration).show()
}

fun ImageView.setImage(idImage: Int, context: Context) {
    setImageDrawable(ContextCompat.getDrawable(context, idImage))
}

fun View.changeVisibility(isVisible: Boolean) {
    this.visibility = when(isVisible) {
        true -> View.VISIBLE
        else -> View.GONE
    }
}