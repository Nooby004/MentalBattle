package com.example.mlallemant.mentalbattle.Utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

/**
 * Created by m.lallemant on 19/10/2017.
 */

public class Calculation implements Parcelable {

    private static final int MAX_VALUE_ADDITION = 150;
    private static final int MAX_RANGE = 100;

    private Integer result;
    private final String calculText;

    public Calculation() {
        Integer A, B;

        A = getRandomNumber();
        B = getRandomNumber();
        result = calculResult(A, B);
        while (result > MAX_VALUE_ADDITION && result < -MAX_VALUE_ADDITION) {
            A = getRandomNumber();
            B = getRandomNumber();
            result = calculResult(A, B);
        }

        calculText = makeString(A, B);
    }

    private Integer getRandomNumber() {
        final Integer a;

        final Random random = new Random();
        a = (random.nextInt(MAX_RANGE + 1 + MAX_RANGE) - MAX_RANGE);

        return a;
    }

    private Integer calculResult(final Integer a, final Integer b) {
        return a + b;
    }

    private String makeString(final Integer a, Integer b) {
        final String text;
        final String sign;

        if (b < 0) {
            sign = "-";
            b *= -1;
        } else {
            sign = "+";
        }

        text = a + " " + sign + " " + b;
        return text;
    }

    public Integer getResult() {
        return result;
    }

    public String getCalculText() {
        return calculText;
    }

    protected Calculation(final Parcel in) {
        result = in.readByte() == 0x00 ? null : in.readInt();
        calculText = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        if (result == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(result);
        }
        dest.writeString(calculText);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Calculation> CREATOR = new Parcelable.Creator<Calculation>() {
        @Override
        public Calculation createFromParcel(final Parcel in) {
            return new Calculation(in);
        }

        @Override
        public Calculation[] newArray(final int size) {
            return new Calculation[size];
        }
    };
}