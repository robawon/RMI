package com.example;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class TimeZoneConverter extends Application {

    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();
    private Map<String, String> zoneToApi = new HashMap<>();

    public TimeZoneConverter() {
        zoneToApi.put("GMT", "Etc/GMT");
        zoneToApi.put("BST", "Europe/London");
        zoneToApi.put("CET", "Europe/Paris");
        zoneToApi.put("CEST", "Europe/Paris");
        zoneToApi.put("EET", "Europe/Helsinki");
        zoneToApi.put("EEST", "Europe/Helsinki");
        zoneToApi.put("AST", "America/Halifax");
        zoneToApi.put("ADT", "America/Halifax");
        zoneToApi.put("IST", "Asia/Kolkata");
        zoneToApi.put("CST", "Asia/Shanghai");
        zoneToApi.put("JST", "Asia/Tokyo");
        zoneToApi.put("AEDT", "Australia/Sydney");
        zoneToApi.put("ACST", "Australia/Adelaide");
        zoneToApi.put("AKST", "America/Anchorage");
        zoneToApi.put("HST", "Pacific/Honolulu");
    }

    @Override
    public void start(Stage primaryStage) {
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
                String convertedTime = convertTime(time, fromZone, toZone);
                resultLabel.setText("Converted Time: " + convertedTime);
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
        primaryStage.setTitle("Time Zone Converter");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    private String getOffset(String zone) throws IOException {
        String apiZone = zoneToApi.get(zone);
        if (apiZone == null)
            return "+00:00";
        Request request = new Request.Builder()
                .url("http://worldtimeapi.org/api/timezone/" + apiZone)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            String json = response.body().string();
            Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
            }.getType());
            return (String) map.get("utc_offset");
        }
    }

    private int parseOffset(String offset) {
        String[] parts = offset.split(":");
        int hours = Integer.parseInt(parts[0]);
        int mins = Integer.parseInt(parts[1]);
        return hours * 60 + mins;
    }

    private String convertTime(String time, String fromZone, String toZone) throws IOException {
        String fromOffsetStr = getOffset(fromZone);
        String toOffsetStr = getOffset(toZone);
        int fromOff = parseOffset(fromOffsetStr);
        int toOff = parseOffset(toOffsetStr);
        String[] parts = time.split(" ");
        String timeStr = parts[1];
        String[] hm = timeStr.split(":");
        int hour = Integer.parseInt(hm[0]);
        int min = Integer.parseInt(hm[1]);
        int localMins = hour * 60 + min;
        int utcMins = localMins - fromOff;
        int targetMins = utcMins + toOff;
        int targetHour = (targetMins / 60) % 24;
        int targetMin = targetMins % 60;
        return String.format("%02d:%02d", targetHour, targetMin);
    }

    public static void main(String[] args) {
        launch(args);
    }
}