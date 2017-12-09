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
    private static Remote[] remoteStub;


    public static void main(String[] args) throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {
	// write your code here
        int N = 100; // number of processes
        int f = 15;
        //int f = (int) Math.floor(Math.round(Math.random()*((N-1)/5)));

        /*
        Failure types :
        00000001 (1) : Don't send any message at all (domines all)
        00000010 (2) : Flip a coin and send a message if heads (recess in front of 001)
        00000100 (4) : Send a random value instead of the real value
        00001000 (8) : Reverse the value (0 => 1, 1 => 0, -1 => 0 or 1)
        00010000 (16): Put agnostic value (-1) on Proposal
         */
        byte failureType = (byte) 4;


        System.out.print("Traitors behaviour : ");
        if ((failureType & 4) == 4) {
            // randomize the value
            System.out.print("Randomize value, ");
        } else if ((failureType & 8) == 8) {
            // reverse the value, once
            System.out.print("Reverse value, ");
        }

        if ((failureType & 16) == 16) {
            // put agnostic. Can actually be combined with other failures that would affect Notifies !!
            System.out.print("put agnostic on P, ");
        }

        if ((failureType & 1) == 1)
        {
            // do nothing
            System.out.print("Never send");
        } else if ((failureType & 2) == 2) {
            // sometimes send (probability 0.5)
            System.out.print("Sometimes send (proba. 0.5)");
        } else {
            // No "send-type" errors : send it.
            System.out.print("Always send");
        }


        System.out.println("\nN = "+N+", f = "+f);


        // Creating ang lauching synchro server
        SynchroServerInterface skeleton = (SynchroServerInterface) UnicastRemoteObject.exportObject(new SynchroServerImplementation(N), 20000);

        Registry registry;
        registry = LocateRegistry.createRegistry(20000);
        registry.rebind("Synchro", (Remote) skeleton);

        Thread.sleep(1000);

        ByzantineServerInterface[] ByzantineSkeleton = new ByzantineServerInterface[N];
        Remote[] remoteStub = new Remote[N];
        Registry[] ByzanceRegistry = new Registry[N];

        //Start the nodes
        for(int i = 0; i < N; i++)
        {
            try {
                // ByzantineServerImplementation(Number of Byzantines, own ID, isTraitor (first "f" ones are, next aren't)
                boolean isTraitor = i < f;
                ByzantineSkeleton[i] = new ByzantineServerImplementation(N, f, i, isTraitor, failureType);
                remoteStub[i] =
                        (ByzantineServerInterface) UnicastRemoteObject.exportObject(ByzantineSkeleton[i], 10000 + i);
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
            System.err.println("Connecting "+i);
            stub[i].connect();
        }

        Thread[] P = new Thread[N];
        for (int i = 0; i < N; i++){
            P[i] = new Thread(new Launcher(stub[i]));
            P[i].start();
        }

    }
}
