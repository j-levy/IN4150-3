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
        int N = 10; // number of processes
        int f = 1;
        //int f = (int) Math.floor(Math.round(Math.random()*((N-1)/5))); // unsure, it should get f < 5N (strictly)
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
                        (ByzantineServerInterface) UnicastRemoteObject.exportObject(new ByzantineServerImplementation(N, f, i, isTraitor), 10000 + i);
                ByzanceRegistry[i] = LocateRegistry.createRegistry(10000+i);
                ByzanceRegistry[i].rebind("Receive", ByzantineSkeleton[i]);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Registry[] reg = new Registry[N];
        ByzantineServerInterface[] stub = new ByzantineServerInterface[N];

        for (int i = 0; i < N; i++)
        {
            reg[i] = LocateRegistry.getRegistry(10000 + i);
            stub[i] = (ByzantineServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (10000 + i) + "/Receive");
        }

        for (int i = 0; i < N; i++)
            stub[i].connect();

        Thread[] P = new Thread[N];
        for (int i = 0; i < N; i++){
            P[i] = new Thread(new Launcher(stub[i]));
            P[i].start();
        }

    }
}
