package com.example.mlallemant.mentalbattle.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by m.lallemant on 13/10/2017.
 */
@Parcelize
class Player(
    var id: String? = null,
    var name: String? = null,
    var score: Int? = null,
    var nb_win: Int? = null,
    var nb_lose: Int? = null,
    var xp: Int? = null,
    var ready: String? = null,
    var new_: String? = null,
) : Parcelable