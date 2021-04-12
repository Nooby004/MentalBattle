package com.example.mlallemant.mentalbattle.UI.Menu.Fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mlallemant.mentalbattle.R;
import com.example.mlallemant.mentalbattle.UI.Menu.MenuActivity;
import com.example.mlallemant.mentalbattle.Utils.DatabaseManager;
import com.example.mlallemant.mentalbattle.Utils.Player;
import com.example.mlallemant.mentalbattle.Utils.Session;
import com.example.mlallemant.mentalbattle.Utils.Utils;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by m.lallemant on 15/11/2017.
 */

public class CreateJoinFragment extends Fragment {

    //UI
    private TextView tv_title;
    private ImageView iv_back;
    private EditText et_session_name;
    private EditText et_session_password;
    private Button btn_create_session;
    private ProgressBar pb_create_session;

    //Utils
    private Player currentPlayer;
    private DatabaseManager db;
    private boolean isCreator = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_create_fragment, container, false);

        MenuActivity menuActivity = (MenuActivity) getActivity();
        currentPlayer = menuActivity.getCurrentPlayer();

        db = DatabaseManager.getInstance();
        db.deletePlayerInLobby(currentPlayer);

        isCreator = getArguments().getBoolean("creator");

        initUI(v);
        initListener();

        return v;
    }


    private void initUI(View v){
        iv_back = (ImageView) v.findViewById(R.id.iv_create_back);
        et_session_name = (EditText) v.findViewById(R.id.et_create_session_name);
        et_session_password = (EditText) v.findViewById(R.id.et_create_session_password);
        btn_create_session = (Button) v.findViewById(R.id.btn_create_session);
        tv_title = (TextView) v.findViewById(R.id.tv_title_session);
        pb_create_session = (ProgressBar) v.findViewById(R.id.pb_create_session);

        pb_create_session.setVisibility(View.INVISIBLE);
        if (isCreator){
            btn_create_session.setText("CREATE");
            tv_title.setText("Create Session");
        } else {
            btn_create_session.setText("JOIN");
            tv_title.setText("Join Session");
        }
    }

    private void initListener(){
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnSelectorFragment();
                db.insertPlayerInLobby(currentPlayer);
            }
        });

        btn_create_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb_create_session.setVisibility(View.VISIBLE);
                if (isCreator){
                    createSession();
                } else {
                    joinSession();
                }

            }
        });

    }

    private void createSession() {
        String sessionName = et_session_name.getText().toString();
        String sessionPassword = et_session_password.getText().toString();

        if (sessionName.length() > 4 && sessionPassword.length() > 5) {

            final Session session = new Session(sessionName, sessionPassword, Utils.SESSION_STATE_WAITING);
            db.initCheckSessionExist(session);

            db.setOnSessionExistListener(new DatabaseManager.OnSessionExistListener() {
                @Override
                public void notifyUser(boolean isExist) {
                    if (isExist){
                        makeToast("Session already exists");
                    } else {
                        db.insertSession(session);
                        Player playerForSession = new Player(currentPlayer.getId(), currentPlayer.getName(), currentPlayer.getScore(), Utils.SESSION_RDY_YES, Utils.SESSION_CREATOR);
                        db.insertPlayerInSession(session, playerForSession);
                        launchSessionLauncherFragment(true);
                    }
                    pb_create_session.setVisibility(View.INVISIBLE);
                }
            });

        } else {
            makeToast("name or password not enough long");
        }

    }

    private void joinSession(){
        String sessionName = et_session_name.getText().toString();
        String sessionPassword = et_session_password.getText().toString();


        if (sessionName.length() > 0 && sessionPassword.length() > 0) {
            final Session session = new Session(sessionName, sessionPassword, Utils.SESSION_STATE_WAITING);
            db.initCheckSessionExist(session);

            db.setOnSessionExistListener(new DatabaseManager.OnSessionExistListener() {
                @Override
                public void notifyUser(boolean isExist) {
                    if (isExist){
                        Player playerForSession = new Player(currentPlayer.getId(), currentPlayer.getName(), currentPlayer.getScore(), Utils.SESSION_RDY_KO, Utils.SESSION_NEW_YES);
                        db.insertPlayerInSession(session, playerForSession);
                        launchSessionLauncherFragment(false);
                    } else {
                        makeToast("Bad name or password");
                    }
                    pb_create_session.setVisibility(View.INVISIBLE);
                }
            });
        }else {
            makeToast("Bad name or password");
        }


    }

    private void makeToast(String text){
        Toast.makeText(getApplicationContext(), text,
                Toast.LENGTH_SHORT).show();
    }


    private void returnSelectorFragment(){
        SelectorFragment selectorFragment = new SelectorFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, selectorFragment);
        ft.commit();
    }

    private void launchSessionLauncherFragment(boolean isCreator){

        SessionLauncherFragment sessionLauncherFragment = new SessionLauncherFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("creator", isCreator);
        bundle.putString("name", et_session_name.getText().toString());
        bundle.putString("password", et_session_password.getText().toString());
        sessionLauncherFragment.setArguments(bundle);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.menu_fl_select, sessionLauncherFragment);
        ft.commit();
    }
}
