package com.example.mlallemant.mentalbattle.utils

/**
 * Created by m.lallemant on 13/10/2017.
 */
object Utils {
    const val SEARCH_TIME = 60000
    const val COUNTDOWN_LOBBY = 5000
    const val COUNTDOWN_PLAY = 60000
    const val MAX_LENGTH_RESULT = 6
    const val AUTHENTIFICATION_GUEST = 100
    const val AUTHENTIFICATION_ACCOUNT = 101
    const val AUTHENTIFICATION_FB = 102
    const val AUTHENTIFICATION_GOOGLE = 102
    const val ACK_REQUEST_SENT = "req_sent"
    const val ACK_REQUEST_RECEIVED = "req_received"
    const val ACK_OK = "req_ok"
    const val PLAY_REQUEST_SENT = "req_sent"
    const val PLAY_REQUEST_RECEIVED = "req_received"
    const val PLAY_OK = "req_ok"
    const val PLAY_KO = "req_ko"
    const val PLAY_CANCEL = "req_cancel"
    const val SESSION_RDY_KO = "ready_ko"
    const val SESSION_RDY_NO = "ready_no"
    const val SESSION_RDY_YES = "ready_yes"
    const val SESSION_NEW_YES = "new_yes"
    const val SESSION_NEW_NO = "new_no"
    const val SESSION_CREATOR = "creator"
    const val SESSION_LEFT = "left"
    const val SESSION_STATE_LAUNCH_ROUND = "launch_round"
    const val SESSION_STATE_LAUNCH_PARTY = "launch_party"
    const val SESSION_STATE_WAITING = "waiting"

    const val ONE_MEGABYTE = (1024 * 1024).toLong()
    var AUTHENTIFICATION_TYPE = AUTHENTIFICATION_GUEST
}