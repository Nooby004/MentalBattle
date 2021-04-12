package com.example.mlallemant.mentalbattle.UI.Session.Fragment;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Calculation;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by m.lallemant on 17/11/2017.
 */

public class RoundFragment extends Fragment {

    //UI
    private TextView tv_round_score_title;
    private TextView tv_round_score;
    private TextView tv_total_score_title;
    private TextView tv_total_score;
    private TextView tv_counter;
    private TextView tv_calculation;
    private EditText et_result;


    //Utils
    private CountDownTimer countDownTimer;
    private DatabaseManager db;
    private Session session;
    private Player currentPlayer;
    private int COUNTER = 0;
    private int roundScore = 0;
    private int totalScore = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.play_fragment, container, false);

        Bundle bundle = getArguments();
        session = bundle.getParcelable("session");
        currentPlayer = bundle.getParcelable("currentPlayer");

        db = DatabaseManager.getInstance();
        db.initListenerCurrentSession(session);
        initUI(v);
        initListener();

        return v;
    }


    private void initUI(View v) {
        tv_round_score_title = (TextView) v.findViewById(R.id.play_tv_player1_name);
        tv_round_score = (TextView) v.findViewById(R.id.play_tv_player1_score);
        tv_total_score_title = (TextView) v.findViewById(R.id.play_tv_player2_name);
        tv_total_score = (TextView) v.findViewById(R.id.play_tv_player2_score);
        tv_counter = (TextView) v.findViewById(R.id.play_tv_counter);
        tv_calculation = (TextView) v.findViewById(R.id.play_tv_calculation);
        et_result = (EditText) v.findViewById(R.id.play_et_result);

        tv_round_score_title.setText("Round score");
        tv_round_score.setText(String.valueOf(roundScore));
        tv_total_score_title.setText("Total score");

        db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_NO);
        initListenerButton(v);
        launchCountDown(Utils.COUNTDOWN_PLAY);
        tv_calculation.setText(session.getCalculationList().get(COUNTER).getCalculText());
    }

    private void initListener() {

        db.setOnSessionUpdateListener(new DatabaseManager.OnSessionUpdateListener() {
            @Override
            public void updateSessionUI(Session session_) {

                session = session_;
                for(Player player : session_.getPlayerList()){
                    if (player.getId().equals(currentPlayer.getId())){
                        totalScore = player.getScore();
                        tv_total_score.setText(String.valueOf(totalScore));
                    }
                }

            }
        });


        et_result.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String resultTextPlayer = editable.toString();

                if (resultTextPlayer.equals(String.valueOf(session.getCalculationList().get(COUNTER).getResult()))){
                    //Player found the correct result
                    COUNTER ++;
                    tv_calculation.setText(session.getCalculationList().get(COUNTER).getCalculText());
                    et_result.setText("");
                    totalScore++;
                    roundScore++;
                    db.updateScoreCurrentPlayerInSession(session, currentPlayer, totalScore);
                    tv_round_score.setText(String.valueOf(roundScore));

                }
            }
        });
    }




    private void launchCountDown(int countdownPlay){

        countDownTimer = new  CountDownTimer(countdownPlay, 1000){
            public void onTick(long millisUntilFinished){

                String remainingTime = ""+millisUntilFinished/1000;
                tv_counter.setText(remainingTime);
            }

            public void onFinish(){
                //GAME FINISHED
                tv_counter.setText("0");
                countDownTimer.cancel();
                db.updatePlayerReady(session, currentPlayer, Utils.SESSION_RDY_YES);
                launchLoadingFragment();

            }
        }.start();
    }

    private void initListenerButton(final View v)
    {
        Button btn_0 = (Button) v.findViewById(R.id.play_btn_0);
        Button btn_1 = (Button) v.findViewById(R.id.play_btn_1);
        Button btn_2 = (Button) v.findViewById(R.id.play_btn_2);
        Button btn_3 = (Button) v.findViewById(R.id.play_btn_3);
        Button btn_4 = (Button) v.findViewById(R.id.play_btn_4);
        Button btn_5 = (Button) v.findViewById(R.id.play_btn_5);
        Button btn_6 = (Button) v.findViewById(R.id.play_btn_6);
        Button btn_7 = (Button) v.findViewById(R.id.play_btn_7);
        Button btn_8 = (Button) v.findViewById(R.id.play_btn_8);
        Button btn_9 = (Button) v.findViewById(R.id.play_btn_9);
        Button btn_minus = (Button) v.findViewById(R.id.play_btn_minus);
        ImageButton btn_backspace = (ImageButton) v.findViewById(R.id.play_btn_backspace);

        btn_0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 0;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 1;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 2;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 3;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 4;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 5;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 6;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 7;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 8;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = et_result.getText().toString() + 9;
                if (checkLengthText(text)) et_result.setText(text);
            }
        });

        btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et_result.getText().toString().contains("-")){
                    String text = "-" + et_result.getText().toString();
                    if (checkLengthText(text)) et_result.setText(text);
                }
            }
        });

        btn_backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_result.getText().toString().length() > 0) {
                    String text = removeLastChar(et_result.getText().toString());
                    if (checkLengthText(text)) et_result.setText(text);
                }
            }
        });

    }

    private Boolean checkLengthText(String text){
        Boolean success = false;
        if (text.length() <= Utils.MAX_LENGTH_RESULT) success = true;
        return success;
    }

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }

    private void launchLoadingFragment(){

        for (Player player : session.getPlayerList()){
            if (player.getNew_().equals(Utils.SESSION_CREATOR)){
                if (player.getId().equals(currentPlayer.getId())){
                    db.updateCalculationListInSession(session, generateCalculationList());
                }
            }
        }

        db.removeListenerCurrentSession(session);
        LoadingFragment lf = new LoadingFragment();

        Bundle args = new Bundle();
        args.putParcelable("session", session);
        args.putParcelable("currentPlayer", currentPlayer);
        lf.setArguments(args);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fl_session, lf);
        ft.commit();
    }
    private List<Calculation> generateCalculationList(){

        List<Calculation> calculationList_ = new ArrayList<>();

        for (int i=0; i<50; i++){
            calculationList_.add(new Calculation());
        }
        return  calculationList_;
    }

}
