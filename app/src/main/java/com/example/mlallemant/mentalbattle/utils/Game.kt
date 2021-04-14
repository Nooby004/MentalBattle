package com.example.mlallemant.mentalbattle.utils

import kotlin.collections.MutableList

/**
 * Created by m.lallemant on 13/10/2017.
 */
class Game(
    var id: String? = null,
    var player1: Player? = null,
    var player2: Player? = null,
    var calculationList: List<Calculation>? = generateCalculationList()
) {

    companion object {
        private const val NB_CALCULATION = 50

        @JvmStatic
        fun generateCalculationList(): List<Calculation> {
            val calculationList: MutableList<Calculation> = ArrayList()
            for (i in 0..NB_CALCULATION) {
                calculationList.add(Calculation())
            }
            return calculationList
        }
    }
}
