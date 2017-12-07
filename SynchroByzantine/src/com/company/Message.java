package com.company;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Random;

public class Message implements Remote, Serializable {
    private char type;
    private int round;
    private int value;
    private Random coin;

    public Message(char type, int round, int value) {
        this.type = type;
        this.round = round;
        this.value = value;
        this.coin = new Random();
    }

    /* message type cannot be changed, so only getters. Setters are built-in modifiers like randValue... */
    public int getRound() { return round; }
    public char getType() { return type; }
    public int getvalue() { return value; }

    public void randValue() {
        this.value = (int) coin.nextInt(1);
        // this.value = (int) Math.round(Math.random());
        //System.err.println("Shuffled !");
    }

    public void reverseValue() {
        switch (value) {
            case 0:
                value = 1;
                break;
            case 1:
                value = 0;
                break;
            case -1:
                randValue();
                break;
        }
    }

    public void agnosticValue() {
        if (type == 'P')
            value = -1;
    }


}
