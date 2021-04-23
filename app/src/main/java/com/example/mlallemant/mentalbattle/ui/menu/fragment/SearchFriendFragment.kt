package com.example.mlallemant.mentalbattle.ui.menu.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.mlallemant.mentalbattle.R
import com.example.mlallemant.mentalbattle.databinding.MenuFriendsFragmentBinding
import com.example.mlallemant.mentalbattle.ui.friends.FriendSearchAdapter
import com.example.mlallemant.mentalbattle.ui.friends.FriendSearchModel
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity
import com.example.mlallemant.mentalbattle.utils.DatabaseManager
import com.example.mlallemant.mentalbattle.utils.Player
import java.util.*

/**
 * Created by m.lallemant on 13/11/2017.
 */
class SearchFriendFragment : Fragment() {

    private var _binding: MenuFriendsFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchView: SearchView

    //Utils
    private var currentPlayer: Player? = null
    private lateinit var db: DatabaseManager
    private var adapter: FriendSearchAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuFriendsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView = view.findViewById(R.id.select_find_sv_friend)
        currentPlayer = (activity as? MenuActivity)?.currentPlayer
        db = DatabaseManager.getInstance()
        initUI()
        initListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initUI() {
        adapter = FriendSearchAdapter(ArrayList(), activity!!)
        binding.selectFriendsListView.adapter = adapter
        adapter?.clear()
    }

    private fun initListener() {
        with(binding) {
            selectFriendsIvBack.setOnClickListener { returnFriendFragment() }
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(s: String): Boolean {
                    adapter?.clear()
                    if (s.isNotEmpty()) {
                        db.findFriend(s)
                    }
                    return false
                }
            })
            searchView.setOnCloseListener {
                adapter?.clear()
                false
            }
            db.setOnFriendFoundListener { players ->
                adapter?.clear()
                for (i in players.indices) {
                    if (players[i].id != currentPlayer!!.id) {
                        adapter?.add(FriendSearchModel(players[i]))
                    }
                }
            }
            selectFriendsListView.onItemClickListener = OnItemClickListener { _, _, i, _ ->
                val friend = adapter?.getItem(i)
                Log.e("SFF", friend?.player?.name ?: "")
                Log.e("SFF", friend?.player?.id ?: "")
                Log.e("SFF", friend?.player?.xp.toString())
                Log.e("SFF", friend?.player?.nb_win.toString())
                Log.e("SFF", friend?.player?.nb_lose.toString())
                db.insertFriend(currentPlayer, friend?.player)
                returnFriendFragment()
            }

        }
    }

    private fun returnFriendFragment() {
        val friendsFragment = FriendsFragment()
        fragmentManager?.beginTransaction()?.replace(R.id.menu_fl_select, friendsFragment)?.commit()
    }
}