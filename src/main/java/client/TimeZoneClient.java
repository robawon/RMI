package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos; // Added import for Pos
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.TimeZoneConverter;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class TimeZoneClient extends Application {

    private TimeZoneConverter converter;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    @Override
    public void start(Stage primaryStage) {
        try {
            converter = null;

            VBox root = new VBox();
            root.setPadding(new Insets(20));
            root.setAlignment(Pos.CENTER); // Set alignment to center

            VBox card = new VBox(15);
            card.getStyleClass().add("card");
            card.setAlignment(Pos.CENTER);
            card.setMaxWidth(420);

            Label headerLabel = new Label("Time Zone Converter");
            headerLabel.getStyleClass().add("header-label");

            // DatePicker for date input
            DatePicker datePicker = new DatePicker(LocalDate.now());
            datePicker.setPromptText("Select date");
            datePicker.setMaxWidth(Double.MAX_VALUE);

            // Reintroduce TextField for time input, initialized with current time
            TextField timeField = new TextField(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            timeField.setPromptText("Enter time e.g. 2:30 PM");
            timeField.setMaxWidth(Double.MAX_VALUE); // Set max width for consistency

            // Connection controls
            TextField hostField = new TextField("127.0.0.1"); // Changed default to server IP
            hostField.setPromptText("RMI host");
            TextField portField = new TextField("1099");
            portField.setPromptText("RMI port");
            Button connectButton = new Button("Connect to Server");
            Button disconnectButton = new Button("Disconnect");
            disconnectButton.setDisable(true);
            Label statusLabel = new Label("Disconnected");

            // Get all available ZoneIds and sort them
            List<String> allZones = new ArrayList<>(ZoneId.getAvailableZoneIds());
            Collections.sort(allZones);

            ChoiceBox<String> fromZoneChoiceBox = new ChoiceBox<>();
            fromZoneChoiceBox.getItems().addAll(allZones);
            fromZoneChoiceBox.setValue(ZoneId.systemDefault().getId());
            fromZoneChoiceBox.setMaxWidth(Double.MAX_VALUE);

            ChoiceBox<String> toZoneChoiceBox = new ChoiceBox<>();
            toZoneChoiceBox.getItems().addAll(allZones);
            toZoneChoiceBox.setValue("UTC");
            toZoneChoiceBox.setMaxWidth(Double.MAX_VALUE);

            // New ChoiceBox for output format
            ChoiceBox<String> outputFormatChoiceBox = new ChoiceBox<>();
            outputFormatChoiceBox.getItems().addAll("12-hour (AM/PM)", "24-hour");
            outputFormatChoiceBox.setValue("12-hour (AM/PM)"); // Default to 12-hour format
            outputFormatChoiceBox.setDisable(true); // Initially disabled
            outputFormatChoiceBox.setMaxWidth(Double.MAX_VALUE);

            Label resultLabel = new Label();

            // News display box
            TextArea newsArea = new TextArea();
            newsArea.setEditable(false);
            newsArea.setWrapText(true);
            newsArea.setPrefHeight(60);
            newsArea.setPromptText("Regional news or historical events will appear here...");

            // Method to perform conversion
            Runnable performConversion = () -> {
                if (converter == null) {
                    resultLabel.setText("Not connected to server");
                    return;
                }
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate == null) {
                    resultLabel.setText("Please select a date.");
                    return;
                }
                try {
                    String selectedTime = timeField.getText();
                    if (selectedTime == null || selectedTime.isEmpty()) {
                        resultLabel.setText("Please enter a time.");
                        return;
                    }
                    String time = formatDateTime(selectedDate, selectedTime);
                    String fromZone = fromZoneChoiceBox.getValue();
                    String toZone = toZoneChoiceBox.getValue();
                    String result = converter.convertTime(time, fromZone, toZone);
                    String timeOnly = result.split(" ")[1]; // Extract time part after space

                    // Format the output based on user selection
                    LocalTime convertedLocalTime = LocalTime.parse(timeOnly);
                    String formattedOutput;
                    if ("12-hour (AM/PM)".equals(outputFormatChoiceBox.getValue())) {
                        formattedOutput = convertedLocalTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    } else {
                        formattedOutput = convertedLocalTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    }
                    resultLabel.setText("Converted Time: " + formattedOutput);
                    fetchNews(toZone, selectedDate, newsArea);
                } catch (Exception ex) {
                    // Detect connection / Remote exceptions and handle disconnect
                    Throwable cause = ex;
                    boolean connectionError = false;
                    while (cause != null) {
                        if (cause instanceof java.rmi.ConnectException || cause instanceof java.net.ConnectException
                                || cause instanceof java.rmi.RemoteException) {
                            connectionError = true;
                            break;
                        }
                        cause = cause.getCause();
                    }
                    if (connectionError) {
                        // Try one automatic reconnect attempt before giving up
                        statusLabel.setText("Connection lost, attempting reconnect...");
                        try {
                            // Use the host from hostField for reconnection
                            TimeZoneConverter newStub = (TimeZoneConverter) Naming
                                    .lookup("rmi://" + hostField.getText() + ":" + portField.getText()
                                            + "/TimeZoneConverter");
                            converter = newStub;
                            statusLabel.setText("Reconnected to server");
                            // retry conversion once
                            String selectedTime2 = timeField.getText();
                            if (selectedTime2 == null || selectedTime2.isEmpty()) {
                                resultLabel.setText("Please enter a time.");
                                return;
                            }
                            String time2 = formatDateTime(selectedDate, selectedTime2);
                            String fromZone2 = fromZoneChoiceBox.getValue();
                            String toZone2 = toZoneChoiceBox.getValue();
                            String result2 = converter.convertTime(time2, fromZone2, toZone2);
                            String timeOnly2 = result2.split(" ")[1];

                            LocalTime convertedLocalTime2 = LocalTime.parse(timeOnly2);
                            String formattedOutput2;
                            if ("12-hour (AM/PM)".equals(outputFormatChoiceBox.getValue())) {
                                formattedOutput2 = convertedLocalTime2
                                        .format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                            } else {
                                formattedOutput2 = convertedLocalTime2.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            }
                            resultLabel.setText("Converted Time: " + formattedOutput2);
                            fetchNews(toZone2, selectedDate, newsArea);
                            return;
                        } catch (Exception reconnectEx) {
                            // Reconnect failed: update UI and disable inputs
                            statusLabel.setText("Connection lost: " + reconnectEx.getMessage());
                            converter = null;
                            disconnectButton.setDisable(true);
                            connectButton.setDisable(false);
                            datePicker.setDisable(true);
                            timeField.setDisable(true); // Disable time field
                            fromZoneChoiceBox.setDisable(true);
                            toZoneChoiceBox.setDisable(true);
                            outputFormatChoiceBox.setDisable(true); // Disable output format choice box
                            resultLabel.setText("Not connected to server");
                            return;
                        }
                    }
                    resultLabel.setText("Error: " + ex.getMessage());
                }
            };

            // Initially disable input controls until connected
            datePicker.setDisable(true);
            timeField.setDisable(true); // Disable time field
            fromZoneChoiceBox.setDisable(true);
            toZoneChoiceBox.setDisable(true);
            outputFormatChoiceBox.setDisable(true); // Disable output format choice box

            // Connect button action: attempt RMI lookup
            connectButton.setOnAction(evt -> {
                String host = hostField.getText().trim();
                String portText = portField.getText().trim();
                int port = 1099;
                try {
                    port = Integer.parseInt(portText);
                } catch (NumberFormatException nfe) {
                    statusLabel.setText("Invalid port: " + portText);
                    return;
                }

                try {
                    // Try connecting using Registry directly
                    Registry registry = LocateRegistry.getRegistry(host, port);
                    converter = (TimeZoneConverter) registry.lookup("TimeZoneConverter");

                    statusLabel.setText("Connected to server at " + host + ":" + port);
                    connectButton.setDisable(true);
                    disconnectButton.setDisable(false);
                    datePicker.setDisable(false);
                    timeField.setDisable(false);
                    fromZoneChoiceBox.setDisable(false);
                    toZoneChoiceBox.setDisable(false);
                    outputFormatChoiceBox.setDisable(false);
                    performConversion.run();
                } catch (Exception e) {
                    // Fallback: If host was "localhost", try "127.0.0.1" explicitly
                    if ("localhost".equalsIgnoreCase(host)) {
                        try {
                            Registry registry = LocateRegistry.getRegistry("127.0.0.1", port);
                            converter = (TimeZoneConverter) registry.lookup("TimeZoneConverter");

                            statusLabel.setText("Connected to server at 127.0.0.1:" + port);
                            hostField.setText("127.0.0.1");
                            connectButton.setDisable(true);
                            disconnectButton.setDisable(false);
                            datePicker.setDisable(false);
                            timeField.setDisable(false);
                            fromZoneChoiceBox.setDisable(false);
                            toZoneChoiceBox.setDisable(false);
                            outputFormatChoiceBox.setDisable(false);
                            performConversion.run();
                            return;
                        } catch (Exception e2) {
                            // Fallback failed
                        }
                    }
                    statusLabel.setText("Connection error: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Disconnect action: clear converter and disable inputs
            disconnectButton.setOnAction(evt -> {
                try {
                    // Just drop reference to remote stub; do not attempt to unbind server-side
                    converter = null;
                    statusLabel.setText("Disconnected");
                    disconnectButton.setDisable(true);
                    connectButton.setDisable(false);
                    datePicker.setDisable(true);
                    timeField.setDisable(true); // Disable time field
                    fromZoneChoiceBox.setDisable(true);
                    toZoneChoiceBox.setDisable(true);
                    outputFormatChoiceBox.setDisable(true); // Disable output format choice box
                    resultLabel.setText("Not connected to server");
                } catch (Exception ex) {
                    statusLabel.setText("Error disconnecting: " + ex.getMessage());
                }
            });

            // Add listeners for real-time conversion (only meaningful once connected)
            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> performConversion.run());
            timeField.textProperty().addListener((obs, oldVal, newVal) -> performConversion.run());
            fromZoneChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> performConversion.run());
            toZoneChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> performConversion.run());
            outputFormatChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> performConversion.run()); // Listener
                                                                                                                 // for
                                                                                                                 // new
                                                                                                                 // choice
                                                                                                                 // box

            // Layout Organization
            HBox connectionBox = new HBox(10, hostField, portField);
            HBox.setHgrow(hostField, Priority.ALWAYS);
            hostField.setMaxWidth(Double.MAX_VALUE);
            portField.setPrefWidth(80);

            HBox actionBox = new HBox(10, connectButton, disconnectButton);
            actionBox.setAlignment(Pos.CENTER);
            connectButton.setMaxWidth(Double.MAX_VALUE);
            disconnectButton.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(connectButton, Priority.ALWAYS);
            HBox.setHgrow(disconnectButton, Priority.ALWAYS);

            // Apply Styles
            connectButton.getStyleClass().add("btn-primary");
            disconnectButton.getStyleClass().add("btn-secondary");
            statusLabel.getStyleClass().add("status-label");
            resultLabel.getStyleClass().add("result-box");
            resultLabel.setMaxWidth(Double.MAX_VALUE);
            newsArea.getStyleClass().add("news-area");

            card.getChildren().addAll(
                    headerLabel,
                    new Label("Server Connection"),
                    connectionBox,
                    actionBox,
                    statusLabel,
                    new Label("Date & Time"),
                    datePicker,
                    timeField, // Add time field to UI
                    new Label("From Zone"),
                    fromZoneChoiceBox,
                    new Label("To Zone"),
                    toZoneChoiceBox,
                    new Label("Output Format"), // Label for the new choice box
                    outputFormatChoiceBox, // Add output format choice box to UI
                    resultLabel,
                    new Label("Regional News"),
                    newsArea);

            root.getChildren().add(card);

            Scene scene = new Scene(root, 480, 750); // Increased height to accommodate new control
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            primaryStage.setTitle("Time Zone Converter Client");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchNews(String zoneAbbr, LocalDate date, TextArea newsArea) {
        boolean isToday = date.equals(LocalDate.now());
        String dateStr = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        if (isToday) {
            newsArea.setText("Loading news for " + zoneAbbr + "...");
        } else {
            newsArea.setText("Loading historical events for " + dateStr + "...");
        }

        new Thread(() -> {
            try {
                if (isToday) {
                    // Determine country code from Zone ID for the News API
                    // Supported by API: in, us, au, ru, fr, gb
                    String countryCode = "us"; // Default
                    if (zoneAbbr.startsWith("Australia/"))
                        countryCode = "au";
                    else if (zoneAbbr.startsWith("America/"))
                        countryCode = "us";
                    else if (zoneAbbr.equals("Europe/London"))
                        countryCode = "gb";
                    else if (zoneAbbr.equals("Europe/Paris"))
                        countryCode = "fr";
                    else if (zoneAbbr.equals("Asia/Kolkata"))
                        countryCode = "in";
                    else if (zoneAbbr.equals("Europe/Moscow"))
                        countryCode = "ru";

                    String url = "https://saurav.tech/NewsAPI/top-headlines/category/general/" + countryCode + ".json";

                    Request request = new Request.Builder().url(url).build();
                    try (Response response = httpClient.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String jsonResponse = response.body().string();
                            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                            JsonArray articles = jsonObject.getAsJsonArray("articles");

                            StringBuilder newsBuilder = new StringBuilder(
                                    "Top Headlines for " + dateStr + " (" + countryCode.toUpperCase() + "):\n");
                            for (int i = 0; i < Math.min(5, articles.size()); i++) {
                                JsonObject article = articles.get(i).getAsJsonObject();
                                String title = article.get("title").getAsString();
                                newsBuilder.append("• ").append(title).append("\n\n");
                            }
                            Platform.runLater(() -> newsArea.setText(newsBuilder.toString()));
                        } else {
                            Platform.runLater(() -> newsArea
                                    .setText("Could not fetch news. Server returned: " + response.code()));
                        }
                    }
                } else {
                    // Use Wikipedia On This Day API for past/future dates
                    String url = "https://en.wikipedia.org/api/rest_v1/feed/onthisday/events/" + date.getMonthValue()
                            + "/" + date.getDayOfMonth();

                    Request request = new Request.Builder()
                            .url(url)
                            .header("User-Agent", "TimeZoneConverter/1.0")
                            .build();

                    try (Response response = httpClient.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String jsonResponse = response.body().string();
                            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                            JsonArray events = jsonObject.getAsJsonArray("events");

                            StringBuilder newsBuilder = new StringBuilder("On this day ("
                                    + date.format(DateTimeFormatter.ofPattern("MMMM d")) + ") in history:\n");
                            for (int i = 0; i < Math.min(5, events.size()); i++) {
                                JsonObject event = events.get(i).getAsJsonObject();
                                String text = event.get("text").getAsString();
                                String year = event.get("year").getAsString();
                                newsBuilder.append("• ").append(year).append(": ").append(text).append("\n\n");
                            }
                            Platform.runLater(() -> newsArea.setText(newsBuilder.toString()));
                        } else {
                            Platform.runLater(() -> newsArea
                                    .setText("Could not fetch history. Server returned: " + response.code()));
                        }
                    }
                }
            } catch (Exception e) {
                Platform.runLater(() -> newsArea.setText("Error fetching news: " + e.getMessage()));
            }
        }).start();
    }

    private String formatDateTime(LocalDate date, String time12Hour) {
        String datePart = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try {
            // Attempt to parse with AM/PM
            LocalTime time = LocalTime.parse(time12Hour, DateTimeFormatter.ofPattern("hh:mm a"));
            return String.format("%s %02d:%02d:00", datePart, time.getHour(), time.getMinute());
        } catch (DateTimeParseException e) {
            try {
                // Attempt to parse with 24-hour format
                LocalTime time = LocalTime.parse(time12Hour, DateTimeFormatter.ofPattern("HH:mm"));
                return String.format("%s %02d:%02d:00", datePart, time.getHour(), time.getMinute());
            } catch (DateTimeParseException e2) {
                // Fallback for simple "H:M" or "HH:MM" without AM/PM
                String[] parts = time12Hour.split(":");
                if (parts.length == 2) {
                    try {
                        int hour = Integer.parseInt(parts[0].trim());
                        int minute = Integer.parseInt(parts[1].trim());
                        return String.format("%s %02d:%02d:00", datePart, hour, minute);
                    } catch (NumberFormatException nfe) {
                        // Invalid number format
                    }
                }
                // If all parsing fails, return original string or throw an exception
                throw new IllegalArgumentException("Invalid time format: " + time12Hour);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
