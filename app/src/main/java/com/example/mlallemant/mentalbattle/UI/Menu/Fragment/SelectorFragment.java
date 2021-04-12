package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.erz.joysticklibrary.JoyStick;
import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.UI.Training.TrainingActivity;
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

    private LinearLayout ll_training;

    //Utils
    private long begin_time = System.currentTimeMillis();
    private Player currentPlayer;
    private DatabaseManager db;


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.menu_selector_fragment, container, false);

        final MenuActivity menuActivity = (MenuActivity) getActivity();
        currentPlayer = menuActivity.getCurrentPlayer();
        db = DatabaseManager.getInstance();

        db.insertPlayerInLobby(currentPlayer);

        initUI(v);
        initListener();

        return v;
    }

    private void initUI(final View v) {
        joy = (JoyStick) v.findViewById(R.id.selector_joy);
        tv_play = (TextView) v.findViewById(R.id.selector_play);
        tv_create_session = (TextView) v.findViewById(R.id.selector_create_session);
        tv_join_session = (TextView) v.findViewById(R.id.selector_join_session);
        tv_friends = (TextView) v.findViewById(R.id.selector_friends);
        iv_play = (ImageView) v.findViewById(R.id.selector_iv_play);
        iv_create = (ImageView) v.findViewById(R.id.selector_iv_create);
        iv_join = (ImageView) v.findViewById(R.id.selector_iv_join);
        iv_friends = (ImageView) v.findViewById(R.id.selector_iv_friends);

        ll_training = (LinearLayout) v.findViewById(R.id.selector_hide_training);

        if (Utils.AUTHENTIFICATION_TYPE == Utils.AUTHENTIFICATION_GUEST) {
            DrawableCompat.setTint(iv_friends.getDrawable(), ContextCompat.getColor(getActivity(), R.color.grayColor));
            tv_friends.setTextColor(ContextCompat.getColor(getActivity(), R.color.grayColor));
        }

    }


    private void initListener() {

        joy.setListener(new JoyStick.JoyStickListener() {
            @Override
            public void onMove(final JoyStick joyStick, final double angle, final double power, final int direction) {

                if (direction == JoyStick.DIRECTION_CENTER) {
                    setTextViewVisibility(true);
                } else {
                    setTextViewVisibility(false);
                }

                if (power < 90) {
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
                    final int[] color = getColorByDuration((int) (System.currentTimeMillis() - begin_time));
                    switch (direction) {
                        case JoyStick.DIRECTION_UP:
                            DrawableCompat.setTint(iv_play.getDrawable(), Color.rgb(color[0], color[1], color[2]));
                            break;

                        case JoyStick.DIRECTION_LEFT:
                            DrawableCompat.setTint(iv_create.getDrawable(), Color.rgb(color[0], color[1], color[2]));
                            break;

                        case JoyStick.DIRECTION_RIGHT:
                            DrawableCompat.setTint(iv_join.getDrawable(), Color.rgb(color[0], color[1], color[2]));
                            break;

                        case JoyStick.DIRECTION_DOWN:
                            if (Utils.AUTHENTIFICATION_TYPE != Utils.AUTHENTIFICATION_GUEST) {
                                DrawableCompat.setTint(iv_friends.getDrawable(), Color.rgb(color[0], color[1], color[2]));
                            }
                            break;
                    }


                    if ((System.currentTimeMillis() - begin_time) > 850) {
                        begin_time = System.currentTimeMillis();

                        switch (direction) {
                            case JoyStick.DIRECTION_UP:
                                Log.e(TAG, "PLAY");
                                launchPlayFragment();
                                break;

                            case JoyStick.DIRECTION_LEFT:
                                Log.e(TAG, "CREATE SESSION");
                                launchCreateFragment();
                                break;

                            case JoyStick.DIRECTION_RIGHT:
                                Log.e(TAG, "JOIN SESSION");
                                launchJoinFragment();
                                break;

                            case JoyStick.DIRECTION_DOWN:
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


        ll_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                launchTrainingActivity();
            }
        });
    }

    private void setTextViewVisibility(final boolean visible) {
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

    private int[] getColorByDuration(final int duration) {

        final int[] color = new int[3];

        final float nbStep = 50;
        final float durationMax = 850;

        final int r1 = 255;
        final int g1 = 143;
        final int b1 = 89;

        final int r2 = 96;
        final int g2 = 195;
        final int b2 = 117;

        final float redStep = (r2 - r1) / nbStep;
        final float greenStep = (g2 - g1) / nbStep;
        final float blueStep = (b2 - b1) / nbStep;

        final float rf = r1 + redStep * (duration * (nbStep / durationMax));
        final float gf = g1 + greenStep * (duration * (nbStep / durationMax));
        final float bf = b1 + blueStep * (duration * (nbStep / durationMax));

        color[0] = Math.round(rf);
        color[1] = Math.round(gf);
        color[2] = Math.round(bf);

        return color;
    }


    private void launchTrainingActivity() {
        final Intent intent = new Intent(getActivity(), TrainingActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}
