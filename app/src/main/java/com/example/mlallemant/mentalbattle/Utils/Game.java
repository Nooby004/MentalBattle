package com.example.mlallemant.mentalbattle.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by m.lallemant on 13/10/2017.
 */

public class Game {

    private final static int NB_CALCULATION = 50;

    private String id;
    private Player player1;
    private Player player2;
    private List<Calculation> calculationList;

    public Game(String id, Player player1, Player player2){
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        calculationList = generateCalculationList();
    }

    public Game(String id, Player player1, Player player2, List<Calculation> calculationList){
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.calculationList = calculationList;
    }

    private List<Calculation> generateCalculationList(){

        List<Calculation> calculationList_ = new ArrayList<>();

        for (int i=0; i<NB_CALCULATION; i++){
            calculationList_.add(new Calculation());
        }
        return  calculationList_;
    }

    public Game(){
        super();
    }

    public List<Calculation> getCalculationList() {
        return calculationList;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public String getId() {
        return id;
    }
}
