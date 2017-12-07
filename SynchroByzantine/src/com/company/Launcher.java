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
            int res = stub.main_proc();
            System.err.println("Finished with value "+res);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
