package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.ui.Model;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@SpringBootApplication
@Controller
public class GettingStartedApplication {

    @Autowired
    public GettingStartedApplication() {
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/api/get-observations")
    public String getObservations(
            @RequestParam(value = "q", required = false, defaultValue = "Diplacus aurantiacus") String query,
            Model model) {
        String fileUrl = "https://www.calflora.org/app/download?xun=136445&format=CSV&cols=Date&mtk=3U55LEOLT&cell=t&georef=a&georeferenced=t&ostatus=a&pcount=1&doc_type=rs&natural_status=w&taxon=Diplacus+aurantiacus&addnloc=t&cch=t&cnabh=t&wint=r";
        String localFilePath = "output.csv";
        List<String> contents = new ArrayList<String>();
        try {
            // Step 1: Download the file
            downloadFile(fileUrl, localFilePath);

            // Step 2: Load and print the file contents
            contents = printFileContents(localFilePath);

        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Parameter Value q: " + query);

        model.addAttribute("observations", contents);

        return "observations";
    }

    // @GetMapping("/database")
    // String database(Map<String, Object> model) {
    //     try (Connection connection = dataSource.getConnection()) {
    //         final var statement = connection.createStatement();
    //         statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
    //         statement.executeUpdate("INSERT INTO ticks VALUES (now())");

    //         final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
    //         final var output = new ArrayList<>();
    //         while (resultSet.next()) {
    //             output.add("Read from DB: " + resultSet.getTimestamp("tick"));
    //         }

    //         model.put("records", output);
    //         return "database";

    //     } catch (Throwable t) {
    //         model.put("message", t.getMessage());
    //         return "error";
    //     }
    // }

    // Downloads a file from a given URL and saves it locally
    private static void downloadFile(String fileUrl, String localFilePath) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Check if the response is OK (HTTP 200)
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(localFilePath)) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                // Read data in chunks and write to the local file
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                System.out.println("File downloaded successfully to: " + localFilePath);
            }
        } else {
            throw new IOException("Failed to download file. HTTP Response Code: " + responseCode);
        }
    }

    // Reads a file and prints its contents
    private static List<String> printFileContents(String filePath) throws IOException {
        List<String> contents = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contents.add(line);
            }
        }
        return contents;
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
