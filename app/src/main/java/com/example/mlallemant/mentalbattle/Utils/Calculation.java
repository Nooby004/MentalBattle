package com.example.mlallemant.mentalbattle.Utils;

import java.util.Random;

/**
 * Created by m.lallemant on 19/10/2017.
 */

public class Calculation {

    private static final int MAX_VALUE_ADDITION = 1500;
    private static final int MAX_RANGE = 1000;

    private Integer result;
    private String calculText;

    public Calculation()
    {
        Integer A, B;

        A = getRandomNumber();
        B = getRandomNumber();
        result = calculResult(A, B);
        while (result > MAX_VALUE_ADDITION && result < -MAX_VALUE_ADDITION){
            A = getRandomNumber();
            B = getRandomNumber();
            result = calculResult(A, B);
        }

        calculText = makeString(A, B);
    }

    private Integer getRandomNumber() {
        Integer a;

        Random random=new Random();
        a=(random.nextInt(MAX_RANGE + 1 + MAX_RANGE) - MAX_RANGE);

        return a;
    }

    private Integer calculResult(Integer a, Integer b){
        return a+b;
    }

    private String makeString(Integer a, Integer b){
        String text, sign;

        if (b < 0){
            sign = "-";
            b *= -1;
        }else{
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
}
