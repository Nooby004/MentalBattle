package com.example.mlallemant.mentalbattle.UI.Friends;

import com.example.mlallemant.mentalbattle.Utils.Player;

/**
 * Created by m.lallemant on 30/10/2017.
 */

public class DataModel {

    Player player;
    Boolean isConnected;
    String friendAcq;
    String playReq;

    public DataModel(Player player, Boolean isConnected, String friendAcq, String playReq){
        this.player = player;
        this.isConnected = isConnected;
        this.friendAcq = friendAcq;
        this.playReq = playReq;
    }

    public DataModel(){
        super();
    }

    public Player getPlayer() {
        return player;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public String getFriendAcq() {
        return friendAcq;
    }

    public String getPlayReq() {
        return playReq;
    }
}
