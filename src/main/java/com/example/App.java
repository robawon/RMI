package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.TimeZoneConverter;
import java.rmi.Naming;

public class App extends Application {

    private  TimeZoneConverter converter;

    @Override
    public void start(Stage primaryStage) {
        try {
            converter = (TimeZoneConverter) Naming.lookup("rmi://localhost/TimeZoneConverter");

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

            Button convertButton = new Button("Convert");

            Label resultLabel = new Label();

            convertButton.setOnAction(e -> {
                try {
                    String selectedTime = timeComboBox.getValue();
                    String time = convertTo24HourFormat(selectedTime);
                    String fromZoneFull = fromZoneChoiceBox.getValue();
                    String toZoneFull = toZoneChoiceBox.getValue();
                    String fromZone = fromZoneFull.split(":")[0];
                    String toZone = toZoneFull.split(":")[0];
                    String result = converter.convertTime(time, fromZone, toZone);
                    resultLabel.setText("Converted Time: " + result);
                } catch (Exception ex) {
                    resultLabel.setText("Error: " + ex.getMessage());
                }
            });

            root.getChildren().addAll(
                    new Label("Time Zone Converter"),
                    timeComboBox,
                    fromZoneChoiceBox,
                    toZoneChoiceBox,
                    convertButton,
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
