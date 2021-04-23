package com.example.mlallemant.mentalbattle.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by m.lallemant on 15/11/2017.
 */
@Parcelize
class Session(
    var name: String? = null,
    var password: String? = null,
    var state: String? = null,
    var calculationList: List<Calculation?>? = generateCalculationList(),
    var playerList: MutableList<Player?>? = null
) : Parcelable {

    constructor(
        name: String,
        password: String,
        state: String,
        calculationList: MutableList<Calculation>
    ) : this() {
        this.name = name
        this.password = password
        this.state = state
        this.calculationList = calculationList
    }

    fun addPlayerToSession(player: Player?) {
        playerList!!.add(player)
    }

    val idSession: String
        get() = name + password

    companion object {
        private const val NB_CALCULATION = 50

        @JvmStatic
        private fun generateCalculationList(): List<Calculation?> {
            val calculationList: MutableList<Calculation?> =
                ArrayList()
            for (i in 0 until NB_CALCULATION) {
                calculationList.add(Calculation())
            }
            return calculationList
        }
    }
}