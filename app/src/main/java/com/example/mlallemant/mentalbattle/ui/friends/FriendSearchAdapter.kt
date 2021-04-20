package com.example.mlallemant.mentalbattle.ui.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mlallemant.mentalbattle.R
import java.util.*

/**
 * Created by m.lallemant on 13/11/2017.
 */
class FriendSearchAdapter(
    dataSet: ArrayList<FriendSearchModel?>,
    mContext: Context
) : ArrayAdapter<FriendSearchModel?>(
    mContext, R.layout.friend_search_row_item_template, dataSet
), View.OnClickListener {

    private class ViewHolder {
        var username: TextView? = null
    }

    override fun onClick(v: View) {
        val position = v.tag as Int
        val `object`: Any? = getItem(position)
        val friendSearchModel = `object` as FriendSearchModel?
    }

    private var lastPosition = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        val convertedView: View
        val friendSearchModel = getItem(position)

        //Check if an existing view is being reused, otherwise inflate the view
        val viewHolder: ViewHolder
        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertedView = inflater.inflate(R.layout.friend_search_row_item_template, parent, false)
            viewHolder.username = convertedView.findViewById<View>(R.id.friend_search_tv_username) as TextView
            convertedView.tag = viewHolder
        } else {
            convertedView = convertView
            viewHolder = convertedView.tag as ViewHolder
        }
        lastPosition = position
        if (friendSearchModel != null) {
            viewHolder.username!!.text = friendSearchModel.player.name
        }
        return convertedView
    }
}