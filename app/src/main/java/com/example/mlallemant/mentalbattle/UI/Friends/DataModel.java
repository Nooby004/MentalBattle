package com.example.mlallemant.mentalbattle.UI.Friends;

import android.graphics.Bitmap;

import com.example.mlallemant.mentalbattle.Utils.Player;

/**
 * Created by m.lallemant on 30/10/2017.
 */

public class DataModel {

    Player player;
    Boolean isConnected;
    String friendAcq;
    String playReq;
    String xp;
    Bitmap profilePicture;


    public DataModel(Player player, Boolean isConnected, String friendAcq, String playReq, String xp, Bitmap profilePicture){
        this.player = player;
        this.isConnected = isConnected;
        this.friendAcq = friendAcq;
        this.playReq = playReq;
        this.xp = xp;
        this.profilePicture = profilePicture;
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

    public String getXp() {
        return xp;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }
}
