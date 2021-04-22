package com.example.mlallemant.mentalbattle.utils

import android.widget.EditText

class GameUtils {

    companion object {
        fun updateEditTextResult(addedValue: Int, editText: EditText) {
            with(editText) {
                val text = text.toString() + addedValue
                if (checkLengthText(text)) setText(text)
            }
        }

        fun updateButtonMinusResult(editText: EditText) {
            if (!editText.text.toString().contains("-")) {
                val text = "-" + editText.text.toString()
                if (checkLengthText(text)) editText.setText(text)
            }
        }

        fun updateButtonBackspaceResult(editText: EditText) {
            if (editText.text.toString().isNotEmpty()) {
                val text = removeLastChar(editText.text.toString())
                if (checkLengthText(text)) editText.setText(text)
            }
        }

        fun checkLengthText(text: String): Boolean {
            var success = false
            if (text.length <= Utils.MAX_LENGTH_RESULT) success = true
            return success
        }

        fun removeLastChar(str: String): String {
            return str.substring(0, str.length - 1)
        }
    }
}