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

    private Registry[] regs;
    private ByzantineServerInterface[] stub;

    private Registry syncreg;
    private SynchroServerInterface syncstub;

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

    public int main_proc() throws RemoteException, InterruptedException {
        while(true)
        {
            
            Message m = new Message('N', round, value);
            //System.out.println(id+" : broadcasting");
            broadcast(m);
            //System.out.println(id+" : broadcast finished");
            while(proceedN.get(round) < N-f) {
            }

            //System.out.println(id+" : enough messages received");
            int count0 =0;
            int count1 =0;
            for (int i =0; i < Notifies.size(); i++)
            {
                if(Notifies.get(i).getRound() == round && Notifies.get(i).getvalue() == 0)
                {
                    count0++;
                } else if(Notifies.get(i).getRound() == round && Notifies.get(i).getvalue() == 1)
                {
                    count1++;
                }
            }
            if (count0 > ((N+f)/2))
            {
                broadcast(new Message('P', round, 0));
            }else if(count1 > ((N+f)/2)){
                broadcast(new Message('P', round, 1));
            }else{
                broadcast(new Message('P', round, -1));
            }


            if (isDecided)
            {
                return decidedValue;
            }else{
                while(proceedP.get(round) < (N-f)) {
                }
            }

            count0 = 0;
            count1 = 0;
            for (int i =0; i < Proposals.size(); i++)
            {
                if(Proposals.get(i).getRound() == round && Proposals.get(i).getvalue() == 0)
                {
                    count0++;
                } else if(Proposals.get(i).getRound() == round && Proposals.get(i).getvalue() == 1)
                {
                    count1++;
                }
            }
            if (count0 > f)
            {
                value = 0;
                if(count0 > (3*f))
                {
                    System.out.flush();
                    decidedValue = 0;
                    isDecided = true;
                    System.out.println(id + " : value = "+value+" ; isDecided = " +isDecided+" ; decided value = "+decidedValue);
                }
            }else if(count1 > f){
                value = 1;
                if(count1 > (3*f))
                {
                    System.out.flush();
                    decidedValue = 1;
                    isDecided = true;
                    System.out.println(id + " : value = "+value+" ; isDecided = " +isDecided+" ; decided value = "+decidedValue);
                }
            }else{
                value = (int) Math.round(Math.random());
                System.out.println(id + " proceedN = "+proceedN);
                System.out.println(id + " proceedP = "+proceedP);
                round++;
            }
    }
}


    public synchronized void receive(Message m) throws RemoteException {
        try{
            Thread.sleep((int) Math.round(Math.random())*100);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        switch(m.getType()) {
            case 'P' :
                Proposals.add(m);
                if (m.getRound() >= proceedP.size())
                    proceedP.SparseAdd(m.getRound(), 0);

                proceedP.set(m.getRound(), proceedP.get(m.getRound())+1) ;

                /*proceedP (index = round number, starts at 0)
                    |_|0|_|_|
                    |_|1|_|_|
                */

                //System.out.println(id+ " receives proposal");
            break;
            case 'N' :
                Notifies.add(m);
                if (m.getRound() >= proceedN.size())
                    proceedN.SparseAdd(m.getRound(), 0);

                proceedN.set(m.getRound(), proceedN.get(m.getRound())+1) ;
                //System.out.println(id+ " receives notify");
            break;
            default:
            break;
        }
    }


    public void broadcast(Message m1) throws RemoteException, InterruptedException {
        int shouldIsend = 1;
        boolean isSending = true;
        for (int i = 0; i < N; i++)
        {
            if (isTraitor)
            {
                if ((failureType & 0b00010000) == 0b00010000)
                {
                    m1.agnosticValue();
                }
                if ((failureType & 0b00001000) == 0b00001000)
                {
                    m1.reverseValue();
                }
                if ((failureType & 0b00000100) == 0b00000100)
                {
                    m1.randValue();
                }
                if ((failureType & 0b00000010) == 0b00000010)
                {
                    shouldIsend = (int) Math.round(Math.random());
                }
                if ((failureType & 0b00000001) == 0b00000001)
                {
                    shouldIsend = 0;
                }
            }
            switch(shouldIsend) {
                case 0:
                    isSending = false;
                    break;
                case 1:
                    isSending = true;
                    break;
                default:
                    isSending = true;
                    break;
            }

            if (isSending)
                stub[i].receive(m1);
        }
    }

}
