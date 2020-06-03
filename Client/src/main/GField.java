package main;

import java.io.Serializable;
import java.util.ArrayList;

public class GField implements Serializable {
    public String count;
    public String names;
    public ArrayList<Player> players;

    public GField(String count,String names){
        this.players = new ArrayList<Player>();
        this.count = count;
        this.names = names;
    }

    public String getCount() {
        return count;
    }
    public String getNames() {
        return names;
    }
}