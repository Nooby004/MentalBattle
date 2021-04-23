package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuFriendsFragmentBinding
import com.example.mlallemant.mentalbattle.ui.extention.toast
import com.example.mlallemant.mentalbattle.ui.friends.FriendAdapter
import com.example.mlallemant.mentalbattle.ui.friends.FriendModel
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity.FriendListToFragment
import com.example.mlallemant.mentalbattle.utils.*
import com.example.mlallemant.mentalbattle.utils.CustomDialog.OnClickBtnListener
import com.example.mlallemant.mentalbattle.utils.Game.Companion.generateCalculationList
import com.facebook.FacebookSdk

/**
 * Created by m.lallemant on 10/11/2017.
 */
class FriendsFragment : Fragment() {

    private var _binding: MenuFriendsFragmentBinding? = null
    private val binding get() = _binding!!

    //Utils
    private lateinit var db: DatabaseManager
    private var adapter: FriendAdapter? = null
    private var currentPlayer: Player? = null
    private var currentGame: Game? = null
    private var cdAskToPlay: CustomDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuFriendsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuActivity = activity as MenuActivity?
        currentPlayer = menuActivity?.currentPlayer
        db = DatabaseManager.getInstance()
        initUI()
        initListener()
    }

    private fun initUI() {
        with(binding) {
            adapter = FriendAdapter(ArrayList(), FacebookSdk.getApplicationContext())
            selectFriendsListView.adapter = adapter
            adapter?.clear()
            adapter?.addAll(db.friendList)
        }
    }

    private fun initListener() {
        binding.selectFriendsIvBack.setOnClickListener { returnSelectorFragment() }
        (activity as? MenuActivity)?.setFriendListToFragment(object : FriendListToFragment {
            override fun sendData(friendList: List<FriendModel>?) {
                friendList?.let { fl ->
                    adapter?.clear()
                    adapter?.addAll(fl)
                    for (i in fl.indices) {
                        val friend = fl[i]
                        if (friend.playReq == Utils.PLAY_CANCEL) {
                            cdAskToPlay?.let {
                                if (it.isShowing() == true) {
                                    it.dismiss()
                                    db.resetToPlayWith(currentPlayer, friend.player)
                                }
                            }
                        }
                    }
                }
            }
        })
        binding.selectFriendsListView.onItemLongClickListener =
            OnItemLongClickListener { _, _, i, _ ->
                val friend = adapter?.getItem(i)
                val cdConfirmDelete = CustomDialog(
                    requireContext(), friend?.player?.id,
                    getString(R.string.delete_friend_dialog_message),
                    getString(R.string.btn_yes), R.color.greenColor,
                    getString(R.string.btn_no), R.color.redColor
                )
                cdConfirmDelete.create()
                cdConfirmDelete.setOnClickBtnListener(object : OnClickBtnListener {
                    override fun onClickBtn1() {
                        db.deleteFriend(currentPlayer, friend?.player)
                        cdConfirmDelete.dismiss()
                    }

                    override fun onClickBtn2() {
                        cdConfirmDelete.dismiss()
                    }
                })
                false
            }
        binding.selectFriendsListView.onItemClickListener = OnItemClickListener { _, _, i, _ ->
            val friend = adapter?.getItem(i)
            if (friend?.friendAcq == Utils.ACK_REQUEST_RECEIVED) {
                // if not ack, open dialog in order to accept him
                val cdAcceptFriend = CustomDialog(
                    requireContext(), friend.player?.id,
                    "Do you want to accept this friend ?",
                    "YES", R.color.greenColor,
                    "NO", R.color.redColor
                )
                cdAcceptFriend.create()
                cdAcceptFriend.setOnClickBtnListener(object : OnClickBtnListener {
                    override fun onClickBtn1() {
                        db.ackFriend(currentPlayer, friend.player)
                        cdAcceptFriend.dismiss()
                    }

                    override fun onClickBtn2() {
                        db.deleteFriend(currentPlayer, friend.player)
                        cdAcceptFriend.dismiss()
                    }
                })
            } else if (friend?.friendAcq == Utils.ACK_REQUEST_SENT) {
                toast(getString(R.string.friend_request_sent))
            } else {
                //REQUEST FRIEND TO PLAY
                if (friend?.connected == true) {
                    db.askToPlayWith(currentPlayer, friend.player)
                    val idGame = currentPlayer?.id + friend.player?.id
                    val game = Game(idGame, currentPlayer, friend.player, generateCalculationList())
                    currentGame = game
                    db.insertAvailableGame(currentGame)
                    db.getAvailableGameById(idGame)
                    cdAskToPlay = CustomDialog(
                        requireContext(), friend.player?.id,
                        "Waiting " + friend.player?.name + " ...",
                        "CANCEL", R.color.redColor,
                        null, 0
                    )
                    cdAskToPlay?.create()
                    cdAskToPlay?.setOnClickBtnListener(object : OnClickBtnListener {
                        override fun onClickBtn1() {
                            db.declineToPlayWith(currentPlayer, friend.player)
                            db.getAvailableGameById(idGame)
                            db.deleteAvailableGame(currentGame)
                            currentGame = null
                            cdAskToPlay!!.dismiss()
                        }

                        override fun onClickBtn2() {}
                    })
                } else {
                    toast(getString(R.string.friend_not_connected))
                }
            }
        }
        binding.selectFriendsIvAdd.setOnClickListener { launchSearchFragment() }
    }

    private fun returnSelectorFragment() {
        val selectorFragment = SelectorFragment()
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, selectorFragment)
            ?.commit()
    }

    private fun launchSearchFragment() {
        val searchFriendFragment = SearchFriendFragment()
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, searchFriendFragment)
            ?.commit()
    }
}