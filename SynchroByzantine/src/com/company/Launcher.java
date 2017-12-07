package com.company;

import java.rmi.RemoteException;

public class Launcher implements Runnable{
    ByzantineServerInterface stub;

    public Launcher(ByzantineServerInterface b) {
        stub = b;
    }


    @Override
    public void run() {
        try {
            stub.main_proc();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
