package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Friends.CustomAdapter;
import com.example.mlallemant.mentalbattle.UI.Friends.DataModel;
import com.example.mlallemant.mentalbattle.UI.Game.GameActivity;
import com.example.mlallemant.mentalbattle.UI.Lobby.PlayAsRegistered;
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
    private ArrayList<DataModel> dataModels;
    private CustomAdapter adapter;
    private Player currentPlayer;
    private Game currentGame;


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

        dataModels = new ArrayList<>();
        adapter = new CustomAdapter(dataModels, getApplicationContext());
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

        db.setOnFriendChangeListener(new DatabaseManager.OnFriendChangeListener() {
            @Override
            public void updateFriendListUI(List<DataModel> friendList) {
                adapter.clear();
                adapter.addAll(friendList);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final DataModel friend = adapter.getItem(i);

                if (friend.getFriendAcq().equals(Utils.ACK_REQUEST_RECEIVED)) {
                    // if not ack, open dialog in order to accept him
                    CustomDialog cdAcceptFriend = new CustomDialog(getActivity(), friend.getPlayer().getId(),
                            "Do you want to accept this friend ?",
                            "YES", R.color.greenColor,
                            "NO", R.color.redColor);
                    cdAcceptFriend.create();
                    cdAcceptFriend.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {
                        @Override
                        public void onClickBtn1() {
                            db.ackFriend(currentPlayer, friend.getPlayer());
                        }

                        @Override
                        public void onClickBtn2() {
                            db.deleteFriend(currentPlayer, friend.getPlayer());
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

                        final CustomDialog cdAskToPlay = new CustomDialog(getActivity(), friend.getPlayer().getId(),
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
    }


    private void returnSelectorFragment(){
        SelectorFragment selectorFragment = new SelectorFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, selectorFragment);
        ft.commit();
    }


    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_LONG).show();
    }
}
