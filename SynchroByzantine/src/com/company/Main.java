package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {

    public static void main(String[] args) throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {
	// write your code here
        int N = 5; // number of processes
        int f = (int) Math.floor(Math.round(Math.random()*((N-1)/5))); // unsure, it should get f < 5N (strictly)
        System.out.println("N = "+N+", f = "+f);


        // Creating ang lauching synchro server
        SynchroServerInterface skeleton = (SynchroServerInterface) UnicastRemoteObject.exportObject(new SynchroServerImplementation(N), 20000);

        Registry registry;
        registry = LocateRegistry.createRegistry(20000);
        registry.rebind("Synchro", (Remote) skeleton);

        Thread.sleep(1000);

        ByzantineServerInterface[] ByzantineSkeleton = new ByzantineServerInterface[N];
        Registry[] ByzanceRegistry = new Registry[N];
        //Start the nodes
        for(int i = 0; i < N; i++)
        {
            try {
                // ByzantineServerImplementation(Number of Byzantines, own ID, isTraitor (first "f" ones are, next aren't)
                boolean isTraitor = i < f;
                ByzantineSkeleton[i] =
                        (ByzantineServerInterface) UnicastRemoteObject.exportObject(new ByzantineServerImplementation(N, i, isTraitor), 10000 + i);
                ByzanceRegistry[i] = LocateRegistry.createRegistry(10000+i);
                ByzanceRegistry[i].rebind("Receive", ByzantineSkeleton[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
