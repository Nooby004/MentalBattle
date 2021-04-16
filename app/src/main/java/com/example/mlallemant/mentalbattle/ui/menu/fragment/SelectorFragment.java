package com.example.mlallemant.mentalbattle.ui.menu.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.ui.menu.MenuActivity;
import com.example.mlallemant.mentalbattle.utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.utils.Player;
import com.example.mlallemant.mentalbattle.utils.Utils;

/**
 * Created by m.lallemant on 09/11/2017.
 */

public class SelectorFragment extends Fragment {

    private final static String TAG = "SelectorFragment";

    //UI
    private LinearLayout ll_play;
    private LinearLayout ll_create_session;
    private LinearLayout ll_join_session;
    private LinearLayout ll_friends;


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.menu_selector_fragment, container, false);

        final MenuActivity menuActivity = (MenuActivity) getActivity();
        //Utils
        final Player currentPlayer = menuActivity.getCurrentPlayer();
        final DatabaseManager db = DatabaseManager.getInstance();

        db.insertPlayerInLobby(currentPlayer);

        initUI(v);
        initListener();

        return v;
    }

    private void initUI(final View v) {
        ll_play = (LinearLayout) v.findViewById(R.id.selector_play);
        ll_create_session = (LinearLayout) v.findViewById(R.id.selector_create_session);
        ll_join_session = (LinearLayout) v.findViewById(R.id.selector_join_session);
        ll_friends = (LinearLayout) v.findViewById(R.id.selector_friends);

        if (Utils.AUTHENTIFICATION_TYPE == Utils.AUTHENTIFICATION_GUEST) {
            ll_friends.setEnabled(false);
        }

    }


    private void initListener() {
        ll_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                launchPlayFragment();
            }
        });

        ll_join_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                launchJoinFragment();
            }
        });

        ll_create_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                launchCreateFragment();
            }
        });

        ll_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                launchFriendsFragment();
            }
        });
    }


    private void launchPlayFragment() {
        final PlayFragment playFragment = new PlayFragment();
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, playFragment);
        ft.commit();
    }

    private void launchCreateFragment() {
        final CreateJoinFragment createJoinFragment = new CreateJoinFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean("creator", true);
        createJoinFragment.setArguments(bundle);
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, createJoinFragment);
        ft.commit();
    }

    private void launchJoinFragment() {
        final CreateJoinFragment createJoinFragment = new CreateJoinFragment();
        final Bundle bundle = new Bundle();
        bundle.putBoolean("creator", false);
        createJoinFragment.setArguments(bundle);
        final FragmentManager fm = getFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, createJoinFragment);
        ft.commit();
    }

    private void launchFriendsFragment() {
        if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
            final FriendsFragment friendsFragment = new FriendsFragment();
            final FragmentManager fm = getFragmentManager();
            final FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.menu_fl_select, friendsFragment);
            ft.commit();
        }

    }

}
