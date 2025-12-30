package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TimeZoneServer {
    public static void main(String[] args) {
        try {
            // Ensure RMI stubs advertise the loopback address so clients
            // connecting to localhost can reach the exported remote object.
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");

            TimeZoneConverterImpl obj = new TimeZoneConverterImpl();

            // Use an existing registry if one is already running, otherwise create one.
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(1099);
                // Test registry by invoking a method; throws exception if not available
                registry.list();
                System.out.println("Found existing rmiregistry on port 1099, using it.");
            } catch (Exception e) {
                registry = LocateRegistry.createRegistry(1099);
                System.out.println("Created new rmiregistry on port 1099.");
            }

            registry.rebind("TimeZoneConverter", obj);
            System.out.println("TimeZone Converter Server is ready");
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