package com.example.mlallemant.mentalbattle.Utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class Player implements Parcelable {

    private String id;
    private String name;
    private Integer score;
    private Integer nb_win;
    private Integer nb_lose;
    private Integer xp;


    public Player(String id, String name, Integer score, Integer nb_win, Integer nb_lose, Integer xp){
        this.id = id;
        this.name = name;
        this.score = score;
        this.nb_win = nb_win;
        this.nb_lose = nb_lose;
        this.xp = xp;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getNb_win() {
        return nb_win;
    }

    public void setNb_win(Integer nb_win) {
        this.nb_win = nb_win;
    }

    public Integer getNb_lose() {
        return nb_lose;
    }

    public void setNb_lose(Integer nb_lose) {
        this.nb_lose = nb_lose;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(Integer xp) {
        this.xp = xp;
    }

    protected Player(Parcel in) {
        id = in.readString();
        name = in.readString();
        score = in.readByte() == 0x00 ? null : in.readInt();
        nb_win = in.readByte() == 0x00 ? null : in.readInt();
        nb_lose = in.readByte() == 0x00 ? null : in.readInt();
        xp = in.readByte() == 0x00 ? null : in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        if (score == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(score);
        }
        if (nb_win == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(nb_win);
        }
        if (nb_lose == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(nb_lose);
        }
        if (xp == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(xp);
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
