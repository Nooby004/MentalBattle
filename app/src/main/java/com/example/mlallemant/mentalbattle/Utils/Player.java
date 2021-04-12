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

    private String ready;
    private String new_;

    public Player(final String id, final String name, final Integer score, final Integer nb_win, final Integer nb_lose, final Integer xp) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.nb_win = nb_win;
        this.nb_lose = nb_lose;
        this.xp = xp;
    }

    public Player(final String id, final String name, final Integer score, final String ready, final String new_) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.ready = ready;
        this.new_ = new_;
    }

    public Player() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(final Integer score) {
        this.score = score;
    }

    public Integer getNb_win() {
        return nb_win;
    }

    public void setNb_win(final Integer nb_win) {
        this.nb_win = nb_win;
    }

    public Integer getNb_lose() {
        return nb_lose;
    }

    public void setNb_lose(final Integer nb_lose) {
        this.nb_lose = nb_lose;
    }

    public Integer getXp() {
        return xp;
    }

    public void setXp(final Integer xp) {
        this.xp = xp;
    }

    public String getReady() {
        return ready;
    }

    public void setReady(final String ready) {
        this.ready = ready;
    }

    public String getNew_() {
        return new_;
    }

    public void setNew_(final String new_) {
        this.new_ = new_;
    }

    protected Player(final Parcel in) {
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
    public void writeToParcel(final Parcel dest, final int flags) {
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
        public Player createFromParcel(final Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(final int size) {
            return new Player[size];
        }
    };

}
