package com.example.mlallemant.mentalbattle.ui.friends

import android.graphics.Bitmap
import com.example.mlallemant.mentalbattle.utils.Player

/**
 * Created by m.lallemant on 30/10/2017.
 */
class FriendModel(
    var player: Player? = null,
    var connected: Boolean? = null,
    var friendAcq: String? = null,
    var playReq: String? = null,
    var xp: String? = null,
    var profilePicture: Bitmap? = null
)