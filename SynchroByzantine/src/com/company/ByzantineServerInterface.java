package com.company;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ByzantineServerInterface extends Remote {
    public void connect() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException;
    public void receive(Message m) throws RemoteException;
    public void broadcast(Message m1) throws RemoteException, InterruptedException;

    public void main_proc() throws RemoteException, InterruptedException;
}
