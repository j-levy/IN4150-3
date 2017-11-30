package com.company;

public class Message {
    private char type;
    private int round;
    private boolean value;

    public Message(char type, int round, boolean value) {
        this.type = type;
        this.round = round;
        this.value = value;
    }


    /* message type cannot be changed, so only getters, so setters outside the constructor */
    public char getType() {
        return type;
    }
    public int getRound() {
        return round;
    }
    public boolean getValue(){
        return value;
    }
}
