package com.example.mlallemant.mentalbattle.ui.menu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.ui.friends.FriendAdapter;
import com.example.mlallemant.mentalbattle.ui.friends.FriendModel;
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity;
import com.example.mlallemant.mentalbattle.utils.CustomDialog;
import com.example.mlallemant.mentalbattle.utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.utils.Game;
import com.example.mlallemant.mentalbattle.utils.Player;
import com.example.mlallemant.mentalbattle.utils.Utils;

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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.menu_friends_fragment, container, false);

        final MenuActivity menuActivity = (MenuActivity) getActivity();
        currentPlayer = menuActivity.getCurrentPlayer();

        db = DatabaseManager.getInstance();

        initUI(v);
        initListener();

        return v;
    }

    private void initUI(final View v) {
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
            public void onClick(final View view) {
                returnSelectorFragment();
            }
        });


        ((MenuActivity) getActivity()).setFriendListToFragment(new MenuActivity.FriendListToFragment() {
            @Override
            public void sendData(final List<FriendModel> friendList) {
                adapter.clear();
                adapter.addAll(friendList);

                for (int i = 0; i < friendList.size(); i++) {
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
            public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {

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
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {

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
                        final String idGame = currentPlayer.getId() + friend.getPlayer().getId();
                        final Game game = new Game(idGame, currentPlayer, friend.getPlayer(), Game.generateCalculationList());
                        currentGame = game;
                        db.insertAvailableGame(currentGame);
                        db.getAvailableGameById(idGame);

                        cdAskToPlay = new CustomDialog(getActivity(), friend.getPlayer().getId(),
                                "Waiting " + friend.getPlayer().getName() + " ...",
                                "CANCEL", R.color.redColor,
                                null, 0);
                        cdAskToPlay.create();
                        cdAskToPlay.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {
                            @Override
                            public void onClickBtn1() {
                                db.declineToPlayWith(currentPlayer, friend.getPlayer());
                                db.getAvailableGameById(idGame);
                                db.deleteAvailableGame(currentGame);
                                currentGame = null;
                                cdAskToPlay.dismiss();
                            }

                            @Override
                            public void onClickBtn2() {

                            }
                        });
                    } else {
                        makeToast("Your friend is not connected");
                    }
                }
            }
        });

        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                launchSearchFragment();
            }
        });
    }


    private void returnSelectorFragment() {
        final SelectorFragment selectorFragment = new SelectorFragment();
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, selectorFragment);
        ft.commit();
    }

    private void launchSearchFragment() {
        final SearchFriendFragment searchFriendFragment = new SearchFriendFragment();
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, searchFriendFragment);
        ft.commit();
    }


    private void makeToast(final String text) {
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }
}
