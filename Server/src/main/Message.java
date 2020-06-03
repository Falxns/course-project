package main;

import javafx.collections.ObservableList;
import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    public String msg;
    public ArrayList<GField> gamesArray;

    public Message(String msg, ObservableList<GField> gamesArray){
        this.msg = msg;
        if (gamesArray != null)
            this.gamesArray = new ArrayList<GField>(gamesArray);
        else
            this.gamesArray = null;
    }
}