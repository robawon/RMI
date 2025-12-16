package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeZoneConverterImpl extends UnicastRemoteObject implements TimeZoneConverter {

    protected TimeZoneConverterImpl() throws RemoteException {
        super();
    }

    @Override
    public String convertTime(String time, String fromZone, String toZone) throws RemoteException {
        if (time == null || time.trim().isEmpty()) {
            throw new RemoteException("Time string cannot be null or empty");
        }
        if (fromZone == null || fromZone.trim().isEmpty()) {
            throw new RemoteException("From zone cannot be null or empty");
        }
        if (toZone == null || toZone.trim().isEmpty()) {
            throw new RemoteException("To zone cannot be null or empty");
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(time, formatter);
            ZonedDateTime fromZonedDateTime = localDateTime.atZone(ZoneId.of(fromZone));
            ZonedDateTime toZonedDateTime = fromZonedDateTime.withZoneSameInstant(ZoneId.of(toZone));
            return toZonedDateTime.format(formatter);
        } catch (Exception e) {
            throw new RemoteException("Error converting time: " + e.getMessage());
        }
    }
}