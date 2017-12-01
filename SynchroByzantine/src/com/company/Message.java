package com.company;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public class Message implements Remote, Serializable {
    private char type;
    private int round;
    private int value;

    public Message(char type, int round, int value) {
        this.type = type;
        this.round = round;
        this.value = value;
    }

    /* message type cannot be changed, so only getters, so setters outside the constructor */

    public int getRound() { return round; }
    public char getType() { return type; }
    public int getvalue() { return value; }
}
