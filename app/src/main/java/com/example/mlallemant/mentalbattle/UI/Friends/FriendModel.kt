package com.example.mlallemant.mentalbattle.UI.Friends

import android.graphics.Bitmap
import com.example.mlallemant.mentalbattle.Utils.Player

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