package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.Config;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.erz.joysticklibrary.JoyStick;
import com.example.mlallemant.mentalbattle.R;

import org.w3c.dom.Text;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_selector_fragment, container, false);


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
                    DrawableCompat.setTint(iv_friends.getDrawable(), ContextCompat.getColor(getActivity(), R.color.orangeColor));

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
                            DrawableCompat.setTint(iv_friends.getDrawable(), ContextCompat.getColor(getActivity(), R.color.greenColor));
                            break;
                    }



                    if ((System.currentTimeMillis() - begin_time) > 1000) {
                        begin_time = System.currentTimeMillis();

                        switch (direction){
                            case JoyStick.DIRECTION_UP :
                                Log.e(TAG, "PLAY");
                                launchPlayFragment();
                                break;

                            case JoyStick.DIRECTION_LEFT :
                                Log.e(TAG, "CREATE SESSION");
                                break;

                            case JoyStick.DIRECTION_RIGHT :
                                Log.e(TAG, "JOIN SESSION");
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

    private void launchFriendsFragment(){

        FriendsFragment friendsFragment = new FriendsFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, friendsFragment);
        ft.commit();
    }

}
