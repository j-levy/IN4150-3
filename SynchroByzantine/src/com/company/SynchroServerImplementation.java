package com.company;


import java.rmi.Remote;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SynchroServerImplementation implements SynchroServerInterface {


    private int N, countdown;
    private Registry[] reg;
    private ByzantineServerInterface[] stub;


    public SynchroServerImplementation(int N) throws RemoteException, NotBoundException, MalformedURLException {
        this.N = N;
        this.countdown = N;
        reg = new Registry[N];
        stub = new ByzantineServerInterface[N];
        System.err.println("Synchro server started. Countdown : "+countdown);

    }

    public synchronized void down(int res) throws RemoteException, NotBoundException, MalformedURLException {
        countdown--;
        System.err.print(countdown+", ");
        System.err.println("Finished with value "+res);
        if (countdown <= 0 )
        {
            System.exit(0);
        }
    }
}
