package com.example.mlallemant.mentalbattle.Utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m.lallemant on 15/11/2017.
 */

public class Session implements Parcelable {

    private final static int NB_CALCULATION = 50;

    private String name;
    private String password;
    private String state;
    private List<Calculation> calculationList;
    private List<Player> playerList;

    public Session(final String name, final String password, final String state) {
        this.name = name;
        this.password = password;
        this.state = state;
        calculationList = generateCalculationList();
        playerList = new ArrayList<>();
    }


    public Session(final String name, final String password, final String state, final List<Calculation> calculationList) {
        this.name = name;
        this.password = password;
        this.state = state;
        this.calculationList = calculationList;
        playerList = new ArrayList<>();
    }

    public Session() {
        super();
    }

    private List<Calculation> generateCalculationList() {

        final List<Calculation> calculationList_ = new ArrayList<>();

        for (int i = 0; i < NB_CALCULATION; i++) {
            calculationList_.add(new Calculation());
        }
        return calculationList_;
    }

    public void setPlayerList(final List<Player> playerList) {
        this.playerList = playerList;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public List<Calculation> getCalculationList() {
        return calculationList;
    }

    public void setCalculationList(final List<Calculation> calculationList) {
        this.calculationList = calculationList;
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

    public void addPlayerToSession(final Player player) {
        playerList.add(player);
    }

    public void deletePlayerToSession(final Player player) {
        playerList.remove(player);
    }

    public String getIdSession() {
        return name + password;
    }

    protected Session(final Parcel in) {
        name = in.readString();
        password = in.readString();
        state = in.readString();
        if (in.readByte() == 0x01) {
            calculationList = new ArrayList<Calculation>();
            in.readList(calculationList, Calculation.class.getClassLoader());
        } else {
            calculationList = null;
        }
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
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(name);
        dest.writeString(password);
        dest.writeString(state);
        if (calculationList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(calculationList);
        }
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
        public Session createFromParcel(final Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(final int size) {
            return new Session[size];
        }
    };
}
