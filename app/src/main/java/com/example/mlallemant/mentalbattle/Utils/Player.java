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


    public Player(String id, String name, Integer score){
        this.id = id;
        this.name = name;
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


    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    protected Player(Parcel in) {
        id = in.readString();
        name = in.readString();
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
