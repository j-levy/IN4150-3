package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SynchroServerInterface  extends Remote {
    public void down(int res) throws RemoteException, NotBoundException, MalformedURLException;
}
