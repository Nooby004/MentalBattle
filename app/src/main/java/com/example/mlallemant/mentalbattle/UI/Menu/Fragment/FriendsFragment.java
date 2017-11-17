package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Friends.FriendAdapter;
import com.example.mlallemant.mentalbattle.UI.Friends.FriendModel;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.Utils.CustomDialog;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by m.lallemant on 10/11/2017.
 */

public class FriendsFragment extends Fragment {

    //UI
    private ImageView iv_back;
    private ImageView iv_add;
    private ListView listView;

    //Utils
    private DatabaseManager db;
    private ArrayList<FriendModel> friendModels;
    private FriendAdapter adapter;
    private Player currentPlayer;
    private Game currentGame;
    private CustomDialog cdAskToPlay;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_friends_fragment, container, false);

        MenuActivity menuActivity = (MenuActivity) getActivity();
        currentPlayer = menuActivity.getCurrentPlayer();

        db = DatabaseManager.getInstance();

        initUI(v);
        initListener();

        return v;
    }

    private void initUI(View v){
        iv_back = (ImageView) v.findViewById(R.id.select_friends_iv_back);
        iv_add = (ImageView) v.findViewById(R.id.select_friends_iv_add);
        listView = (ListView) v.findViewById(R.id.select_friends_list_view);

        friendModels = new ArrayList<>();
        adapter = new FriendAdapter(friendModels, getApplicationContext());
        listView.setAdapter(adapter);
        adapter.clear();
        adapter.addAll(db.getFriendList());
    }

    private void initListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnSelectorFragment();
            }
        });


        ((MenuActivity) getActivity()).setFriendListToFragment(new MenuActivity.FriendListToFragment() {
            @Override
            public void sendData(List<FriendModel> friendList) {
                adapter.clear();
                adapter.addAll(friendList);

                for (int i = 0; i< friendList.size(); i++) {
                    final FriendModel friend = friendList.get(i);
                    if (friend.getPlayReq().equals(Utils.PLAY_CANCEL)) {
                        if (cdAskToPlay != null) {
                            if (cdAskToPlay.isShowing()) {
                                cdAskToPlay.dismiss();
                                db.resetToPlayWith(currentPlayer, friend.getPlayer());
                            }
                        }
                    }
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final FriendModel friend = adapter.getItem(i);

                final CustomDialog cdConfirmDelete = new CustomDialog(getActivity(), friend.getPlayer().getId(),
                        "Do you really want delete this friend ?",
                        "YES", R.color.greenColor,
                        "NO", R.color.redColor);
                cdConfirmDelete.create();
                cdConfirmDelete.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {
                    @Override
                    public void onClickBtn1() {
                        db.deleteFriend(currentPlayer, friend.getPlayer());
                        cdConfirmDelete.dismiss();
                    }

                    @Override
                    public void onClickBtn2() {
                        cdConfirmDelete.dismiss();
                    }
                });

                return false;
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final FriendModel friend = adapter.getItem(i);

                if (friend.getFriendAcq().equals(Utils.ACK_REQUEST_RECEIVED)) {
                                       // if not ack, open dialog in order to accept him
                    final CustomDialog cdAcceptFriend = new CustomDialog(getActivity(), friend.getPlayer().getId(),
                            "Do you want to accept this friend ?",
                            "YES", R.color.greenColor,
                            "NO", R.color.redColor);
                    cdAcceptFriend.create();
                    cdAcceptFriend.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {
                        @Override
                        public void onClickBtn1() {
                            db.ackFriend(currentPlayer, friend.getPlayer());
                            cdAcceptFriend.dismiss();
                        }

                        @Override
                        public void onClickBtn2() {
                            db.deleteFriend(currentPlayer, friend.getPlayer());
                            cdAcceptFriend.dismiss();
                        }
                    });

                } else if (friend.getFriendAcq().equals(Utils.ACK_REQUEST_SENT)) {
                    makeToast("Friend request sent");
                } else {
                    //REQUEST FRIEND TO PLAY
                    if (friend.getConnected()) {
                        db.askToPlayWith(currentPlayer, friend.getPlayer());
                        final String idGame = currentPlayer.getId()+friend.getPlayer().getId();
                        Game game = new Game(idGame, currentPlayer, friend.getPlayer());
                        currentGame = game;
                        db.insertAvailableGame(currentGame);
                        db.getAvailableGameById(idGame);

                        cdAskToPlay = new CustomDialog(getActivity(), friend.getPlayer().getId(),
                                "Waiting " + friend.getPlayer().getName() + " ...",
                                "CANCEL" , R.color.redColor,
                                null, 0);
                        cdAskToPlay.create();
                        cdAskToPlay.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {
                            @Override
                            public void onClickBtn1() {
                                db.declineToPlayWith(currentPlayer,friend.getPlayer());
                                db.getAvailableGameById(idGame);
                                db.deleteAvailableGame(currentGame);
                                currentGame = null;
                                cdAskToPlay.dismiss();
                            }

                            @Override
                            public void onClickBtn2() {

                            }
                        });
                    }else {
                        makeToast("Your friend is not connected");
                    }
                }
            }
        });

        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchSearchFragment();
            }
        });
    }


    private void returnSelectorFragment(){
        SelectorFragment selectorFragment = new SelectorFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, selectorFragment);
        ft.commit();
    }

    private void launchSearchFragment(){
        SearchFriendFragment searchFriendFragment = new SearchFriendFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, searchFriendFragment);
        ft.commit();
    }


    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }
}
