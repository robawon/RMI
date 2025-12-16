package client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.TimeZoneConverter;
import java.rmi.Naming;
import java.util.HashMap;
import java.util.Map;

public class TimeZoneClient extends Application {

    private TimeZoneConverter converter;
    private Map<String, String> zoneMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        try {
            try {
                converter = (TimeZoneConverter) Naming.lookup("rmi://localhost/TimeZoneConverter");
            } catch (Exception lookupEx) {
                // If RMI lookup fails (server not running), fall back to local implementation
                // so the UI can still function for demo/testing purposes.
                converter = new server.TimeZoneConverterImpl();
            }

            zoneMap.put("GMT", "GMT");
            zoneMap.put("BST", "Europe/London");
            zoneMap.put("CET", "Europe/Paris");
            zoneMap.put("CEST", "Europe/Paris");
            zoneMap.put("EET", "Europe/Bucharest");
            zoneMap.put("EEST", "Europe/Bucharest");
            zoneMap.put("AST", "America/Halifax");
            zoneMap.put("ADT", "America/Halifax");
            zoneMap.put("IST", "Asia/Kolkata");
            zoneMap.put("CST", "Asia/Shanghai");
            zoneMap.put("JST", "Asia/Tokyo");
            zoneMap.put("AEDT", "Australia/Sydney");
            zoneMap.put("ACST", "Australia/Adelaide");
            zoneMap.put("AKST", "America/Anchorage");
            zoneMap.put("HST", "Pacific/Honolulu");

            VBox root = new VBox(10);
            root.setPadding(new Insets(20));

            ComboBox<String> timeComboBox = new ComboBox<>();
            timeComboBox.getItems().addAll(
                    "12:00 AM", "1:00 AM", "2:00 AM", "3:00 AM", "4:00 AM", "5:00 AM",
                    "6:00 AM", "7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM",
                    "12:00 PM", "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM",
                    "6:00 PM", "7:00 PM", "8:00 PM", "9:00 PM", "10:00 PM", "11:00 PM");
            timeComboBox.setValue("12:00 AM");

            ChoiceBox<String> fromZoneChoiceBox = new ChoiceBox<>();
            fromZoneChoiceBox.getItems().addAll(
                    "GMT: Greenwich Mean Time (UTC+0)",
                    "BST: British Summer Time (UTC+1, during daylight saving)",
                    "CET: Central European Time (UTC+1)",
                    "CEST: Central European Summer Time (UTC+2, during daylight saving)",
                    "EET: Eastern European Time (UTC+2)",
                    "EEST: Eastern European Summer Time (UTC+3, during daylight saving)",
                    "AST: Atlantic Standard Time (UTC-4)",
                    "ADT: Atlantic Daylight Time (UTC-3, during daylight saving)",
                    "IST: Indian Standard Time (UTC+5:30)",
                    "CST: China Standard Time (UTC+8)",
                    "JST: Japan Standard Time (UTC+9)",
                    "AEDT: Australian Eastern Daylight Time (UTC+11, during daylight saving)",
                    "ACST: Australian Central Standard Time (UTC+9:30)",
                    "AKST: Alaska Standard Time (UTC-9)",
                    "HST: Hawaii-Aleutian Standard Time (UTC-10)");
            fromZoneChoiceBox.setValue("GMT: Greenwich Mean Time (UTC+0)");

            ChoiceBox<String> toZoneChoiceBox = new ChoiceBox<>();
            toZoneChoiceBox.getItems().addAll(
                    "GMT: Greenwich Mean Time (UTC+0)",
                    "BST: British Summer Time (UTC+1, during daylight saving)",
                    "CET: Central European Time (UTC+1)",
                    "CEST: Central European Summer Time (UTC+2, during daylight saving)",
                    "EET: Eastern European Time (UTC+2)",
                    "EEST: Eastern European Summer Time (UTC+3, during daylight saving)",
                    "AST: Atlantic Standard Time (UTC-4)",
                    "ADT: Atlantic Daylight Time (UTC-3, during daylight saving)",
                    "IST: Indian Standard Time (UTC+5:30)",
                    "CST: China Standard Time (UTC+8)",
                    "JST: Japan Standard Time (UTC+9)",
                    "AEDT: Australian Eastern Daylight Time (UTC+11, during daylight saving)",
                    "ACST: Australian Central Standard Time (UTC+9:30)",
                    "AKST: Alaska Standard Time (UTC-9)",
                    "HST: Hawaii-Aleutian Standard Time (UTC-10)");
            toZoneChoiceBox.setValue("GMT: Greenwich Mean Time (UTC+0)");

            Label resultLabel = new Label();

            // Method to perform conversion
            Runnable performConversion = () -> {
                try {
                    String selectedTime = timeComboBox.getValue();
                    String time = convertTo24HourFormat(selectedTime);
                    String fromZoneFull = fromZoneChoiceBox.getValue();
                    String toZoneFull = toZoneChoiceBox.getValue();
                    String fromZoneAbbr = fromZoneFull.split(":")[0];
                    String toZoneAbbr = toZoneFull.split(":")[0];
                    String fromZone = zoneMap.get(fromZoneAbbr);
                    String toZone = zoneMap.get(toZoneAbbr);
                    String result = converter.convertTime(time, fromZone, toZone);
                    String timeOnly = result.split(" ")[1]; // Extract time part after space
                    resultLabel.setText("Converted Time: " + timeOnly);
                } catch (Exception ex) {
                    resultLabel.setText("Error: " + ex.getMessage());
                }
            };

            // Add listeners for real-time conversion
            timeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> performConversion.run());
            fromZoneChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> performConversion.run());
            toZoneChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> performConversion.run());

            // Perform initial conversion
            performConversion.run();

            root.getChildren().addAll(
                    new Label("Time Zone Converter"),
                    timeComboBox,
                    fromZoneChoiceBox,
                    toZoneChoiceBox,
                    resultLabel);

            Scene scene = new Scene(root, 400, 300);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            primaryStage.setTitle("Time Zone Converter Client");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertTo24HourFormat(String time12Hour) {
        String[] parts = time12Hour.split(" ");
        String timePart = parts[0];
        String amPm = parts[1];
        String[] hourMin = timePart.split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int minute = Integer.parseInt(hourMin[1]);

        if (amPm.equals("PM") && hour != 12) {
            hour += 12;
        } else if (amPm.equals("AM") && hour == 12) {
            hour = 0;
        }

        return String.format("2023-01-01 %02d:%02d:00", hour, minute);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
