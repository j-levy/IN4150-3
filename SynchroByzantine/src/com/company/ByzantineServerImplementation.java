package com.company;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class ByzantineServerImplementation implements ByzantineServerInterface{

    private int N, id, value, round;
    private boolean isTraitor;

    private Registry[] regs;
    private ByzantineServerInterface[] stub;

    private Registry syncreg;
    private SynchroServerInterface syncstub;


    private ArrayList<Message> Notifies, Proposals;

    public ByzantineServerImplementation(int remoteN, int remoteID, boolean remoteIsTraitor) throws RemoteException, NotBoundException, MalformedURLException {
        this.N = remoteN;
        this.id = remoteID;
        this.value = (int) Math.round(Math.random());
        this.isTraitor = remoteIsTraitor;
        this.round = 1;
        this.Proposals = new ArrayList<Message>();
        this.Notifies = new ArrayList<Message>();

        regs = new Registry[N];
        stub = new ByzantineServerInterface[N];

        syncreg = LocateRegistry.getRegistry(20000);
        syncstub = (SynchroServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (20000) + "/Synchro");
        syncstub.down();
    }

    public void connect() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        for (int i = 0; i < N; i++)
        {
            //if (i != id) {
                regs[i] = LocateRegistry.getRegistry(10000+i);
                stub[i] = (ByzantineServerInterface) Naming.lookup("rmi://localhost:" + (10000+i) + "/Receive");
            //}
        }
    }

    public void main_proc() throws RemoteException, InterruptedException {
        Message m = new Message('N', round, value);

        int i = 0;
        while(true) {
            broadcast(m);
            while (Notifies.size() <= 5) ;

            System.out.println(id + " recieved all messages");
            Notifies.clear();
        }
    }

    public void receive(Message m) throws RemoteException {
        switch(m.getType()) {
            case 'P' :
                Proposals.add(m);
            break;
            case 'N' :
                Notifies.add(m);
            break;
            default:
            break;
        }
    }


    public void broadcast(Message m1) throws RemoteException, InterruptedException {
        for (int i = 0; i < N; i++)
        {
            Thread.sleep((long) (1000*Math.random()));
            stub[i].receive(m1);
        }
    }

}
