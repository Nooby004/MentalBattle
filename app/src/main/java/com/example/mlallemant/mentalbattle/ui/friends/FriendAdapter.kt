package com.example.mlallemant.mentalbattle.ui.friends

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.utils.RankComputer
import com.example.mlallemant.mentalbattle.utils.Utils
import com.example.mlallemant.mentalbattle.utils.Utils.ONE_MEGABYTE
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

/**
 * Created by m.lallemant on 30/10/2017.
 */
class FriendAdapter(
    dataSet: ArrayList<FriendModel?>,
    mContext: Context
    ) : ArrayAdapter<FriendModel?>(
        mContext, R.layout.friend_row_item_template, dataSet
    ) {

    private class ViewHolder {
        var username: TextView? = null
        var level: TextView? = null
        var rank: TextView? = null
        var profilePicture: CircleImageView? = null
        var connect: CircleImageView? = null
    }

    private var lastPosition = -1
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        val convertedView: View
        val friendModel = getItem(position)

        //Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder
        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertedView = inflater.inflate(R.layout.friend_row_item_template, parent, false)
            viewHolder.username =
                convertedView.findViewById<View>(R.id.row_item_tv_username) as TextView
            viewHolder.connect =
                convertedView.findViewById<View>(R.id.row_item_iv_connect) as CircleImageView
            viewHolder.rank = convertedView.findViewById<View>(R.id.row_item_rank) as TextView
            viewHolder.level = convertedView.findViewById<View>(R.id.row_item_tv_level) as TextView
            viewHolder.profilePicture =
                convertedView.findViewById<View>(R.id.row_item_profile_picture) as CircleImageView
            convertedView.tag = viewHolder
        } else {
            convertedView = convertView
            viewHolder = convertedView.tag as ViewHolder
        }
        lastPosition = position
        if (friendModel != null) {

            //USERNAME
            if (friendModel.player != null) {
                val splitName = friendModel.player?.name?.split(" ")?.toTypedArray()?.get(0)
                viewHolder.username!!.text = splitName
            }

            //CONNECTED/DISCONNECTED
            if (friendModel.friendAcq != null) {
                if (friendModel.friendAcq == Utils.ACK_OK) {
                    if (friendModel.connected!!) {
                        viewHolder.connect!!.setImageResource(R.color.greenColor)
                    } else {
                        viewHolder.connect!!.setImageResource(R.color.redColor)
                        //gd.setColor(ContextCompat.getColor(mContext, R.color.redColor));
                    }
                } else {
                    viewHolder.connect!!.setImageResource(R.color.whiteGrayColor)
                }
            }

            //LEVEL / RANK
            if (friendModel.xp != null) {
                val level = RankComputer().getLevelByXp(friendModel.xp!!.toInt())
                val text = "LVL $level"
                viewHolder.level!!.text = text
                viewHolder.rank!!.text = RankComputer().getRankByLevel(level, context)
            }


            //PROFILE PICTURE
            if (friendModel.profilePicture == null) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val text = "profilePictures/" + friendModel.player!!.id + ".png"
                val imagesRef = storageRef.child(text)
                imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
                    val bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    viewHolder.profilePicture!!.setImageBitmap(bm)
                }
            }
        }
        return convertedView
    }
}