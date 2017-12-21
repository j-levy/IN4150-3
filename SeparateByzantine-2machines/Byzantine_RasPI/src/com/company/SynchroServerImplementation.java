package com.company;


import java.rmi.Remote;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class SynchroServerImplementation implements SynchroServerInterface {


    private int N, countdown;
    private static ArrayList<Integer> results;
    private static Registry[] reg;
    private ByzantineServerInterface[] stub;
    private static final String PC_IP = "145.94.142.70";

    public SynchroServerImplementation(int N) throws RemoteException, NotBoundException, MalformedURLException {
        this.N = N;
        this.countdown = N;
        this.results = new ArrayList<Integer>();
        reg = new Registry[N];
        stub = new ByzantineServerInterface[N];
        System.err.println("Synchro server started. Countdown : "+countdown);

    }

    public synchronized void down(int res) throws RemoteException, NotBoundException, MalformedURLException {
        countdown--;
        results.add(res);
        //System.err.print(countdown+", ");
        //System.err.println("Finished with value "+res);
        if (countdown <= 0 )
        {
            System.err.println("Results :\n"+results);
            int checksum = 0;
            for (int i = 0; i < results.size(); i++)
                checksum += results.get(i);

            System.err.println("Checksum = "+checksum%N+". (Checksum should be 0, meaning no error.)");
            System.exit(0);
        }
    }
}
