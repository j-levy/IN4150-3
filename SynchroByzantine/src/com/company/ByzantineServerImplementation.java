package com.company;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class ByzantineServerImplementation implements ByzantineServerInterface {

    private int N, f, id, value, round;
    private boolean isTraitor;
    private byte failureType;

    private boolean isDecided;
    private int decidedValue;

    private static Registry[] regs;
    private static ByzantineServerInterface[] stub;

    private static Registry syncreg;
    private static SynchroServerInterface syncstub;

    private volatile SparseArrayList proceedN, proceedP;
    private volatile ArrayList<Message> Notifies, Proposals;
    Random coin;

    public ByzantineServerImplementation(int remoteN, int f, int remoteID, boolean remoteIsTraitor, byte remoteFailureType) throws RemoteException, NotBoundException, MalformedURLException {

        this.coin = new Random();

        this.N = remoteN;
        this.f = f;
        this.id = remoteID;
        this.value = (int) Math.round(Math.random());
        this.isTraitor = remoteIsTraitor;
        this.round = 0;
        this.Proposals = new ArrayList<Message>();
        this.Notifies = new ArrayList<Message>();
        this.proceedN = new SparseArrayList();
        this.proceedP = new SparseArrayList();
        this.isDecided = false;
        this.decidedValue = -1;

        this.failureType = remoteFailureType;

        regs = new Registry[N];
        stub = new ByzantineServerInterface[N];

        syncreg = LocateRegistry.getRegistry(20000);
        syncstub = (SynchroServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (20000) + "/Synchro");

    }

    public void connect() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        for (int i = 0; i < N; i++)
        {
                regs[i] = LocateRegistry.getRegistry(10000+i);
                stub[i] = (ByzantineServerInterface) Naming.lookup("rmi://localhost:" + (10000+i) + "/Receive");
        }
    }

    public int main_proc() throws RemoteException, InterruptedException, MalformedURLException, NotBoundException {
        while(true)
        {
            Message m = new Message('N', round, value);
            broadcast(m);
            while(proceedN.SparseGet(round) < N-f) {
            }

            //System.out.println(id+" : enough messages received");
            int count0 =0;
            int count1 =0;
            for (int i = 0; i < Notifies.size(); i++)
            {
                if(Notifies.get(i).getRound() == round && Notifies.get(i).getvalue() == 0)
                {
                    count0++;
                } else if(Notifies.get(i).getRound() == round && Notifies.get(i).getvalue() == 1)
                {
                    count1++;
                }
            }
            int toSend = -1;

            if (count0 > ((N+f)/2.0))
            {
                toSend = 0;
            }else if(count1 > ((N+f)/2.0))
            {
                toSend = 1;
            }else
            {
                toSend = -1;
            }
            broadcast(new Message('P', round, toSend));


            if (isDecided)
            {
                System.out.println("PID : "+id+" decided on value : "+value+" at round "+round);
                syncstub.down(decidedValue);
                break;
            }

            while(proceedP.SparseGet(round) < (N-f)) {
            }


            count0 = 0;
            count1 = 0;
            for (Message Proposal : Proposals) {
                if (Proposal.getRound() == round && Proposal.getvalue() == 0) {
                    count0++;
                } else if (Proposal.getRound() == round && Proposal.getvalue() == 1) {
                    count1++;
                }
            }

            if (count0 > f || count1 > f) {
                value = (count0 > f) ? 0 : 1;
                if ((value == 0 && count0 > 3*f) || (value == 1 && count1 > 3*f)) {
                    decidedValue = value;
                    isDecided = true;
                }
            }else{
                value = (int) Math.round(Math.random());
            }
            System.out.println(id+"  finished round "+round+" with value "+value+" and isDecided "+isDecided);
            round++;
            //System.out.println(id + " proceedN = "+proceedN);
            //System.out.println(id + " proceedP = "+proceedP);
        }
        return 0;
}


    public synchronized void receive(Message m) throws RemoteException {
        try{
            Thread.sleep((int) Math.round(Math.random())*10);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        switch(m.getType()) {
            case 'P' :
                if (proceedP.SparseGet(m.getRound()) < N-f)
                {
                    Proposals.add(m);
                    if (m.getRound() >= proceedP.size())
                        proceedP.SparseAdd(m.getRound(), 0);

                    proceedP.set(m.getRound(), proceedP.get(m.getRound())+1) ;

                /*proceedP (index = round number, starts at 0)
                    |_|0|_|_|
                    |_|1|_|_|
                */

                }
            break;
            case 'N' :
                if (proceedN.SparseGet(m.getRound()) < N-f)
                {
                    Notifies.add(m);
                    if (m.getRound() >= proceedN.size())
                        proceedN.SparseAdd(m.getRound(), 0);

                    proceedN.set(m.getRound(), proceedN.get(m.getRound())+1) ;
                }
            break;
            default:
            break;
        }
    }


    public void broadcast(Message m1) throws RemoteException, InterruptedException {
        Message m2 = new Message(m1.getType(), m1.getRound(), m1.getvalue());
        for (int i = 0; i < N; i++)
        {
            if (isTraitor)
            {
                m2.randValue();
                stub[i].receive(m2);
            } else {
                stub[i].receive(m2);
            }
        }
    }

}
