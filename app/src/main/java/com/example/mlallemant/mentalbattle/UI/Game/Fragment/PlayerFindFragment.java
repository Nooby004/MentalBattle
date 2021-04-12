package com.example.mlallemant.mentalbattle.UI.Game.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Utils;

/**
 * Created by m.lallemant on 18/10/2017.
 */

public class PlayerFindFragment extends Fragment {

    OnCountdownFinish mCallBack;

    public interface OnCountdownFinish {
        void launchGame();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.player_find_fragment, container, false);

        final Bundle args = getArguments();
        final String currentPlayerName = args.getString("currentPlayer");
        final String otherPlayerName = args.getString("otherPlayer");

        final TextView tvPlayerOne = (TextView) v.findViewById(R.id.tv_player1);
        final TextView tvPlayerTwo = (TextView) v.findViewById(R.id.tv_player2);

        tvPlayerOne.setText(currentPlayerName.split(" ")[0]);
        tvPlayerTwo.setText(otherPlayerName.split(" ")[0]);

        launchCountDown(v);

        return v;
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (activity instanceof OnCountdownFinish) {
            mCallBack = (OnCountdownFinish) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (context instanceof OnCountdownFinish) {
            mCallBack = (OnCountdownFinish) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private void launchCountDown(final View v) {

        final TextView tv_countdown = (TextView) v.findViewById(R.id.tv_countdown);
        new CountDownTimer(Utils.COUNTDOWN_LOBBY, 1000) {
            public void onTick(final long millisUntilFinished) {

                final String remainingTime = "" + millisUntilFinished / 1000;
                tv_countdown.setText(remainingTime);
            }

            public void onFinish() {
                mCallBack.launchGame();
            }

        }.start();
    }
}
