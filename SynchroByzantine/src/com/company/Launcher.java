package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
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
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
