package com.example.mlallemant.mentalbattle.ui.session.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.utils.Player
import java.util.*

/**
 * Created by m.lallemant on 17/11/2017.
 */
class RankingPlayerAdapter(
    data: ArrayList<Player?>?,
    private val mContext: Context
) : ArrayAdapter<Player?>(mContext, R.layout.session_transition_row_item_template, data!!) {

    private class ViewHolder {
        var tvPosition: TextView? = null
        var tvUsername: TextView? = null
        var tvScore: TextView? = null
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val rankingPlayerModel = getItem(position)
        val viewHolder: ViewHolder
        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            convertView =
                inflater.inflate(R.layout.session_transition_row_item_template, parent, false)
            viewHolder.tvPosition =
                convertView.findViewById<View>(R.id.session_transition_row_tv_position) as TextView
            viewHolder.tvUsername =
                convertView.findViewById<View>(R.id.session_transition_row_tv_username) as TextView
            viewHolder.tvScore =
                convertView.findViewById<View>(R.id.session_transition_row_tv_score) as TextView
            convertView.tag = viewHolder
        } else {
            viewHolder =
                convertView.tag as ViewHolder
        }
        if (rankingPlayerModel != null) {

            //POSITION
            viewHolder.tvPosition!!.text = (position + 1).toString()
            if (position == 0) {
                viewHolder.tvPosition!!.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.greenColor
                    )
                )
                viewHolder.tvUsername!!.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.greenColor
                    )
                )
                viewHolder.tvScore!!.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.greenColor
                    )
                )
            }
            if (rankingPlayerModel.name != null) {
                viewHolder.tvUsername!!.text = rankingPlayerModel.name
            }
            if (rankingPlayerModel.score != null) {
                viewHolder.tvScore!!.text = rankingPlayerModel.score.toString()
            }
        }
        return convertView!!
    }

}