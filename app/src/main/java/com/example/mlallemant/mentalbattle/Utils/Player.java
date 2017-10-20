package com.example.mlallemant.mentalbattle.Utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class Player implements Parcelable {

    private String id;
    private String name;
    private Boolean inGame;
    private Boolean isConnected;
    private Boolean isSearchingGame;
    private Integer score;


    public Player(String id, String name, Boolean inGame, Boolean isConnected, Boolean isSearchingGame, Integer score){
        this.id = id;
        this.name = name;
        this.inGame = inGame;
        this.isConnected = isConnected;
        this.isSearchingGame = isSearchingGame;
        this.score = score;
    }

    public Player()
    {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getInGame() {
        return inGame;
    }

    public void setInGame(Boolean inGame) {
        this.inGame = inGame;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public Boolean getSearchingGame() {
        return isSearchingGame;
    }

    public void setSearchingGame(Boolean searchingGame) {
        isSearchingGame = searchingGame;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    protected Player(Parcel in) {
        id = in.readString();
        name = in.readString();
        byte inGameVal = in.readByte();
        inGame = inGameVal == 0x02 ? null : inGameVal != 0x00;
        byte isConnectedVal = in.readByte();
        isConnected = isConnectedVal == 0x02 ? null : isConnectedVal != 0x00;
        byte isSearchingGameVal = in.readByte();
        isSearchingGame = isSearchingGameVal == 0x02 ? null : isSearchingGameVal != 0x00;
        score = in.readByte() == 0x00 ? null : in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        if (inGame == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (inGame ? 0x01 : 0x00));
        }
        if (isConnected == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (isConnected ? 0x01 : 0x00));
        }
        if (isSearchingGame == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (isSearchingGame ? 0x01 : 0x00));
        }
        if (score == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(score);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };

}
