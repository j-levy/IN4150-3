package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SynchroServerInterface  extends Remote {
    public void down() throws RemoteException, NotBoundException, MalformedURLException;
    public void main_synchro() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException;
    public void main_startup() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException;
}
