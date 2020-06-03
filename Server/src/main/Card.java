package main;

import java.io.Serializable;

public class Card implements Serializable {
    public int value;
    public int lear;

    public Card(int value, int lear){
        this.value = value;
        this.lear = lear;
    }
}

