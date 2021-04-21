package com.example.mlallemant.mentalbattle.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.utils.Utils.ONE_MEGABYTE
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

/**
 * Created by m.lallemant on 10/11/2017.
 */
class CustomDialog(
    private val context: Context,
    private var idUser: String?,
    private val message: String,
    private val btnText1: String,
    private val btnColor1: Int,
    private val btnText2: String?,
    private val btnColor2: Int
) {

    private var onClickBtnListener: OnClickBtnListener? = null

    interface OnClickBtnListener {
        fun onClickBtn1()
        fun onClickBtn2()
    }

    private var bm: Bitmap? = null
    private var dialog: Dialog? = null

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun setOnClickBtnListener(listener: OnClickBtnListener?) {
        onClickBtnListener = listener
    }

    fun create() {
        dialog = Dialog(context).apply {
            setContentView(R.layout.menu_dialog_template)
            setCancelable(false)
        }

        (dialog?.findViewById(R.id.dialog_message) as? TextView)?.text = this.message
        (dialog?.findViewById(R.id.dialog_btn1) as? TextView)?.let {
            it.text = btnText1
            it.setTextColor(ContextCompat.getColor(context, btnColor1))
            it.setOnClickListener { onClickBtnListener?.onClickBtn1() }
        }

        (dialog?.findViewById(R.id.dialog_btn2) as? TextView)?.let {
            if (btnText2 != null) {
                it.text = btnText2
                it.setTextColor(ContextCompat.getColor(context, btnColor2))
            } else {
                it.visibility = View.GONE
            }
            it.setOnClickListener { onClickBtnListener?.onClickBtn2() }
        }

        val profileUser = dialog?.findViewById<View>(R.id.dialog_profile_user) as CircleImageView
        val imagesRef = storage.reference.child("profilePictures/$idUser.png")
        if (bm == null) {
            imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
                val bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                profileUser.setImageBitmap(bm)
            }
        }

        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog?.window?.attributes)
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        dialog?.show()
        dialog?.window?.attributes = layoutParams
    }

    fun dismiss() {
        dialog?.dismiss()
    }

    fun isShowing() = dialog?.isShowing

}