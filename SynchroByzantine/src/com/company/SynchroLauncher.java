package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class SynchroLauncher implements Runnable {
    @Override
    public void run() {
        Registry reg2;
        SynchroServerInterface syncstub;

        try {
            reg2 = LocateRegistry.getRegistry(20000);
            syncstub = (SynchroServerInterface) java.rmi.Naming.lookup("rmi://localhost:" + (20000) + "/Synchro");
            syncstub.main_startup();

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
