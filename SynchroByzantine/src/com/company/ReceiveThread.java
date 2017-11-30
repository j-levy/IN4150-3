package com.company;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ReceiveThread implements Runnable {

    Message m;
    Registry regs;
    ByzantineServerInterface stub;

    public ReceiveThread(Message remotem, int port) throws RemoteException, MalformedURLException, NotBoundException {
        this.m = remotem;
        regs = LocateRegistry.getRegistry(10000+port);
        stub = (ByzantineServerInterface) Naming.lookup("rmi://localhost:" + (10000+port) + "/Receive");
    }

    @Override
    public void run() {
        try {
            Thread.sleep((long) (5000*Math.random()));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
