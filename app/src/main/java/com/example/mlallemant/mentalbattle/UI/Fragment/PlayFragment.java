package com.example.mlallemant.mentalbattle.UI.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.Utils.Calculation;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by m.lallemant on 19/10/2017.
 */

public class PlayFragment extends Fragment {


    private int COMPTEUR = 0;
    private Game game;
    private EditText et_result;
    private TextView tv_scoreCurrentPlayer;
    private TextView tv_scoreOtherPlayer;
    private TextView tv_calculation;
    private String currentPlayerID;
    private String otherPlayerID;
    private DatabaseManager db;
    private Integer scoreCurrentPlayer = 0;
    private Integer scoreOtherPlayer = 0;
    private List<Calculation> calculationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.play_fragment, container, false);

        //get arguments
        Bundle args = getArguments();
        String currentPlayerName = args.getString("currentPlayer");
        String otherPlayerName = args.getString("otherPlayer");
        currentPlayerID = args.getString("currentPlayerID");
        otherPlayerID = args.getString("otherPlayerID");
        String gameID = args.getString("gameID");

        //init textView
        TextView tv_player1_name = (TextView) v.findViewById(R.id.play_tv_player1_name);
        TextView tv_player2_name = (TextView) v.findViewById(R.id.play_tv_player2_name);

        tv_player1_name.setText(currentPlayerName + " (you)");
        tv_player2_name.setText(otherPlayerName);

        tv_scoreCurrentPlayer = (TextView) v.findViewById(R.id.play_tv_player1_score);
        tv_scoreOtherPlayer = (TextView) v.findViewById(R.id.play_tv_player2_score);
        et_result = (EditText) v.findViewById(R.id.play_et_result);

        tv_calculation = (TextView) v.findViewById(R.id.play_tv_calculation);

        //init parameters
        db = DatabaseManager.getInstance();
        game = db.getGameById(gameID);

        launchCountDown(v);
        initListenerButton(v);

        db.setScoreChangeListener(new DatabaseManager.OnScoreChangeListener() {
            @Override
            public void updateScoreUI(Integer score, String playerID){
                if (score!= null && playerID !=null) {
                    if (playerID.equals(currentPlayerID)) {
                        tv_scoreCurrentPlayer.setText(String.valueOf(score));
                    } else {
                        tv_scoreOtherPlayer.setText(String.valueOf(score));
                    }
                }
            }
        });

        calculationList = game.getCalculationList();
        tv_calculation.setText(calculationList.get(COMPTEUR).getCalculText());

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

                if (resultTextPlayer.equals(String.valueOf(calculationList.get(COMPTEUR).getResult()))){
                    //Player found the correct calculation

                    COMPTEUR++;
                    tv_calculation.setText(calculationList.get(COMPTEUR).getCalculText());
                    et_result.setText("");
                    scoreCurrentPlayer++;

                    if (game.getPlayer1().getId().equals(currentPlayerID)){
                        db.setScorePlayer1ByIdGame(scoreCurrentPlayer, game.getId());
                    }else{
                        db.setScorePlayer2ByIdGame(scoreCurrentPlayer, game.getId());
                    }

                }

            }
        });

        return v;
    }

    private void launchCountDown(final View v){

        final TextView tv_countdown = (TextView)  v.findViewById(R.id.play_tv_counter);
        final ProgressBar pg_counter = (ProgressBar) v.findViewById(R.id.play_pg_counter);
        new CountDownTimer(Utils.COUNTDOWN_PLAY, 1000){
            public void onTick(long millisUntilFinished){

                String remainingTime = ""+millisUntilFinished/1000;
                tv_countdown.setText(remainingTime);
            }

            public void onFinish(){
                //GAME FINISHED
                tv_countdown.setText("0");
                pg_counter.setVisibility(View.INVISIBLE);

            }
        }.start();
    }

    private Boolean isCurrentPlayerWining(){
        Boolean isWining = false;
        Integer currentScore = Integer.parseInt(tv_scoreCurrentPlayer.getText().toString());
        Integer otherScore = Integer.parseInt(tv_scoreOtherPlayer.getText().toString());

        if(currentScore > otherScore) isWining = true;

        if (currentScore == otherScore ) isWining = null;

        return isWining;
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
        if (text.length() <= Utils.MAX_LENGHT_RESULT) success = true;
        return success;
    }

    private String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }


    /*private class MyAsyncTask extends AsyncTask<String, Void, Game> {

        @Override
        protected Game doInBackground(String... params) {
            return db.getGameById(game.getId());
        }

        @Override
        protected void onPostExecute(Game result) {
            if (result!=null){
                if (result.getPlayer1().getId().equals(currentPlayerID)){
                    scoreCurrentPlayer = result.getPlayer1().getScore();
                    tv_scoreCurrentPlayer.setText(String.valueOf(scoreCurrentPlayer));

                    scoreOtherPlayer = result.getPlayer2().getScore();
                    tv_scoreOtherPlayer.setText(String.valueOf(scoreOtherPlayer));

                }else{
                    scoreCurrentPlayer = result.getPlayer2().getScore();
                    tv_scoreCurrentPlayer.setText(String.valueOf(scoreCurrentPlayer));

                    scoreOtherPlayer = result.getPlayer1().getScore();
                    tv_scoreOtherPlayer.setText(String.valueOf(scoreOtherPlayer));
                }
            }

        }
    }*/
}

