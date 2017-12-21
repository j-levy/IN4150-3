package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Time;

public class Main {

    private static Registry[] reg;
    private static ByzantineServerInterface[] stub;
    private static Remote[] remoteStub;

    private static SynchroServerImplementation TimeLord;
    private static Remote TimeLordStub;

    private static final String PC_IP = "145.94.142.70";

    public static void main(String[] args) throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {


        /*
        The structure to show the algorithm run on multiple machine is quite basic :
        The PC hosts N processes, while the remote Raspberry hosts an N+1-th node.
        Hence, by structure, the Raspberry is never a traitor (because only the first "f" nodes are traitors).
        This demonstrates the algorithm on multiple machines, while keeping the structure simple.
        The TimeLord server, only used for termination, verification and debugging, stays on the PC.
         */

        System.setProperty("java.rmi.server.hostname","yattoz.ddns.net");



        int N = 11; // number of processes
        int f = 1;
        //int f = (int) Math.floor(Math.round(Math.random()*((N-1)/5)));

        /*
        Failure types :
        00000001 (1) : Don't send any message at all (domines all)
        00000010 (2) : Flip a coin and send a message if heads (recess in front of 001)
        00000100 (4) : Send a random value instead of the real value
        00001000 (8) : Reverse the value (0 => 1, 1 => 0, -1 => 0 or 1)
        00010000 (16): Put agnostic value (-1) on Proposal
         */
        byte failureType = (byte) 0b00000100;


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
        /*
        TimeLord = new SynchroServerImplementation(N);
        // Creating ang lauching synchro server
        TimeLordStub = (SynchroServerInterface) UnicastRemoteObject.exportObject(TimeLord, 20000);

        Registry registry;
        registry = LocateRegistry.createRegistry(20000);
        registry.rebind("Synchro", (Remote) TimeLord);
        */
        Thread.sleep(1000);

        ByzantineServerInterface[] ByzantineSkeleton = new ByzantineServerInterface[N];
        Remote[] remoteStub = new Remote[N];
        Registry[] ByzanceRegistry = new Registry[N];

        //Start the last node
        for(int i = N-1; i < N; i++)
        {
            try {
                // ByzantineServerImplementation(Number of Byzantines, own ID, isTraitor (first "f" ones are, next aren't)
                boolean isTraitor = false;
                ByzantineSkeleton[i] = new ByzantineServerImplementation(N, f, i, isTraitor, failureType);
                remoteStub[i] =
                        (ByzantineServerInterface) UnicastRemoteObject.exportObject(ByzantineSkeleton[i], 10000 + i);
                ByzanceRegistry[i] = LocateRegistry.createRegistry(10000+i);
                // You must bind the actual object ByzantineServerImplementation, not the result of UnicastRemoteObject
                // Because the result of UnicastRemoteObject creates a new thread that goes out of scope, then garbage-collected !
                ByzanceRegistry[i].rebind("Receive", ByzantineSkeleton[i]);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("1 node on RaspPi created. Create N-1 nodes on x86 side, then press Enter key to continue...");
        try
        {
            System.in.read();
        }
        catch(Exception e)
        {}
        System.out.println("Continuing.");

        reg = new Registry[N];
        stub = new ByzantineServerInterface[N];


        for (int i = N-1; i < N; i++)
        {
            reg[i] = LocateRegistry.getRegistry(10000 + i);
            stub[i] = (ByzantineServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (10000 + i) + "/Receive");
        }

        for (int i = N-1; i < N; i++) {
            System.err.println("Connecting "+i);
            stub[i].connect();
        }


        // Start last node. Changed to be simpler.
        Thread P;
        P = new Thread(new Launcher(stub[N-1]));
        P.start();


    }
}
