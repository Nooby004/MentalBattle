package com.example.mlallemant.mentalbattle.UI.Session.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mlallemant.mentalbattle.R;

/**
 * Created by m.lallemant on 17/11/2017.
 */

public class LoadingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.loading_activity, container, false);

        return v;
    }

}
