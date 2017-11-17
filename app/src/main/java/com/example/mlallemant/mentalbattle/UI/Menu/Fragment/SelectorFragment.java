package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.erz.joysticklibrary.JoyStick;
import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;

/**
 * Created by m.lallemant on 09/11/2017.
 */

public class SelectorFragment extends Fragment {

    private final static String TAG = "SelectorFragment";

    //UI
    private JoyStick joy;
    private TextView tv_play;
    private TextView tv_create_session;
    private TextView tv_join_session;
    private TextView tv_friends;
    private ImageView iv_play;
    private ImageView iv_create;
    private ImageView iv_join;
    private ImageView iv_friends;

    //Utils
    private long begin_time;
    private Player currentPlayer;
    private DatabaseManager db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_selector_fragment, container, false);

        MenuActivity menuActivity = (MenuActivity) getActivity();
        currentPlayer = menuActivity.getCurrentPlayer();
        db = DatabaseManager.getInstance();

        db.insertPlayerInLobby(currentPlayer);

        initUI(v);
        initListener();

        return v;
    }

    private void initUI(View v){
        joy = (JoyStick) v.findViewById(R.id.selector_joy);
        tv_play = (TextView) v.findViewById(R.id.selector_play);
        tv_create_session = (TextView) v.findViewById(R.id.selector_create_session);
        tv_join_session = (TextView) v.findViewById(R.id.selector_join_session);
        tv_friends = (TextView) v.findViewById(R.id.selector_friends);
        iv_play = (ImageView) v.findViewById(R.id.selector_iv_play);
        iv_create = (ImageView) v.findViewById(R.id.selector_iv_create);
        iv_join = (ImageView) v.findViewById(R.id.selector_iv_join);
        iv_friends = (ImageView) v.findViewById(R.id.selector_iv_friends);

        if (Utils.AUTHENTIFICATION_TYPE == Utils.AUTHENTIFICATION_GUEST) {
            DrawableCompat.setTint(iv_friends.getDrawable(), ContextCompat.getColor(getActivity(), R.color.grayColor));
            tv_friends.setTextColor(ContextCompat.getColor(getActivity(), R.color.grayColor));
        }

    }


    private void initListener(){

        joy.setListener(new JoyStick.JoyStickListener() {
            @Override
            public void onMove(JoyStick joyStick, double angle, double power, int direction) {

                if (direction == JoyStick.DIRECTION_CENTER) {
                    setTextViewVisibility(true);
                } else {
                    setTextViewVisibility(false);
                }

                if (power < 90){
                    begin_time = System.currentTimeMillis();
                    DrawableCompat.setTint(iv_play.getDrawable(), ContextCompat.getColor(getActivity(), R.color.orangeColor));
                    DrawableCompat.setTint(iv_create.getDrawable(), ContextCompat.getColor(getActivity(), R.color.orangeColor));
                    DrawableCompat.setTint(iv_join.getDrawable(), ContextCompat.getColor(getActivity(), R.color.orangeColor));

                    if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
                        DrawableCompat.setTint(iv_friends.getDrawable(), ContextCompat.getColor(getActivity(), R.color.orangeColor));
                    } else {
                        DrawableCompat.setTint(iv_friends.getDrawable(), ContextCompat.getColor(getActivity(), R.color.grayColor));
                    }

                } else {

                    switch (direction){
                        case JoyStick.DIRECTION_UP :
                            DrawableCompat.setTint(iv_play.getDrawable(), ContextCompat.getColor(getActivity(), R.color.greenColor));
                            break;

                        case JoyStick.DIRECTION_LEFT :
                            DrawableCompat.setTint(iv_create.getDrawable(), ContextCompat.getColor(getActivity(), R.color.greenColor));
                            break;

                        case JoyStick.DIRECTION_RIGHT :
                            DrawableCompat.setTint(iv_join.getDrawable(), ContextCompat.getColor(getActivity(), R.color.greenColor));
                            break;

                        case JoyStick.DIRECTION_DOWN :
                            if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
                                DrawableCompat.setTint(iv_friends.getDrawable(), ContextCompat.getColor(getActivity(), R.color.greenColor));
                            }
                            break;
                    }



                    if ((System.currentTimeMillis() - begin_time) > 750) {
                        begin_time = System.currentTimeMillis();

                        switch (direction){
                            case JoyStick.DIRECTION_UP :
                                Log.e(TAG, "PLAY");
                                launchPlayFragment();
                                break;

                            case JoyStick.DIRECTION_LEFT :
                                Log.e(TAG, "CREATE SESSION");
                                launchCreateFragment();
                                break;

                            case JoyStick.DIRECTION_RIGHT :
                                Log.e(TAG, "JOIN SESSION");
                                launchJoinFragment();
                                break;

                            case JoyStick.DIRECTION_DOWN :
                                Log.e(TAG, "FRIENDS");
                                launchFriendsFragment();
                                break;
                        }
                    }
                }
            }

            @Override
            public void onTap() {

            }

            @Override
            public void onDoubleTap() {

            }
        });
    }

    private void setTextViewVisibility(boolean visible) {
        if (visible) {
            tv_friends.setVisibility(View.VISIBLE);
            tv_play.setVisibility(View.VISIBLE);
            tv_join_session.setVisibility(View.VISIBLE);
            tv_create_session.setVisibility(View.VISIBLE);

        } else {
            tv_friends.setVisibility(View.INVISIBLE);
            tv_play.setVisibility(View.INVISIBLE);
            tv_join_session.setVisibility(View.INVISIBLE);
            tv_create_session.setVisibility(View.INVISIBLE);
        }
    }


    private void launchPlayFragment(){
        PlayFragment playFragment = new PlayFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, playFragment);
        ft.commit();
    }

    private void launchCreateFragment() {
        CreateJoinFragment createJoinFragment = new CreateJoinFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("creator", true);
        createJoinFragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, createJoinFragment);
        ft.commit();
    }

    private void launchJoinFragment() {
        CreateJoinFragment createJoinFragment = new CreateJoinFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("creator", false);
        createJoinFragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, createJoinFragment);
        ft.commit();
    }

    private void launchFriendsFragment(){
        if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
            FriendsFragment friendsFragment = new FriendsFragment();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.menu_fl_select, friendsFragment);
            ft.commit();
        }

    }

}
