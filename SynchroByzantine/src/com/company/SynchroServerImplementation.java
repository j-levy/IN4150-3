package com.company;


import java.rmi.Remote;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SynchroServerImplementation implements SynchroServerInterface {


    private int N, countdown;
    private Registry[] reg;
    private ByzantineServerInterface[] stub;


    public SynchroServerImplementation(int N) throws RemoteException, NotBoundException, MalformedURLException {
        this.N = N;
        this.countdown = N;
        reg = new Registry[N];
        stub = new ByzantineServerInterface[N];
        System.err.println("Synchro server started. Countdown : "+countdown);

    }

    public void down() throws RemoteException, NotBoundException, MalformedURLException {
        countdown--;
        System.err.println(countdown+", ");
        System.err.flush();
        if (countdown <= 0 )
        {
            // create a separate thread to launch the startup. Another thread is needed to let the node exit the function call.
            Thread Launch = new Thread(new SynchroLauncher());
            Launch.start();
        }
    }


    public void main_startup() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        for (int i = 0; i < N; i++)
        {
            reg[i] = LocateRegistry.getRegistry(10000 + i);
            stub[i] = (ByzantineServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (10000 + i) + "/Receive");
        }
        for (int i = 0; i < N; i++)
            stub[i].connect();

        main_synchro();
    }


    public void main_synchro() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        System.out.println("Launcher started !");
        Thread[] P = new Thread[N];
        for (int i = 0; i < N; i++){
            P[i] = new Thread(new Launcher(stub[i]));
            P[i].start();
        }
    }
}
