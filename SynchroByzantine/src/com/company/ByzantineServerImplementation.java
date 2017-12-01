package com.company;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class ByzantineServerImplementation implements ByzantineServerInterface {

    private int N, f, id, value, round;
    private boolean isTraitor;

    private boolean isDecided;
    private int decidedValue;

    private Registry[] regs;
    private ByzantineServerInterface[] stub;

    private Registry syncreg;
    private SynchroServerInterface syncstub;

    private volatile int proceedN, proceedP;
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
        this.proceedN = 0;
        this.proceedP = 0;
        this.isDecided = false;
        this.decidedValue = -1;

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
            while(proceedN < N-f);
            
            System.out.println(id+" : enough messages received");
            int count0 =0;
            int count1 =0;
            for (int i =0; i < proceedN; i++)
            {
                if(Notifies.get(i).getvalue() == 0)
                {
                    count0++;
                }else if(Notifies.get(i).getvalue() == 1)
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
                while(proceedP < (N-f));
            }

            count0 = 0;
            count1 = 0;
            for (int i =0; i < proceedP; i++)
            {
                if(Proposals.get(i).getvalue() == 0)
                {
                    count0++;
                }else if(Proposals.get(i).getvalue() == 1)
                {
                    count1++;
                }
            }
            if (count0 > f)
            {
                value = 0;
                if(count0 > (3*f))
                {
                    decidedValue = 0;
                    isDecided = true;
                }
            }else if(count1 > f){
                value = 1;
                if(count1 > (3*f))
                {
                    decidedValue = 1;
                    isDecided = true;
                }
            }else{
                value = (int) Math.round(Math.random());
                round++;
            }
            System.out.println(id + " : value = "+value+" ; isDecided = " +isDecided+" ; decided value = "+decidedValue);
    }
}


    public synchronized void receive(Message m) throws RemoteException {
        switch(m.getType()) {
            case 'P' :
                Proposals.add(m);
                proceedP++;
                //System.out.println(id+ " receives proposal");
            break;
            case 'N' :
                Notifies.add(m);
                proceedN++;
                //System.out.println(id+ " receives notify");
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
