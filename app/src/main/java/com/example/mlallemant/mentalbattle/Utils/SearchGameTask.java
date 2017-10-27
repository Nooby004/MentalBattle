package com.example.mlallemant.mentalbattle.Utils;

import android.os.AsyncTask;

/**
 * Created by m.lallemant on 27/10/2017.
 */

public class SearchGameTask extends AsyncTask <String, Void, Game> {

    public SearchGameTask(){

    }


    @Override
    protected Game doInBackground(String... urls) {

        return new Game();
    }


    @Override
    protected void onPostExecute(Game game) {

    }



}
