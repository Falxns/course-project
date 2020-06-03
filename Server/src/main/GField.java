package main;

import java.io.Serializable;
import java.util.ArrayList;

public class GField implements Serializable {
    public String count;
    public String names;
    public ArrayList<Player> players;
    public ArrayList<Card> deck;
    public ArrayList<Card> retreat;
    public int eightVal;
    public int jackVal;
    public String action;

    public GField(String count,String names, ArrayList<Integer> deck){
        this.eightVal = 0;
        this.jackVal = 0;
        this.retreat = new ArrayList<Card>();
        this.players = new ArrayList<Player>();
        this.count = count;
        this.names = names;
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int num : deck) {
            int value = 6 + (num - 1) % 9;
            int lear = 0;
            if (num < 10) {
                lear = 1;
            }
            if (num > 9 && num < 19) {
                lear = 2;
            }
            if (num > 18 && num < 28) {
                lear = 3;
            }
            if (num > 27) {
                lear = 4;
            }
            Card card = new Card(value, lear);
            cards.add(card);
        }
        this.deck = cards;
    }

    public String getCount() {
        return count;
    }
    public String getNames() {
        return names;
    }
}