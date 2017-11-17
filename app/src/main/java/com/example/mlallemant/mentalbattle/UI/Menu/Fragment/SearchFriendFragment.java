package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Friends.FriendSearchAdapter;
import com.example.mlallemant.mentalbattle.UI.Friends.FriendSearchModel;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by m.lallemant on 13/11/2017.
 */

public class SearchFriendFragment extends Fragment {

    //UI
    private ListView lv_search;
    private SearchView sv_friend;
    private ImageView iv_back;


    //Utils
    private Player currentPlayer;
    private DatabaseManager db;
    private ArrayList<FriendSearchModel> friendSearchModel;
    private FriendSearchAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_find_friend_fragment, container, false);

        MenuActivity menuActivity = (MenuActivity) getActivity();
        currentPlayer = menuActivity.getCurrentPlayer();

        db = DatabaseManager.getInstance();

        initUI(v);
        initListener();

        return v;
    }

    private void initUI(View v){
        lv_search = (ListView) v.findViewById(R.id.select_find_list_view);
        sv_friend = (SearchView) v.findViewById(R.id.select_find_sv_friend);
        iv_back = (ImageView) v.findViewById(R.id.select_find_iv_back);

        friendSearchModel = new ArrayList<>();
        adapter = new FriendSearchAdapter(friendSearchModel, getActivity());
        lv_search.setAdapter(adapter);
        adapter.clear();
    }

    private void initListener(){

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnFriendFragment();
            }
        });

        sv_friend.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.clear();
                if (s.length() > 0) {
                    db.findFriend(s);
                }
                return false;
            }
        });

        sv_friend.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.clear();
                return false;
            }
        });

        db.setOnFriendFoundListener(new DatabaseManager.OnFriendFoundListener() {
            @Override
            public void updateFriendFoundUI(List<Player> players) {
                adapter.clear();
                for (int i = 0; i<players.size();i++) {
                    if (!players.get(i).getId().equals(currentPlayer.getId())) {
                        adapter.add(new FriendSearchModel(players.get(i)));
                    }
                }
            }
        });

        lv_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FriendSearchModel friend = adapter.getItem(i);

                Log.e("SFF", friend.getPlayer().getName());
                Log.e("SFF", friend.getPlayer().getId());
                Log.e("SFF", friend.getPlayer().getXp().toString());
                Log.e("SFF", friend.getPlayer().getNb_win().toString());
                Log.e("SFF", friend.getPlayer().getNb_lose().toString());


                db.insertFriend(currentPlayer, friend.getPlayer());

                returnFriendFragment();
            }
        });


    }

    private void returnFriendFragment(){
        FriendsFragment friendsFragment = new FriendsFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, friendsFragment);
        ft.commit();
    }
}
