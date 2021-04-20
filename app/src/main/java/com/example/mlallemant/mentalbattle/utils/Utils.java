package com.example.mlallemant.mentalbattle.utils;

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class Utils {

    public final static int SEARCH_TIME = 60000;

    public final static int COUNTDOWN_LOBBY = 5000;

    public final static int COUNTDOWN_PLAY = 60000;

    public final static int MAX_LENGTH_RESULT = 6;

    public final static int AUTHENTIFICATION_GUEST = 100;
    public final static int AUTHENTIFICATION_ACCOUNT = 101;
    public final static int AUTHENTIFICATION_FB = 102;
    public final static int AUTHENTIFICATION_GOOGLE = 102;


    public static int AUTHENTIFICATION_TYPE = AUTHENTIFICATION_GUEST;

    public static String ACK_REQUEST_SENT = "req_sent";
    public static String ACK_REQUEST_RECEIVED = "req_received";
    public static String ACK_OK = "req_ok";

    public static String PLAY_REQUEST_SENT = "req_sent";
    public static String PLAY_REQUEST_RECEIVED = "req_received";
    public static String PLAY_OK = "req_ok";
    public static String PLAY_KO = "req_ko";
    public static String PLAY_CANCEL = "req_cancel";

    public static String SESSION_RDY_KO = "ready_ko";
    public static String SESSION_RDY_NO = "ready_no";
    public static String SESSION_RDY_YES = "ready_yes";

    public static String SESSION_NEW_YES = "new_yes";
    public static String SESSION_NEW_NO = "new_no";
    public static String SESSION_CREATOR = "creator";
    public static String SESSION_LEFT = "left";

    public static String SESSION_STATE_LAUNCH_ROUND = "launch_round";
    public static String SESSION_STATE_LAUNCH_PARTY = "launch_party";
    public static String SESSION_STATE_WAITING = "waiting";

}
