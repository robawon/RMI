package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TimeZoneConverter extends Remote {
    String convertTime(String time, String fromZone, String toZone) throws RemoteException;
}