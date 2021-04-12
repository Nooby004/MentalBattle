package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Game.GameActivity;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Game;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.SearchGameTask;


/**
 * Created by m.lallemant on 10/11/2017.
 */

public class PlayFragment extends Fragment {

    private final static String TAG = "PlayFragment";

    //UI
    private ImageView iv_back;
    private ImageView iv_play;
    private ProgressBar pg_play;
    private TextView tv_info;

    //Utils
    private boolean isSearchingGame = false;
    private Game currentGame;
    private Player currentPlayer;
    private SearchGameTask searchGameTask;
    private DatabaseManager db;
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_play_fragment, container, false);

        MenuActivity menuActivity = (MenuActivity) getActivity();
        currentPlayer = menuActivity.getCurrentPlayer();
        db = DatabaseManager.getInstance();

        initUI(v);
        initListener();

        DisplayMetrics metrics = new DisplayMetrics();
        menuActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.e(TAG, "density : " + metrics.density);
        Log.e(TAG, "densityDpi : " + metrics.densityDpi);
        Log.e(TAG, "scaleDensity : " + metrics.scaledDensity);

        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelSearch();
    }

    private void cancelSearch() {
        if (searchGameTask != null) {
            searchGameTask.cancel(true);
            searchGameTask = null;
        }
        isSearchingGame = false;
        tv_info.setText("Click on Play to search game !");
        iv_play.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_select_play));
        pg_play.setVisibility(View.INVISIBLE);
    }


    private void initUI(View v) {
        iv_back = (ImageView) v.findViewById(R.id.select_play_iv_back);
        iv_play = (ImageView) v.findViewById(R.id.select_play_iv_play);
        pg_play = (ProgressBar) v.findViewById(R.id.select_play_pg_play);
        tv_info = (TextView) v.findViewById(R.id.select_play_tv_info);

        tv_info.setText("Click on Play to search game !");
    }

    private void initListener() {

        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isSearchingGame) {
                    launchSearchingGameTask();
                } else {
                    cancelSearch();
                }

            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnSelectorFragment();
            }
        });


    }

    private void returnSelectorFragment() {
        SelectorFragment selectorFragment = new SelectorFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, selectorFragment);
        ft.commit();
    }

    private void launchSearchingGameTask() {
        isSearchingGame = true;
        tv_info.setText("Searching game ...");
        iv_play.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_select_cancel));
        pg_play.setVisibility(View.VISIBLE);

        currentGame = null;
        searchGameTask = new SearchGameTask(new SearchGameTask.AsyncResponse() {
            @Override
            public void onFinishTask(Game game) {
                updateUI(game);
            }
        });
        searchGameTask.setParams(currentPlayer, currentGame);
        searchGameTask.execute("");
    }

    private void launchGameActivity(Game game) {
        Intent intent = new Intent(getActivity(), GameActivity.class);
        intent.putExtra("idGame", game.getId());
        intent.putExtra("currentPlayerId", currentPlayer.getId());
        startActivity(intent);
        getActivity().finish();
    }


    private void updateUI(Game game) {
        currentGame = searchGameTask.getCurrentGame();
        currentPlayer = searchGameTask.getCurrentPlayer();

        if (game != null) {
            if ((!game.getPlayer1().getId().equals("")) && (!game.getPlayer2().getId().equals(""))) {

                db.insertInProgressGame(game);
                db.initListenerCurrentGame(game);
                db.deleteAvailableGame(game);

                tv_info.setText("Game found !");
                //Launch game
                launchGameActivity(game);

            } else {
                db.deleteAvailableGame(game);
                db.deletePlayerSearchingPlayer(currentPlayer);
            }
            //pb_btn_play.setVisibility(View.GONE);
        } else {
            launchSearchingGameTask();
        }
    }
}
