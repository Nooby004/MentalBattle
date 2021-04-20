package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.SessionPlayerRowItemTemplateBinding
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import com.example.mlallemant.mentalbattle.utils.Session
import com.example.mlallemant.mentalbattle.utils.Utils

/**
 * Created by m.lallemant on 16/11/2017.
 */
class PlayersAdapter(
    dataSet: List<Player>,
    private val session: Session?,
    private val isCreator: Boolean,
    var mContext: Context
) : ArrayAdapter<Player?>(mContext, R.layout.session_player_row_item_template, dataSet) {
    private val db: DatabaseManager = DatabaseManager.getInstance()

    lateinit var binding: SessionPlayerRowItemTemplateBinding

    private var lastPosition = -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val convertedView: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            viewHolder = ViewHolder()
            binding = SessionPlayerRowItemTemplateBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
            convertedView = binding.root
            convertedView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            convertedView = convertView
        }
        lastPosition = position
        viewHolder.bind(getItem(position))
        return convertedView
    }

    inner class ViewHolder {

        fun bind(playerModel: Player?) {
            with(binding) {
                playerModel?.let {
                    //USERNAME
                    if (it.name != null) {
                        sessionRowTvUsername.apply {
                            text = it.name
                            if (it.ready == Utils.SESSION_RDY_YES) {
                                setTextColor(ContextCompat.getColor(mContext, R.color.greenColor))
                            } else {
                                setTextColor(ContextCompat.getColor(mContext, R.color.redColor))
                            }
                        }
                    }

                    //DELETE
                    if (isCreator) {
                        if (it.new_ == Utils.SESSION_CREATOR) {
                            sessionRowIvDelete.visibility = View.INVISIBLE
                        } else {
                            sessionRowIvDelete.visibility = View.VISIBLE
                            sessionRowIvDelete.setOnClickListener {
                                db.removePlayerInSession(session, playerModel)
                            }
                        }
                    } else {
                        sessionRowIvDelete.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

}