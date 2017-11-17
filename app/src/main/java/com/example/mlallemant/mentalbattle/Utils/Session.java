package com.example.mlallemant.mentalbattle.Utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m.lallemant on 15/11/2017.
 */

public class Session implements Parcelable{

    private String name;
    private String password;
    private List<Player> playerList;

    public Session(String name, String password){
        this.name = name;
        this.password = password;
        playerList = new ArrayList<>();
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void addPlayerToSession(Player player){
        playerList.add(player);
    }

    public void deletePlayerToSession(Player player){
        playerList.remove(player);
    }

    public String getIdSession(){
        return  name + password;
    }


    protected Session(Parcel in) {
        name = in.readString();
        password = in.readString();
        if (in.readByte() == 0x01) {
            playerList = new ArrayList<Player>();
            in.readList(playerList, Player.class.getClassLoader());
        } else {
            playerList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(password);
        if (playerList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(playerList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
}
