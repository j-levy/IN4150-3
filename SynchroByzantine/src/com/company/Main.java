package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {

    static Registry[] reg;
    static ByzantineServerInterface[] stub;

    public static void main(String[] args) throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {
	// write your code here
        int N = 20; // number of processes
        int f = 3;
        //int f = (int) Math.floor(Math.round(Math.random()*((N-1)/5)));

        /*
        Failure types :
        00000001 : Don't send any message at all (domines all)
        00000010 : Flip a coin and send a message if heads (recess in front of 001)
        00000100 : Send a random value instead of the real value
        00001000 : Reverse the value (0 => 1, 1 => 0, -1 => 0 or 1)
        00010000 : Put agnostic value (-1) on Proposal
         */
        byte failureType = (byte) 0b00000010;

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
                        (ByzantineServerInterface) UnicastRemoteObject.exportObject(new ByzantineServerImplementation(N, f, i, isTraitor, failureType), 10000 + i);
                ByzanceRegistry[i] = LocateRegistry.createRegistry(10000+i);
                ByzanceRegistry[i].rebind("Receive", ByzantineSkeleton[i]);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        reg = new Registry[N];
        stub = new ByzantineServerInterface[N];


        for (int i = 0; i < N; i++)
        {
            reg[i] = LocateRegistry.getRegistry(10000 + i);
            stub[i] = (ByzantineServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (10000 + i) + "/Receive");
        }

        for (int i = 0; i < N; i++) {
            stub[i].connect();
        }

        Thread[] P = new Thread[N];
        for (int i = 0; i < N; i++){
            P[i] = new Thread(new Launcher(stub[i]));
            P[i].start();
        }

    }
}
