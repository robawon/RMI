package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TimeZoneServer {
    public static void main(String[] args) {
        try {
            TimeZoneConverterImpl obj = new TimeZoneConverterImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("TimeZoneConverter", obj);
            System.out.println("TimeZone Converter Server is ready.");
            // Keep the server process alive so the in-process RMI registry
            // and bound objects remain available to clients.
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ie) {
                // Restore interrupted status and exit
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}