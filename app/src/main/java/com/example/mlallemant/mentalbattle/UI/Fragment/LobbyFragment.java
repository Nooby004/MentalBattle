package com.example.mlallemant.mentalbattle.UI.Fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.LoginActivity;
import com.example.mlallemant.mentalbattle.Utils.Utils;

/**
 * Created by m.lallemant on 18/10/2017.
 */

public class LobbyFragment extends Fragment {

    OnCountdownFinish mCallBack;

    public interface OnCountdownFinish{
        void launchGame();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.lobby_fragment, container, false);

        Bundle args = getArguments();
        String currentPlayerName = args.getString("currentPlayer");
        String otherPlayerName = args.getString("otherPlayer");

        TextView tvPlayerOne = (TextView) v.findViewById(R.id.tv_player1);
        TextView tvPlayerTwo = (TextView) v.findViewById(R.id.tv_player2);

        tvPlayerOne.setText(currentPlayerName);
        tvPlayerTwo.setText(otherPlayerName);

        launchCountDown(v);

        return v;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if(context instanceof OnCountdownFinish)
        {
            mCallBack = (OnCountdownFinish) context;
        }
        else
        {
            throw new ClassCastException();
        }
    }


    private void launchCountDown(final View v){

        final TextView tv_countdown = (TextView)  v.findViewById(R.id.tv_countdown);
        new CountDownTimer(Utils.COUNTDOWN_LOBBY, 1000){
            public void onTick(long millisUntilFinished){

                String remainingTime = ""+millisUntilFinished/1000;
                tv_countdown.setText(remainingTime);
            }

            public void onFinish(){
                mCallBack.launchGame();
            }

        }.start();
    }
}
