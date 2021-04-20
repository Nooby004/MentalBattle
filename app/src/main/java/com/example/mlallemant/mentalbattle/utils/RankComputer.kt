package com.example.mlallemant.mentalbattle.utils

import android.content.Context
import com.example.mlallemant.mentalbattle.R
import kotlin.math.roundToInt
import kotlin.math.sqrt

class RankComputer {

    fun getLevelByXp(xp: Int) = (sqrt((100 * (2 * xp + 25) + 50).toDouble()) / 100).roundToInt()

    fun getRangeLevelByLevel(level: Int): IntArray {
        val range = IntArray(2)
        range[0] = (level * level + level) / 2 * 100 - level * 100
        range[1] = ((level + 1) * (level + 1) + (level + 1)) / 2 * 100 - (level + 1) * 100
        return range
    }

    fun getRankByLevel(level: Int, context: Context): String {
        return when (level) {
            in 1..5 -> context.getString(R.string.rank_brainless)
            in 6..10 -> context.getString(R.string.rank_little_head)
            in 11..20 -> context.getString(R.string.rank_genius)
            in 21..35 -> context.getString(R.string.rank_brain_master)
            in 36..60 -> context.getString(R.string.rank_super_calculator)
            in 61..100 -> context.getString(R.string.rank_god)
            in 100..Float.POSITIVE_INFINITY.toInt() -> context.getString(R.string.rank_chuck_norris)
            else -> context.getString(R.string.rank_chuck_norris)
        }
    }

    fun getNextRankByLevel(level: Int, context: Context): String {
        return when (level) {
            in 1..5 -> context.getString(R.string.next_rank_little_head)
            in 6..10 -> context.getString(R.string.next_rank_genius)
            in 11..20 -> context.getString(R.string.next_rank_brain_master)
            in 21..35 -> context.getString(R.string.next_rank_super_calculator)
            in 36..60 -> context.getString(R.string.next_rank_god)
            in 61..100 -> context.getString(R.string.next_rank_chuck_norris)
            else -> context.getString(R.string.next_rank_unknown)
        }
    }
}