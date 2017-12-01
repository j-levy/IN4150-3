package com.company;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class ByzantineServerImplementation implements ByzantineServerInterface {

    private int N, f, id, value, round;
    private boolean isTraitor;

    private Registry[] regs;
    private ByzantineServerInterface[] stub;

    private Registry syncreg;
    private SynchroServerInterface syncstub;


    private volatile ArrayList<Message> Notifies, Proposals;

    public ByzantineServerImplementation(int remoteN, int f, int remoteID, boolean remoteIsTraitor) throws RemoteException, NotBoundException, MalformedURLException {
        this.N = remoteN;
        this.f = f;
        this.id = remoteID;
        this.value = (int) Math.round(Math.random());
        this.isTraitor = remoteIsTraitor;
        this.round = 1;
        this.Proposals = new ArrayList<Message>();
        this.Notifies = new ArrayList<Message>();

        regs = new Registry[N];
        stub = new ByzantineServerInterface[N];
        /*
        syncreg = LocateRegistry.getRegistry(20000);
        syncstub = (SynchroServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (20000) + "/Synchro");
        syncstub.down();
        */
    }

    public void connect() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        for (int i = 0; i < N; i++)
        {
                regs[i] = LocateRegistry.getRegistry(10000+i);
                stub[i] = (ByzantineServerInterface) Naming.lookup("rmi://localhost:" + (10000+i) + "/Receive");
        }
    }

    public void main_proc() throws RemoteException, InterruptedException {
        Message m = new Message('N', round, value);
        System.out.println(id+" : broadcasting");
        broadcast(m);
        System.out.println(id+" : broadcast finished");
        while(Notifies.size() < N-f);
        System.out.println(id+" : enough messages received");
    }


    public void receive(Message m) throws RemoteException {
        switch(m.getType()) {
            case 'P' :
                Proposals.add(m);
                System.out.println(id+ " recieves proposal");
            break;
            case 'N' :
                Notifies.add(m);
                System.out.println(id+ " recieves notify");
            break;
            default:
            break;
        }
    }


    public void broadcast(Message m1) throws RemoteException, InterruptedException {
        int j = 0;
        for (int i = 0; i < N; i++)
        {
            stub[i].receive(m1);
        }
    }

}
