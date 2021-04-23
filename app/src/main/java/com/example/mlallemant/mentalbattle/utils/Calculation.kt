package com.example.mlallemant.mentalbattle.utils

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by m.lallemant on 19/10/2017.
 */
class Calculation : Parcelable {
    var result: Int?
        private set
    val calculText: String?

    constructor() {
        var A: Int
        var B: Int
        A = randomNumber
        B = randomNumber
        result = calculResult(A, B)
        while (result!! > MAX_VALUE_ADDITION && result!! < -MAX_VALUE_ADDITION) {
            A = randomNumber
            B = randomNumber
            result = calculResult(A, B)
        }
        calculText = makeString(A, B)
    }

    private val randomNumber: Int
        private get() {
            val a: Int
            val random = Random()
            a =
                random.nextInt(MAX_RANGE + 1 + MAX_RANGE) - MAX_RANGE
            return a
        }

    private fun calculResult(a: Int, b: Int): Int {
        return a + b
    }

    private fun makeString(a: Int, b: Int): String {
        var b = b
        val text: String
        val sign: String
        if (b < 0) {
            sign = "-"
            b *= -1
        } else {
            sign = "+"
        }
        text = "$a $sign $b"
        return text
    }

    protected constructor(`in`: Parcel) {
        result = if (`in`.readByte().toInt() == 0x00) null else `in`.readInt()
        calculText = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (result == null) {
            dest.writeByte(0x00.toByte())
        } else {
            dest.writeByte(0x01.toByte())
            dest.writeInt(result!!)
        }
        dest.writeString(calculText)
    }

    companion object CREATOR : Parcelable.Creator<Calculation> {
        private const val MAX_VALUE_ADDITION = 150
        private const val MAX_RANGE = 100

        override fun createFromParcel(parcel: Parcel): Calculation {
            return Calculation(parcel)
        }

        override fun newArray(size: Int): Array<Calculation?> {
            return arrayOfNulls(size)
        }
    }
}