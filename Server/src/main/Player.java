package main;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    public String name;
    public ArrayList<Card> hand;
    public boolean isTurn;

    public Player(String name, ArrayList<Card> hand, boolean isTurn){
        this.hand = hand;
        this.name = name;
        this.isTurn = isTurn;
    }
}
